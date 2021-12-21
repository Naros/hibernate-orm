/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.query.criteria.internal;

import java.util.Locale;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.envers.exception.AuditException;
import org.hibernate.envers.internal.entities.RelationDescription;
import org.hibernate.envers.internal.entities.RelationType;
import org.hibernate.envers.internal.tools.query.Parameters;
import org.hibernate.envers.internal.tools.query.QueryBuilder;
import org.hibernate.envers.query.internal.property.PropertyNameGetter;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class SimpleAuditExpression extends AbstractAtomicExpression {

	private PropertyNameGetter propertyNameGetter;
	private Object value;
	private String op;

	public SimpleAuditExpression(String alias, PropertyNameGetter propertyNameGetter, Object value, String op) {
		super( alias );
		this.propertyNameGetter = propertyNameGetter;
		this.value = value;
		this.op = op;
	}

	@Override
	protected void addToQuery(
			String entityName,
			String alias,
			String componentPrefix,
			QueryBuilder qb,
			Parameters parameters) {
		String propertyName = qb.resolvePropertyName( entityName, propertyNameGetter );
		String prefixedPropertyName = componentPrefix.concat( propertyName );
		RelationDescription relatedEntity = qb.getRelationDescription( entityName, prefixedPropertyName );

		if ( relatedEntity == null ) {
			// HHH-9178 - Add support to component type equality.
			// This basically will allow = and <> operators to perform component-based equality checks.
			// Any other operator for a component type will not be supported.
			// Non-component types will continue to behave normally.
			final Type type = getPropertyType( qb.getSessionFactory(), entityName, propertyName );
			if ( type != null && type.isComponentType() ) {
				if ( !"=".equals( op ) && !"<>".equals( op ) ) {
					throw new AuditException(
							String.format(
									Locale.ENGLISH,
									"Component-based criterion is not supported for op: %s",
									op
							)
					);
				}
				final ComponentType componentType = (ComponentType) type;
				for ( int i = 0; i < componentType.getPropertyNames().length; i++ ) {
					final Object componentValue = componentType.getPropertyValue( value, i, null );
					parameters.addWhereWithParam(
							alias,
							prefixedPropertyName + "_" + componentType.getPropertyNames()[ i ],
							op,
							componentValue
					);
				}
			}
			else {
				parameters.addWhereWithParam( alias, prefixedPropertyName, op, value );
			}
		}
		else if ( relatedEntity.getRelationType() == RelationType.TO_ONE ) {
			if ( !"=".equals( op ) && !"<>".equals( op ) ) {
				throw new AuditException(
						String.format(
								Locale.ENGLISH,
								"This type of operation: %s (%s.%s) isn't supported and can't be used in queries.",
								op,
								entityName,
								propertyName
						)
				);
			}
			Object id = relatedEntity.getIdMapper().mapToIdFromEntity( value );
			relatedEntity.getIdMapper().addIdEqualsToQuery( parameters, id, alias, null, "=".equals( op ) );
		}
		else {
			throw new AuditException(
					String.format(
							"This type of relation (%s.%s) can't be used in audit query restrictions.",
							entityName,
							propertyName
					)
			);
		}
	}

	/**
	 * Get the property type of a given property in the specified entity.
	 *
	 * @param sessionFactory the session factory
	 * @param entityName the entity name
	 * @param propertyName the property name
	 * @return the property type of the property or {@code null} if the property name isn't found.
	 */
	private Type getPropertyType(SessionFactoryImplementor sessionFactory, String entityName, String propertyName) {
		// rather than rely on QueryException from calling getPropertyType(), this allows a non-failure way
		// to determine whether to return null or lookup the value safely.
		final EntityPersister persister = sessionFactory.getMetamodel().entityPersister( entityName );
		for ( String name : persister.getPropertyNames() ) {
			if ( name.equals( propertyName ) ) {
				return persister.getPropertyType( propertyName );
			}
		}
		return null;
	}
}
