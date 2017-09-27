/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.internal;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hibernate.HibernateException;
import org.hibernate.boot.model.domain.BasicValueMapping;
import org.hibernate.cfg.Environment;
import org.hibernate.mapping.RootClass;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.spi.AbstractSingularPersistentAttribute;
import org.hibernate.metamodel.model.domain.spi.EntityHierarchy;
import org.hibernate.metamodel.model.domain.spi.ManagedTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.VersionDescriptor;
import org.hibernate.metamodel.model.domain.spi.VersionSupport;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.ast.tree.spi.expression.domain.NavigableReference;
import org.hibernate.sql.results.internal.ScalarQueryResultImpl;
import org.hibernate.sql.results.internal.SqlSelectionGroupImpl;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.QueryResultCreationContext;
import org.hibernate.sql.results.spi.SqlSelectionGroup;
import org.hibernate.sql.results.spi.SqlSelectionGroupResolutionContext;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.descriptor.spi.ValueBinder;
import org.hibernate.type.descriptor.spi.ValueExtractor;
import org.hibernate.type.spi.BasicType;

/**
 * @author Steve Ebersole
 */
public class VersionDescriptorImpl<O,J>
		extends AbstractSingularPersistentAttribute<O,J>
		implements VersionDescriptor<O,J>, BasicValuedExpressableType<J> {
	private final BasicType<J> basicType;
	private final VersionSupport<J> versionSupport;

	private final Column column;
	private final String unsavedValue;

	@SuppressWarnings("unchecked")
	public VersionDescriptorImpl(
			EntityHierarchy hierarchy,
			RootClass rootEntityBinding,
			String name,
			boolean nullable,
			BasicValueMapping<J> bootMapping,
			String unsavedValue,
			RuntimeModelCreationContext creationContext) {
		super(
				hierarchy.getRootEntityType(),
				name,
				hierarchy.getRootEntityType().getRepresentationStrategy().generatePropertyAccess(
						rootEntityBinding,
						rootEntityBinding.getVersion(),
						hierarchy.getRootEntityType(),
						Environment.getBytecodeProvider()
				),
				Disposition.VERSION,
				nullable,
				bootMapping
		);
		this.column = creationContext.getDatabaseObjectResolver().resolveColumn( bootMapping.getMappedColumn() );
		this.unsavedValue = unsavedValue;

		this.basicType = bootMapping.resolveType();

		final Optional<VersionSupport<J>> versionSupportOptional = getBasicType().getVersionSupport();
		if ( ! versionSupportOptional.isPresent() ) {
			throw new HibernateException(
					"BasicType [" + basicType + "] associated with VersionDescriptor [" +
							hierarchy.getRootEntityType().getEntityName() +
							"] did not define VersionSupport"
			);
		}
		else {
			versionSupport = versionSupportOptional.get();
		}
	}

	@Override
	public PersistentAttributeType getPersistentAttributeType() {
		return PersistentAttributeType.BASIC;
	}

	@Override
	public String getUnsavedValue() {
		return unsavedValue;
	}

	@Override
	public VersionSupport getVersionSupport() {
		return versionSupport;
	}


	@Override
	public List<Column> getColumns() {
		return Collections.singletonList( column );
	}

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return SingularAttributeClassification.BASIC;
	}

	@Override
	public String asLoggableText() {
		return getContainer().asLoggableText() + '.' + getNavigableName();
	}

	@Override
	public Column getBoundColumn() {
		return column;
	}

	@Override
	public BasicType<J> getBasicType() {
		return basicType;
	}

	@Override
	@SuppressWarnings("unchecked")
	public BasicJavaDescriptor<J> getJavaTypeDescriptor() {
		return (BasicJavaDescriptor) super.getJavaTypeDescriptor();
	}

	@Override
	public QueryResult createQueryResult(
			NavigableReference navigableReference,
			String resultVariable,
			QueryResultCreationContext creationContext) {
		return new ScalarQueryResultImpl(
				resultVariable,
				creationContext.getSqlSelectionResolver().resolveSqlSelection(
						creationContext.getSqlSelectionResolver().resolveSqlExpression(
								navigableReference.getSqlExpressionQualifier(),
								VersionDescriptorImpl.this.column
						)
				),
				this
		);
	}

	@Override
	public ValueBinder getValueBinder() {
		return getBasicType().getValueBinder();
	}

	@Override
	public ValueExtractor getValueExtractor() {
		return getBasicType().getValueExtractor();
	}

	@Override
	public SqlSelectionGroup resolveSqlSelectionGroup(
			ColumnReferenceQualifier qualifier,
			SqlSelectionGroupResolutionContext resolutionContext) {
		return new SqlSelectionGroupImpl(
				resolutionContext.getSqlSelectionResolver().resolveSqlSelection(
						resolutionContext.getSqlSelectionResolver().resolveSqlExpression(
								qualifier,
								column
						)
				)
		);
	}
}
