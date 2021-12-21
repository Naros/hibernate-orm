/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.query.criteria.internal;

import org.hibernate.envers.internal.tools.query.Parameters;
import org.hibernate.envers.internal.tools.query.QueryBuilder;
import org.hibernate.envers.query.criteria.AuditFunction;

/**
 * An audit query criterion where a function is compared to a property.
 *
 * @author Felix Feisst (feisst dot felix at gmail dot com)
 */
public class PropertyFunctionAuditExpression extends AbstractExpression {

	private AuditFunction function;
	private String otherAlias;
	private String otherPropertyName;
	private String op;

	public PropertyFunctionAuditExpression(
			AuditFunction function,
			String otherAlias,
			String otherPropertyName,
			String op) {
		this.function = function;
		this.otherAlias = otherAlias;
		this.otherPropertyName = otherPropertyName;
		this.op = op;
	}

	@Override
	public void addToQuery(String baseAlias, QueryBuilder qb, Parameters parameters) {
		String effectiveOtherAlias = otherAlias == null ? baseAlias : otherAlias;
		String otherEntityName = qb.getEntityNameByAlias( effectiveOtherAlias );
		/*
		 * Check that the other property name is not a relation. However, we can only do this for audited entities. If
		 * the other property belongs to a non-audited entity, we have to skip this check.
		 */
		if ( qb.getEnversService().getEntitiesConfigurations().isVersioned( otherEntityName ) ) {
			String otherPropertyNamePrefix = qb.resolveComponentPropertyPrefix( effectiveOtherAlias );
			checkPropertyNotRelation(
					qb.getEnversService(),
					otherEntityName,
					otherPropertyNamePrefix.concat( otherPropertyName )
			);
		}
		parameters.addWhereWithFunction(
				qb.getEnversService().getConfig(),
				qb,
				function,
				op,
				effectiveOtherAlias,
				otherPropertyName
		);
	}
}
