/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.bytecode.enhance.spi.LazyPropertyInitializer;
import org.hibernate.cache.spi.access.EntityDataAccess;
import org.hibernate.cache.spi.entry.CacheEntry;
import org.hibernate.engine.internal.TwoPhaseLoad;
import org.hibernate.engine.internal.Versioning;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionEventListenerManager;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.event.spi.PreLoadEventListener;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.metamodel.model.domain.spi.PersistentAttribute;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.property.access.internal.PropertyAccessStrategyBackRefImpl;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.query.spi.QueryOptions;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.sql.results.internal.values.JdbcValuesSource;
import org.hibernate.sql.results.spi.JdbcValuesSourceProcessingOptions;
import org.hibernate.sql.results.spi.JdbcValuesSourceProcessingState;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.type.Type;
import org.hibernate.type.internal.TypeHelper;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class JdbcValuesSourceProcessingStateStandardImpl implements JdbcValuesSourceProcessingState {
	private static final Logger log = Logger.getLogger( JdbcValuesSourceProcessingStateStandardImpl.class );

	private final JdbcValuesSource jdbcValuesSource;
	private final ExecutionContext executionContext;
	private final JdbcValuesSourceProcessingOptions processingOptions;

	private Map<EntityKey,LoadingEntity> loadingEntityMap;
	private Map<Object,EntityKey> hydratedEntityKeys;

	// todo (6.0) : "loading collections" as well?

	public JdbcValuesSourceProcessingStateStandardImpl(
			JdbcValuesSource jdbcValuesSource,
			ExecutionContext executionContext,
			JdbcValuesSourceProcessingOptions processingOptions) {
		this.jdbcValuesSource = jdbcValuesSource;
		this.executionContext = executionContext;
		this.processingOptions = processingOptions;
	}

	@Override
	public QueryOptions getQueryOptions() {
		return executionContext.getQueryOptions();
	}

	@Override
	public JdbcValuesSourceProcessingOptions getProcessingOptions() {
		return processingOptions;
	}

	@Override
	public JdbcValuesSource getJdbcValuesSource() {
		return jdbcValuesSource;
	}

	@Override
	public void registerLoadingEntity(EntityKey entityKey, EntityDescriptor persister, Object entityInstance, Object[] hydratedState) {
		if ( loadingEntityMap == null ) {
			loadingEntityMap = new HashMap<>();
		}

		final LoadingEntity old = loadingEntityMap.computeIfAbsent(
				entityKey,
				key -> new LoadingEntity(
						entityKey,
						persister,
						entityInstance,
						null,
						hydratedState
				)
		);
		if ( old != null && old.entityInstance != entityInstance ) {
			log.debugf( "Encountered duplicate hydrating entity registration for same EntityKey [%s]", entityKey );
		}

		if ( hydratedEntityKeys == null ) {
			hydratedEntityKeys = new HashMap<>();
		}

		hydratedEntityKeys.put( entityInstance, entityKey );
	}

	@Override
	public SharedSessionContractImplementor getPersistenceContext() {
		return executionContext.getSession();
	}

	@Override
	public void finishUp() {
		// for arrays, we should end the collection load beforeQuery resolving the entities, since the
		// actual array instances are not instantiated during loading
		finishLoadingArrays();

		// now finish loading the entities (2-phase load)
		performTwoPhaseLoad();

		// now we can finalize loading collections
		finishLoadingCollections();

		// finally, perform post-load operations
		postLoad();
	}

	private void finishLoadingArrays() {
//		for ( CollectionReferenceInitializer arrayReferenceInitializer : arrayReferenceInitializers ) {
//			arrayReferenceInitializer.endLoading( context );
//		}
	}

	private void performTwoPhaseLoad() {
		if ( loadingEntityMap == null ) {
			return;
		}

		// IMPORTANT: reuse the same event instances for performance!
		final PreLoadEvent preLoadEvent;
		if ( executionContext.getSession().isEventSource() ) {
			preLoadEvent = new PreLoadEvent( (EventSource) executionContext.getSession() );
		}
		else {
			preLoadEvent = null;
		}

		log.tracev( "Total objects hydrated: {0}", loadingEntityMap.size() );

		// todo : consider the feasibility of building a dependency graph for association dependencies
		// 		e.g. Employee#1 should be resolved before Employee#2 when Employee#1 is Employee#2's manager
		//		this could happen inside #registerHydratedEntity

		for ( LoadingEntity loadingEntity : loadingEntityMap.values() ) {
			//TwoPhaseLoad.initializeEntity(
			initializeEntity(
					loadingEntity,
					isReadOnly(),
					executionContext.getSession(),
					preLoadEvent
			);
		}
	}

	@SuppressWarnings("SimplifiableIfStatement")
	private boolean isReadOnly() {
		if ( getQueryOptions().isReadOnly() != null ) {
			return getQueryOptions().isReadOnly();
		}

		if ( executionContext.getSession() instanceof EventSource ) {
			return ( (EventSource) executionContext.getSession() ).isDefaultReadOnly();
		}

		return false;
	}

	/**
	 * Copy of TwoPhaseLoad#initializeEntity until that can be adapted to this SQL-AST approach
	 */
	public void initializeEntity(
			final LoadingEntity loadingEntity,
			final boolean readOnly,
			final SharedSessionContractImplementor session,
			final PreLoadEvent preLoadEvent) {
		final PersistenceContext persistenceContext = session.getPersistenceContext();
		EntityEntry entityEntry = persistenceContext.getEntry( loadingEntity.entityInstance );
		if ( entityEntry == null ) {
			final EntityKey entityKey = hydratedEntityKeys.get( loadingEntity.entityInstance );
			if ( entityKey == null ) {
				throw new AssertionFailure( "possible non-threadsafe access to the session - could not locate EntityEntry for entity instance : " + loadingEntity.entityInstance  );
			}

			entityEntry = persistenceContext.addEntity(
					entityKey,
					Status.LOADING,
					loadingEntity.hydratedEntityState,
					loadingEntity.entityKey,
					// todo : we need to handle version
					null,
					// todo : handle LockMode
					LockMode.NONE,
					true,
					loadingEntity.persister.getEntityDescriptor(),
					// disableVersionIncrement?
					false
			);
		}
		doInitializeEntity( loadingEntity.entityInstance, entityEntry, readOnly, session, preLoadEvent );
	}

	private void doInitializeEntity(
			final Object entity,
			final EntityEntry entityEntry,
			final boolean readOnly,
			final SharedSessionContractImplementor session,
			final PreLoadEvent preLoadEvent) throws HibernateException {
		// see if there is a hydrating entity for this
		final PersistenceContext persistenceContext = session.getPersistenceContext();
		final EntityDescriptor persister = entityEntry.getPersister();
		final Serializable id = entityEntry.getId();
		final Object[] hydratedState = entityEntry.getLoadedState();

		final boolean debugEnabled = log.isDebugEnabled();
		if ( debugEnabled ) {
			log.debugf(
					"Resolving associations for %s",
					MessageHelper.infoString( persister, id, session.getFactory() )
			);
		}

		final Type[] types = persister.getPropertyTypes();
		for ( int i = 0; i < hydratedState.length; i++ ) {
			final Object value = hydratedState[i];
			if ( value!= LazyPropertyInitializer.UNFETCHED_PROPERTY && value!= PropertyAccessStrategyBackRefImpl.UNKNOWN ) {
				//hydratedState[i] = types[i].resolve( value, session, entity );
				throw new NotYetImplementedFor6Exception(  );
			}
		}

		//Must occur afterQuery resolving identifiers!
		if ( session.isEventSource() ) {
			preLoadEvent.setEntity( entity ).setState( hydratedState ).setId( id ).setPersister( persister );

			final EventListenerGroup<PreLoadEventListener> listenerGroup = session
					.getFactory()
					.getServiceRegistry()
					.getService( EventListenerRegistry.class )
					.getEventListenerGroup( EventType.PRE_LOAD );
			for ( PreLoadEventListener listener : listenerGroup.listeners() ) {
				listener.onPreLoad( preLoadEvent );
			}
		}

		persister.setPropertyValues( entity, hydratedState );

		final SessionFactoryImplementor factory = session.getFactory();
		final EntityDataAccess cacheAccess = factory.getCache().getEntityRegionAccess( persister.getHierarchy() );
		if ( cacheAccess != null && session.getCacheMode().isPutEnabled() ) {

			if ( debugEnabled ) {
				log.debugf(
						"Adding entity to second-level cache: %s",
						MessageHelper.infoString( persister, id, session.getFactory() )
				);
			}

			final Object version = Versioning.getVersion( hydratedState, persister );
			final CacheEntry entry = persister.buildCacheEntry( entity, hydratedState, version, session );
			final Object cacheKey = cacheAccess.generateCacheKey( id, persister.getHierarchy(), factory, session.getTenantIdentifier() );

			// explicit handling of caching for rows just inserted and then somehow forced to be read
			// from the database *within the same transaction*.  usually this is done by
			// 		1) Session#refresh, or
			// 		2) Session#clear + some form of load
			//
			// we need to be careful not to clobber the lock here in the cache so that it can be rolled back if need be
			if ( session.getPersistenceContext().wasInsertedDuringTransaction( persister, id ) ) {
				cacheAccess.update(
						session,
						cacheKey,
						persister.getCacheEntryStructure().structure( entry ),
						version,
						version
				);
			}
			else {
				final SessionEventListenerManager eventListenerManager = session.getEventListenerManager();
				try {
					eventListenerManager.cachePutStart();
					final boolean put = cacheAccess.putFromLoad(
							session,
							cacheKey,
							persister.getCacheEntryStructure().structure( entry ),
							version,
							//useMinimalPuts( session, entityEntry )
							false
					);

					if ( put && factory.getStatistics().isStatisticsEnabled() ) {
						factory.getStatistics().secondLevelCachePut( cacheAccess.getRegion().getName() );
					}
				}
				finally {
					eventListenerManager.cachePutEnd();
				}
			}
		}

		if ( persister.getHierarchy().getNaturalIdDescriptor() != null ) {
			persistenceContext.getNaturalIdHelper().cacheNaturalIdCrossReferenceFromLoad(
					persister,
					id,
					persistenceContext.getNaturalIdHelper().extractNaturalIdValues( hydratedState, persister )
			);
		}

		boolean isReallyReadOnly = readOnly;
		if ( !persister.getHierarchy().isMutable() ) {
			isReallyReadOnly = true;
		}
		else {
			final Object proxy = persistenceContext.getProxy( entityEntry.getEntityKey() );
			if ( proxy != null ) {
				// there is already a proxy for this impl
				// only set the status to read-only if the proxy is read-only
				isReallyReadOnly = ( (HibernateProxy) proxy ).getHibernateLazyInitializer().isReadOnly();
			}
		}
		if ( isReallyReadOnly ) {
			//no need to take a snapshot - this is a
			//performance optimization, but not really
			//important, except for entities with huge
			//mutable property values
			persistenceContext.setEntryStatus( entityEntry, Status.READ_ONLY );
		}
		else {
			//take a snapshot
			persister.visitAttributes(
					new TypeHelper.FilteredAttributeConsumer() {
						int i = 0;

						@Override
						protected boolean shouldAccept(PersistentAttribute attribute) {
							// "property update-ability"
							//		- org.hibernate.persister.entity.EntityPersister#getPropertyUpdateability
							return super.shouldAccept( attribute );
						}

						@Override
						protected void acceptAttribute(PersistentAttribute attribute) {
							hydratedState[i] = attribute.deepCopy( hydratedState[i], session );
							i++;
						}
					}
			);
			persistenceContext.setEntryStatus( entityEntry, Status.MANAGED );
		}

		persister.afterInitialize( entity, session );

		if ( debugEnabled ) {
			log.debugf(
					"Done materializing entity %s",
					MessageHelper.infoString( persister, id, session.getFactory() )
			);
		}

		if ( factory.getStatistics().isStatisticsEnabled() ) {
			factory.getStatistics().loadEntity( persister.getEntityName() );
		}
	}

	private void finishLoadingCollections() {
//		for ( InitializerCollection initializer : initializers ) {
//			initializer.endLoading( context );
//		}

		// todo (6.0) : need something like org.hibernate.engine.loading.internal.LoadingCollectionEntry

		throw new NotYetImplementedFor6Exception(  );
	}

	private void postLoad() {
		if ( loadingEntityMap == null ) {
			return;
		}

		// IMPORTANT: reuse the same event instances for performance!
		final PostLoadEvent postLoadEvent;
		if ( executionContext.getSession().isEventSource() ) {
			postLoadEvent = new PostLoadEvent( (EventSource) executionContext.getSession() );
		}
		else {
			postLoadEvent = null;
		}

		for ( LoadingEntity loadingEntity : loadingEntityMap.values() ) {
			TwoPhaseLoad.postLoad( loadingEntity.entityInstance, executionContext.getSession(), postLoadEvent );
		}
	}

	private static class LoadingEntity {
		private final EntityKey entityKey;
		private final EntityDescriptor persister;
		private final Object entityInstance;
		private final Object rowId;
		private final Object[] hydratedEntityState;

		public LoadingEntity(
				EntityKey entityKey,
				EntityDescriptor persister,
				Object entityInstance,
				Object rowId,
				Object[] hydratedEntityState) {
			this.entityKey = entityKey;
			this.persister = persister;
			this.entityInstance = entityInstance;
			this.rowId = rowId;
			this.hydratedEntityState = hydratedEntityState;
		}
	}
}
