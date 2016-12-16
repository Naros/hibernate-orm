/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.spi.descriptor.java.managed.attribute;

import javax.persistence.metamodel.PluralAttribute;

import org.hibernate.type.spi.descriptor.java.JavaTypeDescriptor;

/**
 * @author Andrea Boriero
 */
public interface JavaTypeDescriptorPlularAttributeImplementor extends JavaTypeDescriptor, PluralAttribute {
	@Override
	default Class getJavaType() {
		return getJavaTypeClass();
	}
}
