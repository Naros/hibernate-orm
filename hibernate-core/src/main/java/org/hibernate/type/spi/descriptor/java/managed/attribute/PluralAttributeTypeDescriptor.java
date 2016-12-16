/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.spi.descriptor.java.managed.attribute;

import java.lang.reflect.Member;
import java.util.Comparator;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.Type;

import org.hibernate.type.spi.descriptor.JdbcRecommendedSqlTypeMappingContext;
import org.hibernate.type.spi.descriptor.WrapperOptions;
import org.hibernate.type.spi.descriptor.java.MutabilityPlan;
import org.hibernate.type.spi.descriptor.sql.SqlTypeDescriptor;

/**
 * @author Andrea Boriero
 */
public class PluralAttributeTypeDescriptor implements JavaTypeDescriptorPlularAttributeImplementor {

	@Override
	public Type getElementType() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public PersistentAttributeType getPersistentAttributeType() {
		return null;
	}

	@Override
	public ManagedType getDeclaringType() {
		return null;
	}

	@Override
	public Member getJavaMember() {
		return null;
	}

	@Override
	public boolean isAssociation() {
		return false;
	}

	@Override
	public boolean isCollection() {
		return false;
	}

	@Override
	public BindableType getBindableType() {
		return null;
	}

	@Override
	public Class getBindableJavaType() {
		return null;
	}

	@Override
	public Class getJavaTypeClass() {
		return null;
	}

	@Override
	public SqlTypeDescriptor getJdbcRecommendedSqlType(JdbcRecommendedSqlTypeMappingContext context) {
		return null;
	}

	@Override
	public MutabilityPlan getMutabilityPlan() {
		return null;
	}

	@Override
	public Comparator getComparator() {
		return null;
	}

	@Override
	public int extractHashCode(Object value) {
		return 0;
	}

	@Override
	public boolean areEqual(Object one, Object another) {
		return false;
	}

	@Override
	public String extractLoggableRepresentation(Object value) {
		return null;
	}

	@Override
	public String toString(Object value) {
		return null;
	}

	@Override
	public Object fromString(String string) {
		return null;
	}

	@Override
	public Object wrap(Object value, WrapperOptions options) {
		return null;
	}

	@Override
	public Object unwrap(Object value, Class type, WrapperOptions options) {
		return null;
	}

	@Override
	public PersistenceType getPersistenceType() {
		return null;
	}
}
