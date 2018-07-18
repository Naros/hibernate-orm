/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.tree.spi.expression;

import org.hibernate.sql.ast.consume.spi.SqlAstWalker;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;
import org.hibernate.sql.results.internal.SqlSelectionImpl;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public class ModFunction extends AbstractStandardFunction implements StandardFunction {
	private final Expression dividend;
	private final Expression divisor;
	private final BasicValuedExpressableType type;

	public ModFunction(Expression dividend, Expression divisor) {
		this( dividend, divisor, (BasicValuedExpressableType) dividend.getType() );
	}

	public ModFunction(
			Expression dividend,
			Expression divisor,
			BasicValuedExpressableType type) {
		this.dividend = dividend;
		this.divisor = divisor;
		this.type = type;
	}

	public Expression getDividend() {
		return dividend;
	}

	public Expression getDivisor() {
		return divisor;
	}

	@Override
	public void accept(SqlAstWalker walker) {
		walker.visitModFunction( this );
	}

	@Override
	public BasicValuedExpressableType getType() {
		return type;
	}

	@Override
	public SqlSelection createSqlSelection(
			int jdbcPosition,
			BasicJavaDescriptor javaTypeDescriptor,
			TypeConfiguration typeConfiguration) {
		return new SqlSelectionImpl(
				jdbcPosition,
				this,
				getType().getBasicType().getJdbcValueMapper( typeConfiguration )
		);
	}
}
