/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.query.projection;

import org.hibernate.envers.internal.entities.EntityInstantiator;

/**
 * @author Chris Cranford
 */
public abstract class AbstractAuditProjection implements AuditProjection {

	private final String alias;

	public AbstractAuditProjection(String alias) {
		this.alias = alias;
	}

	@Override
	public String getAlias(String baseAlias) {
		return alias == null ? baseAlias : alias;
	}

	@Override
	public Object convertQueryResult(
			EntityInstantiator entityInstantiator,
			String entityName,
			Number revision,
			Object value) {
		return value;
	}

	protected String getAlias() {
		return alias;
	}
}
