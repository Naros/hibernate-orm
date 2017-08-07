/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

//$Id: CollectionInterceptor.java 7700 2005-07-30 05:02:47Z oneovthafew $
package org.hibernate.test.interceptor;

import java.io.Serializable;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

public class CollectionInterceptor extends EmptyInterceptor {

	@Override
	public boolean onFlushDirty(
			Object entity,
			Serializable id,
			Object[] currentState,
			Object[] previousState,
			String[] propertyNames,
			JavaTypeDescriptor[] javaTypeDescriptors) {
		( (User) entity ).getActions().add( "updated" );
		return false;
	}

	@Override
	public boolean onSave(
			Object entity,
			Serializable id,
			Object[] state,
			String[] propertyNames,
			JavaTypeDescriptor[] javaTypeDescriptors) {
		( (User) entity ).getActions().add( "created" );
		return false;
	}

}
