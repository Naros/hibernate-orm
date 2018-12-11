/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.produce.spi;

import org.hibernate.LockOptions;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.internal.util.collections.Stack;
import org.hibernate.sql.ast.produce.metamodel.spi.SqlAliasBaseGenerator;
import org.hibernate.sql.ast.tree.spi.QuerySpec;
import org.hibernate.sql.ast.tree.spi.expression.domain.NavigableReference;
import org.hibernate.sql.ast.tree.spi.from.TableGroup;
import org.hibernate.sql.ast.tree.spi.from.TableSpace;
import org.hibernate.sql.results.spi.AssemblerCreationContext;
import org.hibernate.sql.results.spi.DomainResultCreationContext;

/**
 * The "context" in which creation of SQL AST occurs.
 *
 * @author Steve Ebersole
 */
public interface SqlAstCreationContext extends AssemblerCreationContext, DomainResultCreationContext {
	SqlExpressionResolver getSqlSelectionResolver();

	LoadQueryInfluencers getLoadQueryInfluencers();

	default boolean shouldCreateShallowEntityResult() {
		return false;
	}

	LockOptions getLockOptions();

	SqlAliasBaseGenerator getSqlAliasBaseGenerator();

	TableSpace getTableSpace();

	QuerySpec getCurrentQuerySpec();

	Stack<TableGroup> getTableGroupStack();

	Stack<NavigableReference> getNavigableReferenceStack();
}
