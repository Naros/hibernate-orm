/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.internal;

import org.hibernate.sql.NotYetImplementedException;
import org.hibernate.sql.exec.results.spi.FetchEntityAttribute;
import org.hibernate.sql.exec.results.spi.FetchParentAccess;
import org.hibernate.sql.exec.results.spi.RowProcessingState;

/**
 * @author Steve Ebersole
 */
public class EntityFetchInitializerImpl extends AbstractInitializerEntity {
	private final FetchParentAccess parentAccess;

	public EntityFetchInitializerImpl(
			FetchParentAccess parentAccess,
			FetchEntityAttribute entityReference,
			EntitySqlSelectionMappings sqlSelectionMappings,
			boolean isShallow) {
		super( entityReference.getEntityDescriptor(), sqlSelectionMappings, isShallow );
		this.parentAccess = parentAccess;
	}

	@Override
	protected boolean isEntityReturn() {
		return false;
	}

	@Override
	public void finishUpRow(RowProcessingState rowProcessingState) {
		// Use `parentAccess` to inject the parent instance into
		// the fetched entity
		super.finishUpRow( rowProcessingState );
	}

	@Override
	protected boolean shouldBatchFetch() {
		// todo (6.0) : implement this.
		// 		e.g. by adding a method to SingularAttributeEntity to see if it is
		//			a reference to the owner's pk or not

		//return !getEntityReference().getFetchedAttributeDescriptor().isReferenceToNonPk();

		throw new NotYetImplementedException(  );
	}
}
