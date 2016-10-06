/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.internal.java.managed.identifier;

import javax.persistence.metamodel.EmbeddableType;

import org.hibernate.type.descriptor.spi.java.managed.IdentifierDescriptor;
import org.hibernate.type.descriptor.spi.java.managed.IdentifierDescriptorBuilder;

/**
 * @author Andrea Boriero
 */
public class IdentifierDescriptorBuilderAggregatedCompositeImpl implements IdentifierDescriptorBuilder {

	private String name;

	private EmbeddableType type;

	public IdentifierDescriptorBuilderAggregatedCompositeImpl setName(String name) {
		this.name = name;
		return this;
	}

	public IdentifierDescriptorBuilderAggregatedCompositeImpl setType(EmbeddableType type) {
		this.type = type;
		return this;
	}

	@Override
	public IdentifierDescriptor build() {
		return new IdentifierDescriptorAggregatedComposite( name, type );
	}
}
