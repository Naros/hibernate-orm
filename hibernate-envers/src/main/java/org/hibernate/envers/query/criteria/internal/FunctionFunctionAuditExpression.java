/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.query.criteria.internal;

import org.hibernate.envers.internal.tools.query.Parameters;
import org.hibernate.envers.internal.tools.query.QueryBuilder;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.hibernate.envers.query.criteria.AuditFunction;

/**
 * An audit query criterion that defines a predicate where both sides are a function.
 *
 * @author Felix Feisst (feisst dot felix at gmail dot com)
 */
public class FunctionFunctionAuditExpression implements AuditCriterion {

	private AuditFunction leftFunction;
	private AuditFunction rightFunction;
	private String op;

	public FunctionFunctionAuditExpression(
			AuditFunction leftFunction,
			AuditFunction rightFunction,
			String op) {
		this.leftFunction = leftFunction;
		this.rightFunction = rightFunction;
		this.op = op;
	}

	@Override
	public void addToQuery(String baseAlias, QueryBuilder queryBuilder, Parameters parameters) {
		parameters.addWhereWithFunction( queryBuilder, leftFunction, op, rightFunction );
	}
}
