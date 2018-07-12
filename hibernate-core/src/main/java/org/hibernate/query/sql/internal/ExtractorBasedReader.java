/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sql.internal;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.results.spi.JdbcValuesSourceProcessingState;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.sql.results.spi.SqlSelectionReader;

/**
 * @author Steve Ebersole
 */
public class ExtractorBasedReader implements SqlSelectionReader {
	private final JdbcValueMapper jdbcValueMapper;

	@SuppressWarnings("WeakerAccess")
	public ExtractorBasedReader(JdbcValueMapper jdbcValueMapper) {
		this.jdbcValueMapper = jdbcValueMapper;
	}

	@Override
	public Object read(
			ResultSet resultSet,
			JdbcValuesSourceProcessingState jdbcValuesSourceProcessingState,
			SqlSelection sqlSelection) throws SQLException {
		return jdbcValueMapper.getJdbcValueExtractor().extract(
				resultSet,
				sqlSelection.getJdbcResultSetIndex(),
				jdbcValuesSourceProcessingState.getExecutionContext()
		);
	}

	@Override
	public Object extractParameterValue(
			CallableStatement statement,
			JdbcValuesSourceProcessingState jdbcValuesSourceProcessingState,
			int jdbcParameterIndex) throws SQLException {
		return jdbcValueMapper.getJdbcValueExtractor().extract(
				statement,
				jdbcParameterIndex,
				jdbcValuesSourceProcessingState.getExecutionContext()
		);
	}

	@Override
	public Object extractParameterValue(
			CallableStatement statement,
			JdbcValuesSourceProcessingState jdbcValuesSourceProcessingState,
			String jdbcParameterName) throws SQLException {
		return jdbcValueMapper.getJdbcValueExtractor().extract(
				statement,
				jdbcParameterName,
				jdbcValuesSourceProcessingState.getExecutionContext()
		);
	}
}
