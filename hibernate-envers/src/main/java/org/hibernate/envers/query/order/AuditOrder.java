/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.query.order;

import org.hibernate.envers.internal.tools.query.QueryBuilder;

/**
 * @author Adam Warski (adam at warski dot org)
 * @author Chris Cranford
 */
public interface AuditOrder {
	/**
	 * Specifies the null order precedence for the order-by column specification.
	 *
	 * @param nullPrecedence the null precedence, may be {@code null}.
	 * @return this {@link AuditOrder} for chaining purposes
	 */
	AuditOrder nulls(NullPrecedence nullPrecedence);

	/**
	 * Add this order criteria to the specified query.
	 *
	 * @param baseAlias the alias, may be {@literal null}
	 * @param queryBuilder the query builder, must not be {@literal null}
	 */
	void addOrderToQuery(String baseAlias, QueryBuilder queryBuilder);
}
