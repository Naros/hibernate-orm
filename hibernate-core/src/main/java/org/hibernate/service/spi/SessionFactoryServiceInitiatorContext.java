/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.service.spi;

import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.engine.spi.SessionFactoryImplementor;

/**
 * @author Steve Ebersole
 */
public interface SessionFactoryServiceInitiatorContext {
	BootstrapContext getBootstrapContext();
	SessionFactoryImplementor getSessionFactory();
	SessionFactoryOptions getSessionFactoryOptions();
	ServiceRegistryImplementor getServiceRegistry();
}
