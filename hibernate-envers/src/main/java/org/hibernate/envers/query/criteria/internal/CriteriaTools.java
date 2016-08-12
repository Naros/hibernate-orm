/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.query.criteria.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.envers.boot.internal.EnversService;
import org.hibernate.envers.exception.AuditException;
import org.hibernate.envers.internal.entities.EntityConfiguration;
import org.hibernate.envers.internal.entities.PropertyData;
import org.hibernate.envers.internal.entities.RelationDescription;
import org.hibernate.envers.internal.entities.RelationType;
import org.hibernate.envers.internal.reader.AuditReaderImplementor;
import org.hibernate.envers.query.criteria.AuditId;
import org.hibernate.envers.query.internal.property.ModifiedFlagPropertyName;
import org.hibernate.envers.query.internal.property.PropertyNameGetter;
import org.hibernate.type.EmbeddedComponentType;
import org.hibernate.type.Type;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public abstract class CriteriaTools {
	public static void checkPropertyNotARelation(
			EnversService enversService,
			String entityName,
			String propertyName) throws AuditException {
		if ( enversService.getEntitiesConfigurations().get( entityName ).isRelation( propertyName ) ) {
			throw new AuditException(
					"This criterion cannot be used on a property that is " +
							"a relation to another property."
			);
		}
	}

	public static RelationDescription getRelatedEntity(
			EnversService enversService,
			String entityName,
			String propertyName) throws AuditException {
		RelationDescription relationDesc = enversService.getEntitiesConfigurations().getRelationDescription( entityName, propertyName );

		if ( relationDesc == null ) {
			return null;
		}

		if ( relationDesc.getRelationType() == RelationType.TO_ONE ) {
			return relationDesc;
		}

		throw new AuditException(
				"This type of relation (" + entityName + "." + propertyName +
						") isn't supported and can't be used in queries."
		);
	}

	public static String determinePropertyName(
			EnversService enversService,
			AuditReaderImplementor versionsReader,
			String entityName,
			PropertyNameGetter propertyNameGetter) {
		final String propertyName;
		if ( propertyNameGetter instanceof ModifiedFlagPropertyName ) {
			// HHH-10398 - Will now resolve modified property names here.
			propertyName = getModifiedFlagPropertyName(
					enversService,
					entityName,
					propertyNameGetter.get( enversService )
			);
		}
		else {
			propertyName = propertyNameGetter.get( enversService );
		}
		return determinePropertyName(
				enversService,
				versionsReader,
				entityName,
				propertyName
		);
	}

	/**
	 * @param enversService The EnversService
	 * @param versionsReader Versions reader.
	 * @param entityName Original entity name (not audited).
	 * @param propertyName Property name or placeholder.
	 *
	 * @return Path to property. Handles identifier placeholder used by {@link org.hibernate.envers.query.criteria.AuditId}.
	 */
	public static String determinePropertyName(
			EnversService enversService,
			AuditReaderImplementor versionsReader,
			String entityName,
			String propertyName) {
		final SessionFactoryImplementor sessionFactory = versionsReader.getSessionImplementor().getFactory();

		if ( AuditId.IDENTIFIER_PLACEHOLDER.equals( propertyName ) ) {
			final String identifierPropertyName = sessionFactory.getMetamodel().entityPersister( entityName ).getIdentifierPropertyName();
			propertyName = enversService.getAuditEntitiesConfiguration().getOriginalIdPropName() + "." + identifierPropertyName;
		}
		else {
			final List<String> identifierPropertyNames = identifierPropertyNames( sessionFactory, entityName );
			if ( identifierPropertyNames.contains( propertyName ) ) {
				propertyName = enversService.getAuditEntitiesConfiguration().getOriginalIdPropName() + "." + propertyName;
			}
		}

		return propertyName;
	}

	/**
	 * @param sessionFactory Session factory.
	 * @param entityName Entity name.
	 *
	 * @return List of property names representing entity identifier.
	 */
	private static List<String> identifierPropertyNames(SessionFactoryImplementor sessionFactory, String entityName) {
		final String identifierPropertyName = sessionFactory.getMetamodel().entityPersister( entityName ).getIdentifierPropertyName();
		if ( identifierPropertyName != null ) {
			// Single id.
			return Arrays.asList( identifierPropertyName );
		}
		final Type identifierType = sessionFactory.getMetamodel().entityPersister( entityName ).getIdentifierType();
		if ( identifierType instanceof EmbeddedComponentType ) {
			// Multiple ids.
			final EmbeddedComponentType embeddedComponentType = (EmbeddedComponentType) identifierType;
			return Arrays.asList( embeddedComponentType.getPropertyNames() );
		}
		return Collections.emptyList();
	}

	/**
	 * Get the modified flag property name.
	 *
	 * @param enversService The EnversService.
	 * @param entityName Entity name.
	 * @param propertyName Property name.
	 * @return the modified flag property name.
	 */
	private static String getModifiedFlagPropertyName(EnversService enversService, String entityName, String propertyName) {
		String modifiedFlagPropertyName = propertyName;
		final EntityConfiguration entityConfiguration = enversService.getEntitiesConfigurations().get( entityName );
		for ( PropertyData propertyData : entityConfiguration.getPropertyMapper().getProperties().keySet() ) {
			if ( propertyData.getName().equals( propertyName ) ) {
				modifiedFlagPropertyName = propertyData.getModifiedFlagPropertyName();
				break;
			}
		}
		return modifiedFlagPropertyName;
	}
}
