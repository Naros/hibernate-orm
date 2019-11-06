/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.internal.tools;

import java.util.Objects;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.envers.exception.AuditException;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.resource.jdbc.spi.PhysicalConnectionHandlingMode;

/**
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public abstract class EntityTools {
	public static boolean entitiesEqual(SessionImplementor session, String entityName, Object obj1, Object obj2) {
		final Object id1 = getIdentifier( session, entityName, obj1 );
		final Object id2 = getIdentifier( session, entityName, obj2 );

		return Objects.deepEquals( id1, id2 );
	}

	public static Object getIdentifier(SessionImplementor session, String entityName, Object obj) {
		if ( obj == null ) {
			return null;
		}

		if ( obj instanceof HibernateProxy ) {
			final HibernateProxy hibernateProxy = (HibernateProxy) obj;
			return hibernateProxy.getHibernateLazyInitializer().getIdentifier();
		}

		return session.getEntityPersister( entityName, obj ).getIdentifier( obj, session );
	}

	public static Object getTargetFromProxy(SessionFactoryImplementor sessionFactoryImplementor, HibernateProxy proxy) {
		if ( !proxy.getHibernateLazyInitializer().isUninitialized() || activeProxySession( proxy ) ) {
			return proxy.getHibernateLazyInitializer().getImplementation();
		}

		final SharedSessionContractImplementor sessionImplementor = proxy.getHibernateLazyInitializer().getSession();
		final Session tempSession = sessionImplementor == null
				? openTemporarySession( sessionFactoryImplementor )
				: openTemporarySession( sessionImplementor.getFactory() );
		try {
			return tempSession.get(
					proxy.getHibernateLazyInitializer().getEntityName(),
					proxy.getHibernateLazyInitializer().getIdentifier()
			);
		}
		finally {
			tempSession.close();
		}
	}

	private static boolean activeProxySession(HibernateProxy proxy) {
		final Session session = (Session) proxy.getHibernateLazyInitializer().getSession();
		return session != null && session.isOpen() && session.isConnected();
	}

	/**
	 * @param clazz Class wrapped with a proxy or not.
	 * @param <T> Class type.
	 *
	 * @return Returns target class in case it has been wrapped with a proxy. If {@code null} reference is passed,
	 *         method returns {@code null}.
	 */
	@SuppressWarnings({"unchecked"})
	public static <T> Class<T> getTargetClassIfProxied(Class<T> clazz) {
		if ( clazz == null ) {
			return null;
		}
		else if ( HibernateProxy.class.isAssignableFrom( clazz ) ) {
			// Get the source class of Javassist proxy instance.
			return (Class<T>) clazz.getSuperclass();
		}
		return clazz;
	}

	/**
	 * @return Java class mapped to specified entity name.
	 */
	public static Class getEntityClass(SessionImplementor sessionImplementor, String entityName) {
		final EntityPersister entityPersister = sessionImplementor.getFactory().getMetamodel().entityPersister( entityName );
		return entityPersister.getMappedClass();
	}

	/**
	 * Creates a temporary {@link Session} that resolves all state at call time rather than what may
	 * have been cached by the {@link org.hibernate.SessionFactory} during construction.  This is important for
	 * when using {@link org.hibernate.context.spi.CurrentTenantIdentifierResolver}.
	 *
	 * @param sessionFactory the session factory
	 * @return the opened temporary session
	 * @throws AuditException if the call was unable to create a temporary session
	 */
	private static Session openTemporarySession(SessionFactoryImplementor sessionFactory) {
		try {
			return sessionFactory.withOptions()
					.autoClose( false )
					.connectionHandlingMode( PhysicalConnectionHandlingMode.DELAYED_ACQUISITION_AND_RELEASE_AFTER_TRANSACTION )
					.openSession();
		}
		catch (Exception e) {
			throw new AuditException( "Failed to create a temporary session", e );
		}
	}
}
