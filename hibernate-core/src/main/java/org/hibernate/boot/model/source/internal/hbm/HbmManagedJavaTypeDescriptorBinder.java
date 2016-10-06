/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.hbm;

import org.hibernate.boot.model.source.spi.IdentifiableTypeSource;
import org.hibernate.boot.model.source.spi.InheritanceType;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.type.descriptor.internal.java.managed.RootEntityDescriptor;
import org.hibernate.type.descriptor.spi.java.managed.EntityHierarchy;
import org.hibernate.type.descriptor.spi.java.managed.JavaTypeDescriptorEntityImplementor;

/**
 * @author Chris Cranford
 */
public class HbmManagedJavaTypeDescriptorBinder extends AbstractJavaTypeDescriptorBinder {

	protected HbmManagedJavaTypeDescriptorBinder(MetadataBuildingContext metadataBuildingContext) {
		super( metadataBuildingContext );
	}

	public void bindDescriptors(final EntityHierarchySourceImpl hierarchySource) {
		final RootEntityDescriptor rootDescriptor = (RootEntityDescriptor) getJavaTypeDescriptorRegistry()
				.makeRootEntityDescriptor(
						hierarchySource.getRoot().getEntityNamingSource().getEntityName(),
						interpretInheritanceStyle( hierarchySource.getHierarchyInheritanceType() ),
						hierarchySource.getEntityMode()
				);
		if ( !InheritanceType.NO_INHERITANCE.equals( hierarchySource.getHierarchyInheritanceType() ) ) {
			bindSubclassDescriptors( hierarchySource.getRoot(), rootDescriptor );
		}
	}

	private void bindSubclassDescriptors(IdentifiableTypeSource entitySource, JavaTypeDescriptorEntityImplementor parentDescriptor) {
		for ( IdentifiableTypeSource subType : entitySource.getSubTypes() ) {
			bindSubclassDescriptor( subType, parentDescriptor );
		}
	}

	private void bindSubclassDescriptor(
			IdentifiableTypeSource entitySource,
			JavaTypeDescriptorEntityImplementor superTypeDescriptor) {
		final JavaTypeDescriptorEntityImplementor entityDescriptor = getJavaTypeDescriptorRegistry()
				.makeEntityDescriptor(
						entitySource.getTypeName(),
						superTypeDescriptor
				);
		entityDescriptor.getInitializationAccess().setSuperType( superTypeDescriptor );

		bindSubclassDescriptors( entitySource, entityDescriptor );
	}

	private EntityHierarchy.InheritanceStyle interpretInheritanceStyle(InheritanceType hierarchyInheritanceType) {
		switch ( hierarchyInheritanceType ) {
			case JOINED: {
				return EntityHierarchy.InheritanceStyle.JOINED;
			}
			case UNION: {
				return EntityHierarchy.InheritanceStyle.TABLE_PER_CLASS;
			}
			default: {
				return EntityHierarchy.InheritanceStyle.SINGLE_TABLE;
			}
		}
	}

}
