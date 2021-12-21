/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.query.criteria.internal;

import org.hibernate.envers.internal.tools.query.Parameters;
import org.hibernate.envers.internal.tools.query.QueryBuilder;
import org.hibernate.envers.query.internal.property.PropertyNameGetter;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class PropertyAuditExpression extends AbstractExpression {
	private String alias;
	private PropertyNameGetter propertyNameGetter;
	private String otherAlias;
	private String otherPropertyName;
	private String op;

	public PropertyAuditExpression(
			String alias,
			PropertyNameGetter propertyNameGetter,
			String otherAlias,
			String otherPropertyName,
			String op
	) {
		this.alias = alias;
		this.propertyNameGetter = propertyNameGetter;
		this.otherAlias = otherAlias;
		this.otherPropertyName = otherPropertyName;
		this.op = op;
	}

	@Override
	public void addToQuery(String baseAlias, QueryBuilder qb, Parameters parameters) {
		String effectiveAlias = alias == null ? baseAlias : alias;
		String effectiveOtherAlias = otherAlias == null ? baseAlias : otherAlias;
		String entityName = qb.getEntityNameByAlias( effectiveAlias );
		String otherEntityName = qb.getEntityNameByAlias( effectiveOtherAlias );
		String propertyName = qb.resolvePropertyName( entityName, propertyNameGetter );
		String propertyNamePrefix = qb.resolveComponentPropertyPrefix( effectiveAlias );
		String otherPropertyNamePrefix = qb.resolveComponentPropertyPrefix( effectiveOtherAlias );
		String prefixedPropertyName = propertyNamePrefix.concat( propertyName );
		String prefixedOtherPropertyName = otherPropertyNamePrefix.concat( otherPropertyName );
		checkPropertyNotRelation( qb.getEnversService(), entityName, prefixedPropertyName );
		/*
		 * Check that the other property name is not a relation. However, we can only
		 * do this for audited entities. If the other property belongs to a non-audited
		 * entity, we have to skip this check.
		 */
		if ( qb.getEnversService().getEntitiesConfigurations().isVersioned( otherEntityName ) ) {
			checkPropertyNotRelation( qb.getEnversService(), otherEntityName, prefixedOtherPropertyName );
		}
		parameters.addWhere( effectiveAlias, prefixedPropertyName, op, effectiveOtherAlias, prefixedOtherPropertyName );
	}
}
