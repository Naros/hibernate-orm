/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.query.projection;

import org.hibernate.envers.internal.entities.EntityInstantiator;
import org.hibernate.envers.internal.tools.query.QueryBuilder;

/**
 * @author Adam Warski (adam at warski dot org)
 * @author Chris Cranford
 */
public interface AuditProjection {

	/**
	 * Adds an audit projection to the specified query.
	 *
	 * @param baseAlias the base alias, if one is specified; may be {@literal null}
	 * @param queryBuilder the query builder
	 */
	void addProjectionToQuery(String baseAlias, QueryBuilder queryBuilder);

	/**
	 * Get the alias associated with the audit projection.
	 *
	 * @param baseAlias the base alias if one exists; may be {@literal null}
	 * @return the alias
	 */
	String getAlias(String baseAlias);

	/**
	 * @param entityInstantiator the entity instantiator
	 * @param entityName the name of the entity for which the projection has been added
	 * @param revision the revision
	 * @param value the value to convert
	 * @return the converted value
	 */
	Object convertQueryResult(EntityInstantiator entityInstantiator, String entityName, Number revision, Object value);

}
