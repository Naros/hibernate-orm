/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.consume.spi;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Performs parameter value binding to a JDBC PreparedStatement.
 *
 * @author Steve Ebersole
 * @author John O'Hara
 */
public interface JdbcParameterBinder {
	int bindParameterValue(
			PreparedStatement statement,
			int startPosition,
			ParameterBindingContext context) throws SQLException;

}
