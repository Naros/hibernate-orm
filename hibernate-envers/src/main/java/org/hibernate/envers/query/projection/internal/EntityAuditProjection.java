/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.query.projection.internal;

import java.util.Map;

import org.hibernate.envers.internal.entities.EntityInstantiator;
import org.hibernate.envers.internal.tools.query.QueryBuilder;
import org.hibernate.envers.query.projection.AbstractAuditProjection;

/**
 * @author Felix Feisst (feisst dot felix at gmail dot com)
 * @author Chris Cranford
 */
public class EntityAuditProjection extends AbstractAuditProjection {

	private final boolean distinct;

	public EntityAuditProjection(String alias, boolean distinct) {
		super( alias );
		this.distinct = distinct;
	}

	@Override
	public void addProjectionToQuery(String baseAlias, QueryBuilder queryBuilder) {
		String projectionEntityAlias = getAlias( baseAlias );
		queryBuilder.addProjection( null, projectionEntityAlias, null, distinct );
	}

	@Override
	public Object convertQueryResult(
			final EntityInstantiator entityInstantiator,
			final String entityName,
			final Number revision,
			final Object value) {
		if ( entityInstantiator.isVersioned( entityName ) ) {
			return entityInstantiator.createInstanceFromVersionsEntity( entityName, (Map) value, revision );
		}
		return super.convertQueryResult( entityInstantiator, entityName, revision, value );
	}

}
