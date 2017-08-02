/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.produce.spi;

import java.util.List;

import org.hibernate.sql.exec.results.spi.QueryResult;
import org.hibernate.sql.ast.tree.spi.SelectStatement;

/**
 *
 * @author Steve Ebersole
 */
public interface SqlAstSelectInterpretation extends SqlAstInterpretation {
	@Override
	SelectStatement getSqlAstStatement();

	List<QueryResult> getQueryResults();
}
