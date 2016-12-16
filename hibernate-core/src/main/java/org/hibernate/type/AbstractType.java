/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.Size;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.type.spi.AssociationType;
import org.hibernate.type.spi.JdbcLiteralFormatter;
import org.hibernate.type.spi.Type;
import org.hibernate.type.spi.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.spi.descriptor.java.MutabilityPlan;

/**
 * Abstract superclass of the built in Type hierarchy.
 * 
 * @author Gavin King
 */
public abstract class AbstractType implements Type {
	protected static final Size LEGACY_DICTATED_SIZE = new Size();
	protected static final Size LEGACY_DEFAULT_SIZE = new Size( 19, 2, 255, Size.LobMultiplier.NONE ); // to match legacy behavior

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return null;
	}

	@Override
	public MutabilityPlan getMutabilityPlan() {
		return null;
	}

	@Override
	public JdbcLiteralFormatter getJdbcLiteralFormatter() {
		return null;
	}

	@Override
	public String asLoggableText() {
		return null;
	}

	@Override
	public boolean isAssociationType() {
		return false;
	}

	@Override
	public boolean isCollectionType() {
		return false;
	}

	@Override
	public boolean isComponentType() {
		return false;
	}

	@Override
	public boolean isEntityType() {
		return false;
	}

	public Serializable disassemble(Object value, SharedSessionContractImplementor session, Object owner)
	throws HibernateException {

		if (value==null) {
			return null;
		}
		else {
			return (Serializable) deepCopy( value, session.getFactory() );
		}
	}

	public Object assemble(Serializable cached, SharedSessionContractImplementor session, Object owner)
	throws HibernateException {
		if ( cached==null ) {
			return null;
		}
		else {
			return deepCopy( cached, session.getFactory() );
		}
	}

	@Override
	public boolean isDirty(Object old, Object current, SharedSessionContractImplementor session) throws HibernateException {
		return !isSame( old, current );
	}

	@Override
	public Object hydrate(
		ResultSet rs,
		String[] names,
		SharedSessionContractImplementor session,
		Object owner)
	throws HibernateException, SQLException {
		// TODO: this is very suboptimal for some subclasses (namely components),
		// since it does not take advantage of two-phase-load
		return nullSafeGet(rs, names, session, owner);
	}

	@Override
	public Object resolve(Object value, SharedSessionContractImplementor session, Object owner)
	throws HibernateException {
		return value;
	}

	@Override
	public Object semiResolve(Object value, SharedSessionContractImplementor session, Object owner) 
	throws HibernateException {
		return value;
	}

	@Override
	public boolean isAnyType() {
		return false;
	}

	@Override
	public boolean isModified(Object old, Object current, boolean[] checkable, SharedSessionContractImplementor session)
	throws HibernateException {
		return isDirty(old, current, session);
	}

	public boolean isSame(Object x, Object y) throws HibernateException {
		return isEqual(x, y );
	}

	@Override
	public boolean isEqual(Object x, Object y) {
		return EqualsHelper.equals(x, y);
	}

	@Override
	public boolean isEqual(Object x, Object y, SessionFactoryImplementor factory) {
		return isEqual(x, y );
	}

	@Override
	public Type getSemiResolvedType(SessionFactoryImplementor factory) {
		return this;
	}

	@Override
	public Object replace(
			Object original, 
			Object target, 
			SharedSessionContractImplementor session, 
			Object owner, 
			Map copyCache, 
			ForeignKeyDirection foreignKeyDirection) 
	throws HibernateException {
		boolean include;
		if ( isAssociationType() ) {
			AssociationType atype = (AssociationType) this;
			include = atype.getForeignKeyDirection()==foreignKeyDirection;
		}
		else {
			include = ForeignKeyDirection.FROM_PARENT ==foreignKeyDirection;
		}
		return include ? replace(original, target, session, owner, copyCache) : target;
	}
}
