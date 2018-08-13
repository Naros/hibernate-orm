/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import org.hibernate.LockMode;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.FetchStrategy;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.metamodel.model.domain.spi.EntityValuedNavigable;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.results.spi.EntityFetch;
import org.hibernate.sql.results.spi.EntitySqlSelectionGroup;
import org.hibernate.sql.results.spi.FetchParent;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.InitializerCollector;
import org.hibernate.sql.results.spi.SqlAstCreationContext;

/**
 * @author Steve Ebersole
 */
public class EntityFetchImpl extends AbstractFetchParent implements EntityFetch {
	private final FetchParent fetchParent;
	private final ColumnReferenceQualifier qualifier;
	private final FetchStrategy fetchStrategy;
	private final LockMode lockMode;

	private final EntitySqlSelectionGroup sqlSelectionMappings;

	public EntityFetchImpl(
			FetchParent fetchParent,
			ColumnReferenceQualifier qualifier,
			EntityValuedNavigable fetchedNavigable,
			LockMode lockMode,
			NavigablePath navigablePath,
			FetchStrategy fetchStrategy,
			SqlAstCreationContext creationContext) {
		super( fetchedNavigable, navigablePath );
		this.fetchParent = fetchParent;
		this.qualifier = qualifier;
		this.lockMode = lockMode;

		// todo (6.0) : need the "fetch graph" / "entity graph" to really be able to perform this correctly
		//		see org.hibernate.sql.results.internal.EntitySqlSelectionGroupImpl.Builder#create

		this.sqlSelectionMappings = EntitySqlSelectionGroupImpl.buildSqlSelectionGroup(
				getEntityDescriptor(),
				qualifier,
				creationContext
		);
		this.fetchStrategy = fetchStrategy;
	}

	@Override
	public FetchParent getFetchParent() {
		return fetchParent;
	}

	@Override
	public ColumnReferenceQualifier getSqlExpressionQualifier() {
		return qualifier;
	}

	@Override
	public EntityValuedNavigable getFetchedNavigable() {
		return getFetchContainer();
	}

	@Override
	public FetchStrategy getFetchStrategy() {
		return fetchStrategy;
	}

	@Override
	public boolean isNullable() {
		throw new NotYetImplementedFor6Exception(  );
	}

	@Override
	public EntityValuedNavigable getFetchContainer() {
		return (EntityValuedNavigable) super.getFetchContainer();
	}

	@Override
	public EntityDescriptor getEntityDescriptor() {
		return getFetchContainer().getEntityDescriptor();
	}

	@Override
	public void registerInitializers(FetchParentAccess parentAccess, InitializerCollector collector) {
		final EntityFetchInitializer initializer = new EntityFetchInitializer(
				parentAccess,
				this,
				sqlSelectionMappings,
				lockMode,
				false
		);

		collector.addInitializer( initializer );
		registerFetchInitializers( initializer, collector );
	}
}
