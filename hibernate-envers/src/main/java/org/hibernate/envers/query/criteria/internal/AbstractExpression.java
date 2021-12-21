/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.query.criteria.internal;

import org.hibernate.envers.boot.internal.EnversService;
import org.hibernate.envers.exception.AuditException;
import org.hibernate.envers.query.criteria.AuditCriterion;

/**
 * @author Chris Cranford
 */
public abstract class AbstractExpression implements AuditCriterion {

	protected void checkPropertyNotRelation(EnversService enversService, String entityName, String propertyName) {
		if ( enversService.getEntitiesConfigurations().get( entityName ).isRelation( propertyName ) ) {
			throw new AuditException(
					"This criterion cannot be used on a property that is a relation to another property."
			);
		}
	}

}
