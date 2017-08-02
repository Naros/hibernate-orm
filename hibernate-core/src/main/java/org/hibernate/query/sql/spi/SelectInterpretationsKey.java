/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sql.spi;

import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.query.ResultListTransformer;
import org.hibernate.query.TupleTransformer;
import org.hibernate.query.spi.QueryInterpretations;
import org.hibernate.sql.exec.results.spi.ResultSetMapping;

/**
 * @author Steve Ebersole
 */
public class SelectInterpretationsKey implements QueryInterpretations.Key {
	private final String sql;
	private final ResultSetMapping resultSetMapping;

	private final TupleTransformer tupleTransformer;
	private final ResultListTransformer resultListTransformer;

	public SelectInterpretationsKey(
			String sql,
			ResultSetMapping resultSetMapping,
			TupleTransformer tupleTransformer,
			ResultListTransformer resultListTransformer) {
		this.sql = sql;
		this.resultSetMapping = resultSetMapping;
		this.tupleTransformer = tupleTransformer;
		this.resultListTransformer = resultListTransformer;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		SelectInterpretationsKey that = (SelectInterpretationsKey) o;

		return sql.equals( that.sql )
				&& EqualsHelper.areEqual( resultSetMapping, that.resultSetMapping )
				&& EqualsHelper.areEqual( tupleTransformer, that.tupleTransformer )
				&& EqualsHelper.areEqual( resultListTransformer, that.resultListTransformer );

	}

	@Override
	public int hashCode() {
		int result = sql.hashCode();
		result = 31 * result + resultSetMapping.hashCode();
		result = 31 * result + ( tupleTransformer != null ? tupleTransformer.hashCode() : 0 );
		result = 31 * result + ( resultListTransformer != null ? resultListTransformer.hashCode() : 0 );
		return result;
	}
}
