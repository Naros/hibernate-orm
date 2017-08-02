/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.internal;

import org.hibernate.metamodel.model.domain.spi.PersistentAttribute;
import org.hibernate.sql.exec.results.spi.InitializerEntity;
import org.hibernate.sql.exec.results.spi.SqlSelection;
import org.hibernate.sql.exec.results.spi.SqlSelectionGroup;

/**
 * Used in {@link InitializerEntity} implementations
 *
 * @author Steve Ebersole
 */
public interface EntitySqlSelectionMappings {
	SqlSelection getRowIdSqlSelection();

	SqlSelectionGroup getIdSqlSelectionGroup();

	SqlSelection getDiscriminatorSqlSelection();

	SqlSelection getTenantDiscriminatorSqlSelection();

	SqlSelectionGroup getAttributeSqlSelectionGroup(PersistentAttribute attribute);
}
