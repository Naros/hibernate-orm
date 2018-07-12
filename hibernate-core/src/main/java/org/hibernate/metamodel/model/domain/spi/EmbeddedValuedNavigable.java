/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

import javax.persistence.metamodel.Type;

import org.hibernate.sql.ast.produce.metamodel.spi.EmbeddedValueExpressableType;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.ast.tree.spi.expression.domain.NavigableReference;
import org.hibernate.sql.results.internal.CompositeQueryResultImpl;
import org.hibernate.sql.results.internal.CompositeSqlSelectionGroupImpl;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.QueryResultCreationContext;
import org.hibernate.sql.results.spi.SqlSelectionGroupNode;
import org.hibernate.sql.results.spi.SqlSelectionResolutionContext;
import org.hibernate.type.descriptor.java.spi.EmbeddableJavaDescriptor;

/**
 * Describes parts of the domain model that can be composite values.
 *
 * @author Steve Ebersole
 */
public interface EmbeddedValuedNavigable<J> extends EmbeddedValueExpressableType<J>, NavigableContainer<J> {
	@Override
	EmbeddedContainer getContainer();

	EmbeddedTypeDescriptor<J> getEmbeddedDescriptor();

	@Override
	EmbeddableJavaDescriptor<J> getJavaTypeDescriptor();

	@Override
	default Type.PersistenceType getPersistenceType() {
		return Type.PersistenceType.EMBEDDABLE;
	}

	@Override
	default QueryResult createQueryResult(
			NavigableReference navigableReference,
			String resultVariable,
			QueryResultCreationContext creationContext) {
		return new CompositeQueryResultImpl(
				resultVariable,
				getEmbeddedDescriptor(),
				CompositeSqlSelectionGroupImpl.buildSqlSelectionGroup(
						getEmbeddedDescriptor(),
						navigableReference.getSqlExpressionQualifier(),
						creationContext
				)
		);
	}

	@Override
	default SqlSelectionGroupNode resolveSqlSelections(
			ColumnReferenceQualifier qualifier,
			SqlSelectionResolutionContext resolutionContext) {
		return getEmbeddedDescriptor().resolveSqlSelections( qualifier, resolutionContext );
	}

	@Override
	default int getNumberOfJdbcParametersNeeded() {
		return getEmbeddedDescriptor().getNumberOfJdbcParametersNeeded();
	}
}
