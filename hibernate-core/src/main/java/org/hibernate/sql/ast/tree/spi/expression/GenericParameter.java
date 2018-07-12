/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.tree.spi.expression;

import org.hibernate.query.spi.QueryParameterBinding;
import org.hibernate.sql.ast.produce.spi.SqlExpressable;
import org.hibernate.sql.ast.produce.sqm.spi.ParameterSpec;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcParameterBinder;
import org.hibernate.sql.exec.spi.ParameterBindingContext;
import org.hibernate.sql.results.spi.Selectable;

/**
 * @author Steve Ebersole
 */
public interface GenericParameter extends ParameterSpec, JdbcParameterBinder, Expression, SqlExpressable, Selectable {
	@Override
	default JdbcParameterBinder getParameterBinder() {
		return this;
	}

	default QueryParameterBinding resolveBinding(ExecutionContext executionContext) {
		return resolveBinding( executionContext.getParameterBindingContext() );
	}

	QueryParameterBinding resolveBinding(ParameterBindingContext context);

}
