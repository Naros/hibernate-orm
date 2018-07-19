/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.tree.spi.expression;

import org.hibernate.QueryException;
import org.hibernate.metamodel.model.domain.spi.AllowableParameterType;
import org.hibernate.query.spi.QueryParameterBinding;
import org.hibernate.sql.ast.Clause;
import org.hibernate.sql.ast.consume.spi.SqlAstWalker;
import org.hibernate.sql.exec.spi.ParameterBindingContext;
import org.hibernate.type.spi.TypeConfiguration;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class PositionalParameter extends AbstractParameter {
	private static final Logger log = Logger.getLogger( PositionalParameter.class );

	private final int position;

	public PositionalParameter(
			int position,
			AllowableParameterType inferredType,
			Clause clause,
			TypeConfiguration typeConfiguration) {
		super( inferredType, clause, typeConfiguration );
		this.position = position;
	}

	public int getPosition() {
		return position;
	}

	@Override
	public QueryParameterBinding resolveBinding(ParameterBindingContext context) {
		return context.getQueryParameterBindings().getBinding( position );
	}

	@Override
	protected void warnNoBinding() {
		log.debugf( "Query defined positional parameter [%s], but no binding was found (setParameter not called)", getPosition() );
	}

	@Override
	protected void unresolvedType() {
		throw new QueryException( "Unable to determine Type for positional parameter [" + getPosition() + "]" );
	}

	@Override
	protected void warnNullBindValue() {
		log.debugf( "Binding value for positional parameter [:%s] was null", getPosition() );
	}

	@Override
	public void accept(SqlAstWalker  walker) {
		walker.visitPositionalParameter( this );
	}
}
