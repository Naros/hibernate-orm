/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.internal.java.managed.identifier;

import javax.persistence.metamodel.Type;

import org.hibernate.id.EntityIdentifierNature;
import org.hibernate.type.descriptor.spi.java.managed.IdentifierDescriptor;

/**
 * @author Andrea Boriero
 */
public class IdentifierDescriptorSimple<T>
		implements IdentifierDescriptor {

	private final String name;

	private final Type<T> type;

	@Override
	public EntityIdentifierNature getNature() {
		return EntityIdentifierNature.SIMPLE;
	}

	public IdentifierDescriptorSimple(String name, Type<T> type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	Type<T> getType() {
		return type;
	}
}
