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
import org.hibernate.envers.query.internal.property.PropertyNameGetter;

/**
 * An audit query criterion that defines a predicate that is a comparison between a function
 * and an audit property expression.
 *
 * @author Felix Feisst (feisst dot felix at gmail dot com)
 */
public class FunctionPropertyAuditExpression extends AbstractExpression {

	private String alias;
	private PropertyNameGetter propertyNameGetter;
	private AuditFunction function;
	private String op;

	public FunctionPropertyAuditExpression(
			String alias,
			PropertyNameGetter propertyNameGetter,
			AuditFunction function,
			String op) {
		this.alias = alias;
		this.propertyNameGetter = propertyNameGetter;
		this.function = function;
		this.op = op;
	}

	@Override
	public void addToQuery(String baseAlias, QueryBuilder qb, Parameters parameters) {
		String effectiveAlias = alias == null ? baseAlias : alias;
		String entityName = qb.getEntityNameByAlias( effectiveAlias );
		String propertyName = qb.resolvePropertyName( entityName, propertyNameGetter );
		String propertyNamePrefix = qb.resolveComponentPropertyPrefix( effectiveAlias );

		String prefixedPropertyName = propertyNamePrefix.concat( propertyName );
		checkPropertyNotRelation( qb.getEnversService(), entityName, prefixedPropertyName );
		parameters.addWhereWithFunction(
				qb.getEnversService().getConfig(),
				qb,
				effectiveAlias,
				prefixedPropertyName,
				op,
				function
		);
	}
}
