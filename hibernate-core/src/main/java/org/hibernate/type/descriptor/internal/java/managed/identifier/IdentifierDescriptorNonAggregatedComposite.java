/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.internal.java.managed.identifier;

import java.util.Collection;
import java.util.Map;
import javax.persistence.metamodel.Type;

import org.hibernate.id.EntityIdentifierNature;
import org.hibernate.type.descriptor.spi.java.managed.IdentifierDescriptor;

/**
 * @author Andrea Boriero
 */
public class IdentifierDescriptorNonAggregatedComposite implements IdentifierDescriptor {

	private Class idClassType;
	private Map<String, Type> ids;

	@Override
	public EntityIdentifierNature getNature() {
		return EntityIdentifierNature.NON_AGGREGATED_COMPOSITE;
	}

	Class getIdClassJavaType(){
		return idClassType;
	}

	boolean hasIdClass() {
		return idClassType != null;
	}

	Collection<String> getIdNames(){
		return ids.keySet();
	}

	Type getType(String name){
		return ids.get( name );
	}
}
