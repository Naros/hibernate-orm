/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.hbm;

import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.type.descriptor.spi.java.JavaTypeDescriptorRegistry;

/**
 * @author Andrea Boriero
 */
public abstract class AbstractJavaTypeDescriptorBinder {
	private final JavaTypeDescriptorRegistry javaTypeDescriptorRegistry;

	protected AbstractJavaTypeDescriptorBinder(MetadataBuildingContext metadataBuildingContext) {
		this.javaTypeDescriptorRegistry = metadataBuildingContext.getMetadataCollector()
				.getTypeConfiguration()
				.getJavaTypeDescriptorRegistry();
	}

	protected JavaTypeDescriptorRegistry getJavaTypeDescriptorRegistry() {
		return javaTypeDescriptorRegistry;
	}
}
