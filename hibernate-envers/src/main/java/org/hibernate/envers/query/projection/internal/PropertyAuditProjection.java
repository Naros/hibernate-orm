/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.query.projection.internal;

import org.hibernate.envers.internal.tools.query.QueryBuilder;
import org.hibernate.envers.query.internal.property.PropertyNameGetter;
import org.hibernate.envers.query.projection.AbstractAuditProjection;

/**
 * @author Adam Warski (adam at warski dot org)
 * @author Chris Cranford
 */
public class PropertyAuditProjection extends AbstractAuditProjection {
	private final PropertyNameGetter propertyNameGetter;
	private final String function;
	private final boolean distinct;

	public PropertyAuditProjection(String alias, PropertyNameGetter propertyNameGetter, String function, boolean distinct) {
		super( alias );
		this.propertyNameGetter = propertyNameGetter;
		this.function = function;
		this.distinct = distinct;
	}

	@Override
	public void addProjectionToQuery(String baseAlias, QueryBuilder queryBuilder) {
		String projectionEntityAlias = getAlias( baseAlias );
		String projectionEntityName = queryBuilder.getEntityNameByAlias( projectionEntityAlias );
		String propertyName = queryBuilder.resolvePropertyName( projectionEntityName, propertyNameGetter );
		String propertyNamePrefix = queryBuilder.resolveComponentPropertyPrefix( projectionEntityAlias );

		queryBuilder.addProjection( function, projectionEntityAlias, propertyNamePrefix.concat( propertyName ), distinct );
	}
}
