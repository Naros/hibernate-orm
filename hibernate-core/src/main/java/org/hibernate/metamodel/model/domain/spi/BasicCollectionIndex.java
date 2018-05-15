/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

/**
 * @author Steve Ebersole
 */
public interface BasicCollectionIndex<T> extends CollectionIndex<T>, BasicValuedNavigable<T> {
	@Override
	default IndexClassification getClassification() {
		return IndexClassification.BASIC;
	}

	@Override
	default void visitNavigable(NavigableVisitationStrategy visitor) {
		visitor.visitCollectionIndexBasic( this );
	}
}
