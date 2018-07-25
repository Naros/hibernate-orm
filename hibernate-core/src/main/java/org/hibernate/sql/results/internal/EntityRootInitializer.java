/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import org.hibernate.LockMode;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.sql.results.spi.EntitySqlSelectionGroup;

/**
 * InitializerEntity for root
 * @author Steve Ebersole
 */
public class EntityRootInitializer
		extends AbstractEntityInitializer {
	public EntityRootInitializer(
			EntityDescriptor entityDescriptor,
			EntitySqlSelectionGroup sqlSelectionMappings,
			LockMode lockMode,
			boolean isShallow) {
		super( entityDescriptor, sqlSelectionMappings, lockMode, isShallow );
	}

	@Override
	protected boolean isEntityReturn() {
		return true;
	}
}
