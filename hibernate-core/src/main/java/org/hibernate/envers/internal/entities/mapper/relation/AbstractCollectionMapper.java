/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.internal.entities.mapper.relation;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.hibernate.collection.internal.PersistentMap;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.exception.AuditException;
import org.hibernate.envers.internal.entities.PropertyData;
import org.hibernate.envers.internal.entities.mapper.AbstractPropertyMapper;
import org.hibernate.envers.internal.entities.mapper.PersistentCollectionChangeData;
import org.hibernate.envers.internal.entities.mapper.relation.lazy.initializor.Initializor;
import org.hibernate.envers.internal.reader.AuditReaderImplementor;
import org.hibernate.envers.internal.tools.ReflectionTools;
import org.hibernate.metamodel.model.domain.spi.PersistentCollectionDescriptor;
import org.hibernate.property.access.spi.Setter;

/**
 * @author Adam Warski (adam at warski dot org)
 * @author Michal Skowronek (mskowr at o2 dot pl)
 * @author Chris Cranford
 */
public abstract class AbstractCollectionMapper<T> extends AbstractPropertyMapper {
	protected final CommonCollectionMapperData commonCollectionMapperData;
	protected final Class<? extends T> collectionClass;
	protected final boolean ordinalInId;
	protected final boolean revisionTypeInId;

	private final Constructor<? extends T> proxyConstructor;

	protected AbstractCollectionMapper(
			CommonCollectionMapperData commonCollectionMapperData,
			Class<? extends T> collectionClass,
			Class<? extends T> proxyClass,
			boolean ordinalInId,
			boolean revisionTypeInId) {
		this.commonCollectionMapperData = commonCollectionMapperData;
		this.collectionClass = collectionClass;
		this.ordinalInId = ordinalInId;
		this.revisionTypeInId = revisionTypeInId;

		try {
			proxyConstructor = proxyClass.getConstructor( Initializor.class );
		}
		catch (NoSuchMethodException e) {
			throw new AuditException( e );
		}
	}

	protected abstract Collection getNewCollectionContent(PersistentCollection newCollection);

	protected abstract Collection getOldCollectionContent(Serializable oldCollection);

	protected abstract Set<Object> buildCollectionChangeSet(Object eventCollection, Collection collection);

	/**
	 * Maps the changed collection element to the given map.
	 *
	 * @param idData Map to which composite-id data should be added.
	 * @param data Where to map the data.
	 * @param changed The changed collection element to map.
	 */
	protected abstract void mapToMapFromObject(
			SessionImplementor session,
			Map<String, Object> idData,
			Map<String, Object> data,
			Object changed);

	/**
	 * Creates map for storing identifier data. Ordinal parameter guarantees uniqueness of primary key.
	 * Composite primary key cannot contain embeddable properties since they might be nullable.
	 *
	 * @param ordinal Iteration ordinal.
	 *
	 * @return Map for holding identifier data.
	 */
	protected Map<String, Object> createIdMap(int ordinal) {
		final Map<String, Object> idMap = new HashMap<>();
		if ( ordinalInId ) {
			idMap.put( commonCollectionMapperData.getOptions().getEmbeddableSetOrdinalPropertyName(), ordinal );
		}
		return idMap;
	}

	protected void addCollectionChanges(
			SessionImplementor session,
			List<PersistentCollectionChangeData> collectionChanges,
			Set<Object> changed,
			RevisionType revisionType,
			Object id) {
		int ordinal = 0;

		for ( Object changedObj : changed ) {
			final Map<String, Object> entityData = new HashMap<>();
			final Map<String, Object> originalId = createIdMap( ordinal++ );
			entityData.put( commonCollectionMapperData.getOptions().getOriginalIdPropName(), originalId );

			collectionChanges.add(
					new PersistentCollectionChangeData(
							commonCollectionMapperData.getVersionsMiddleEntityName(),
							entityData,
							changedObj
					)
			);
			// Mapping the collection owner's id.
			commonCollectionMapperData.getReferencingIdData().getPrefixedMapper().mapToMapFromId( originalId, id );

			// Mapping collection element and index (if present).
			mapToMapFromObject( session, originalId, entityData, changedObj );

			( revisionTypeInId ? originalId : entityData ).put(
					commonCollectionMapperData.getOptions().getRevisionTypePropName(),
					revisionType
			);
		}
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public List<PersistentCollectionChangeData> mapCollectionChanges(
			SessionImplementor session,
			String referencingPropertyName,
			PersistentCollection newColl,
			Serializable oldColl,
			Object id) {
		final PropertyData collectionPropertyData = commonCollectionMapperData.getCollectionReferencingPropertyData();
		if ( !collectionPropertyData.getName().equals( referencingPropertyName ) ) {
			return null;
		}

//		// HHH-11063
//		final CollectionEntry collectionEntry = session.getPersistenceContext().getCollectionEntry( newColl );
//		if ( collectionEntry != null ) {
//			// This next block delegates only to the descriptor-based collection change code if
//			// the following are true:
//			//	1. New collection is not a PersistentMap.
//			//	2. The collection has a collection descriptor.
//			//	3. The collection is not indexed, e.g. @IndexColumn
//			//
//			// In the case of 1 and 3, the collection is transformed into a set of Pair<> elements where the
//			// pair's left element is either the map key or the index.  In these cases, the key/index do
//			// affect the change code; hence why they're skipped here and handled at the end.
//			//
//			// For all others, the descriptor based method uses the collection's ElementType#isSame to calculate
//			// equality between the newColl and oldColl.  This enforces the same equality check that core uses
//			// for element types such as @Entity in cases where the hash code does not use the id field but has
//			// the same value in both collections.  Using #isSame, these will be seen as differing elements and
//			// changes to the collection will be returned.
//			if ( !( newColl instanceof PersistentMap ) ) {
//				final PersistentCollectionDescriptor collectionDescriptor = collectionEntry.getCurrentDescriptor();
//				if ( collectionDescriptor != null && collectionDescriptor.getIndexDescriptor() != null ) {
//					return mapCollectionChanges( session, newColl, oldColl, id, collectionDescriptor );
//				}
//			}
//		}

		return mapCollectionChanges( session, newColl, oldColl, id );
	}

	@Override
	public boolean mapToMapFromEntity(
			SessionImplementor session,
			Map<String, Object> data,
			Object newObj,
			Object oldObj) {
		// Changes are mapped in the "mapCollectionChanges" method.
		return false;
	}

	@Override
	public void mapModifiedFlagsToMapFromEntity(
			SessionImplementor session,
			Map<String, Object> data,
			Object newObj,
			Object oldObj) {
		final PropertyData propertyData = commonCollectionMapperData.getCollectionReferencingPropertyData();
		if ( propertyData.isUsingModifiedFlag() ) {
			if ( isNotPersistentCollection( newObj ) || isNotPersistentCollection( oldObj ) ) {
				// Compare POJOs.
				data.put( propertyData.getModifiedFlagPropertyName(), !Objects.equals( newObj, oldObj ) );
			}
			else if ( isFromNullToEmptyOrFromEmptyToNull( (PersistentCollection) newObj, (Serializable) oldObj ) ) {
				data.put( propertyData.getModifiedFlagPropertyName(), true );
			}
			else {
				// HHH-7949 - Performance optimization to avoid lazy-fetching collections that have
				// not been changed for deriving the modified flags value.
				final PersistentCollection pc = (PersistentCollection) newObj;
				if ( ( pc != null && !pc.isDirty() ) || ( newObj == null && oldObj == null ) ) {
					data.put( propertyData.getModifiedFlagPropertyName(), false );
					return;
				}

				final List<PersistentCollectionChangeData> changes = mapCollectionChanges(
						session,
						commonCollectionMapperData.getCollectionReferencingPropertyData().getName(),
						(PersistentCollection) newObj,
						(Serializable) oldObj,
						null
				);
				data.put( propertyData.getModifiedFlagPropertyName(), !changes.isEmpty() );
			}
		}
	}

	private boolean isNotPersistentCollection(Object obj) {
		return obj != null && !(obj instanceof PersistentCollection);
	}

	private boolean isFromNullToEmptyOrFromEmptyToNull(PersistentCollection newColl, Serializable oldColl) {
		// Comparing new and old collection content.
		final Collection newCollection = getNewCollectionContent( newColl );
		final Collection oldCollection = getOldCollectionContent( oldColl );

		return oldCollection == null && newCollection != null && newCollection.isEmpty()
				|| newCollection == null && oldCollection != null && oldCollection.isEmpty();
	}

	@Override
	public void mapModifiedFlagsToMapForCollectionChange(String collectionPropertyName, Map<String, Object> data) {
		final PropertyData propertyData = commonCollectionMapperData.getCollectionReferencingPropertyData();
		if ( propertyData.isUsingModifiedFlag() ) {
			data.put(
					propertyData.getModifiedFlagPropertyName(),
					propertyData.getName().equals( collectionPropertyName )
			);
		}
	}

	protected abstract Initializor<T> getInitializor(
			AuditReaderImplementor versionsReader,
			Object primaryKey,
			Number revision,
			boolean removed);

	protected PersistentCollectionDescriptor resolveCollectionDescriptor(
			SessionImplementor session,
			PersistentCollection collection) {
		// First attempt to resolve the descriptor from the collection entry
		if ( collection != null ) {
			CollectionEntry collectionEntry = session.getPersistenceContext().getCollectionEntry( collection );
			if ( collectionEntry != null ) {
				PersistentCollectionDescriptor descriptor = collectionEntry.getCurrentDescriptor();
				if ( descriptor != null ) {
					return descriptor;
				}
			}
		}

		// Fallback to resolving the descripto from the collection role
		final PersistentCollectionDescriptor descriptor = session.getFactory()
				.getMetamodel()
				.getCollectionDescriptor( commonCollectionMapperData.getRole() );

		if ( descriptor == null ) {
			throw new AuditException(
					String.format(
							Locale.ROOT,
							"Failed to locate PersistentCollectionDescriptor for collection [%s].",
							commonCollectionMapperData.getRole()
					)
			);
		}

		return descriptor;
	}

	/**
	 * Checks whether the old and new collection elements are the same.
	 * <p>
	 * By default this delegates to the persistent collection descriptor's {@code JavaTypeDescriptor}.
	 *
	 * @param collectionDescriptor The collection descriptor.
	 * @param oldObject The collection element from the old persistent collection.
	 * @param newObject The collection element from the new persistent collection.
	 *
	 * @return {@code true} if the objects are the same, {@code false} otherwise.
	 */
	@SuppressWarnings("unchecked")
	protected boolean isSame(PersistentCollectionDescriptor collectionDescriptor, Object oldObject, Object newObject) {
		return collectionDescriptor.getElementDescriptor().getJavaTypeDescriptor().areEqual( oldObject, newObject );
	}

	@Override
	public void mapToEntityFromMap(
			Object obj,
			Map data,
			Object primaryKey,
			AuditReaderImplementor versionsReader,
			Number revision) {
		final String revisionTypePropertyName = versionsReader.getAuditService().getOptions().getRevisionTypePropName();

		// construct the collection proxy
		final Object collectionProxy;
		try {
			collectionProxy = proxyConstructor.newInstance(
					getInitializor(
							versionsReader,
							primaryKey,
							revision,
							RevisionType.DEL.equals( data.get( revisionTypePropertyName ) )
					)
			);
		}
		catch ( Exception e ) {
			throw new AuditException( "Failed to construct collection proxy", e );
		}

		final PropertyData collectionPropertyData = commonCollectionMapperData.getCollectionReferencingPropertyData();

		if ( isDynamicComponentMap() ) {
			@SuppressWarnings("unchecked")
			final Map<String, Object> map = (Map<String, Object>) obj;
			map.put( collectionPropertyData.getBeanName(), collectionProxy );
		}
		else {
			final PrivilegedAction<Object> delegatedAction = new PrivilegedAction<Object>() {
				@Override
				public Object run() {
					final Setter setter = ReflectionTools.getSetter(
							obj.getClass(),
							collectionPropertyData,
							versionsReader.getSessionImplementor().getSessionFactory().getServiceRegistry()
					);

					setter.set( obj, collectionProxy, null );
					return null;
				}
			};

			if ( System.getSecurityManager() != null ) {
				AccessController.doPrivileged( delegatedAction );
			}
			else {
				delegatedAction.run();
			}
		}
	}

	/**
	 * Map collection changes using hash identity.
	 *
	 * @param session The session.
	 * @param newColl The new persistent collection.
	 * @param oldColl The old collection.
	 * @param id The owning entity identifier.
	 * @return the persistent collection changes.
	 */
	protected abstract List<PersistentCollectionChangeData> mapCollectionChanges(
			SessionImplementor session,
			PersistentCollection newColl,
			Serializable oldColl,
			Object id
	);
//		final List<PersistentCollectionChangeData> collectionChanges = new ArrayList<>();
//
//		// Comparing new and old collection content.
//		final Collection newCollection = getNewCollectionContent( newColl );
//		final Collection oldCollection = getOldCollectionContent( oldColl );
//
//		final Set<Object> added = buildCollectionChangeSet( newColl, newCollection );
//		// Re-hashing the old collection as the hash codes of the elements there may have changed, and the
//		// removeAll in AbstractSet has an implementation that is hashcode-change sensitive (as opposed to addAll).
//		if ( oldColl != null ) {
//			added.removeAll( new HashSet( oldCollection ) );
//		}
//		addCollectionChanges( session, collectionChanges, added, RevisionType.ADD, id );
//
//		final Set<Object> deleted = buildCollectionChangeSet( oldColl, oldCollection );
//		// The same as above - re-hashing new collection.
//		if ( newColl != null ) {
//			deleted.removeAll( new HashSet( newCollection ) );
//		}
//		addCollectionChanges( session, collectionChanges, deleted, RevisionType.DEL, id );
//
//		return collectionChanges;
//	}

//	/**
//	 * Map collection changes using the collection element type equality functionality.
//	 *
//	 * @param session The session.
//	 * @param newColl The new persistent collection.
//	 * @param oldColl The old collection.
//	 * @param id The owning entity identifier.
//	 * @param collectionDescriptor The collection persister.
//	 * @return the persistent collection changes.
//	 */
//	@SuppressWarnings("unchecked")
//	private List<PersistentCollectionChangeData> mapCollectionChanges(
//			SessionImplementor session,
//			PersistentCollection newColl,
//			Serializable oldColl,
//			Object id,
//			PersistentCollectionDescriptor collectionDescriptor) {
//
//		final List<PersistentCollectionChangeData> collectionChanges = new ArrayList<>();
//
//		// Comparing new and old collection content.
//		final Collection newCollection = getNewCollectionContent( newColl );
//		final Collection oldCollection = getOldCollectionContent( oldColl );
//
//		// take the new collection and remove any that exist in the old collection.
//		// take the resulting Set<> and generate ADD changes.
//		final Set<Object> added = buildCollectionChangeSet( newColl, newCollection );
//		if ( oldColl != null && collectionDescriptor != null ) {
//			for ( Object object : oldCollection ) {
//				for ( Iterator addedIt = added.iterator(); addedIt.hasNext(); ) {
//					Object object2 = addedIt.next();
//					if ( collectionDescriptor.getElementDescriptor().getJavaTypeDescriptor().areEqual( object, object2 ) ) {
//						addedIt.remove();
//						break;
//					}
//				}
//			}
//		}
//		addCollectionChanges( session, collectionChanges, added, RevisionType.ADD, id );
//
//		// take the old collection and remove any that exist in the new collection.
//		// take the resulting Set<> and generate DEL changes.
//		final Set<Object> deleted = buildCollectionChangeSet( oldColl, oldCollection );
//		if ( newColl != null && collectionDescriptor != null ) {
//			for ( Object object : newCollection ) {
//				for ( Iterator deletedIt = deleted.iterator(); deletedIt.hasNext(); ) {
//					Object object2 = deletedIt.next();
//					if ( collectionDescriptor.getElementDescriptor().getJavaTypeDescriptor().areEqual( object, object2 ) ) {
//						deletedIt.remove();
//						break;
//					}
//				}
//			}
//		}
//		addCollectionChanges( session, collectionChanges, deleted, RevisionType.DEL, id );
//
//		return collectionChanges;
//	}

	@Override
	public boolean hasPropertiesWithModifiedFlag() {
		if ( commonCollectionMapperData != null ) {
			final PropertyData propertyData = commonCollectionMapperData.getCollectionReferencingPropertyData();
			return propertyData != null && propertyData.isUsingModifiedFlag();
		}
		return false;
	}
}
