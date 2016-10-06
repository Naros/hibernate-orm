/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.hbm;

import org.hibernate.boot.model.source.internal.hbm.EntityHierarchySourceImpl;
import org.hibernate.boot.model.source.internal.hbm.HbmManagedJavaTypeAttributeBinder;
import org.hibernate.boot.model.source.internal.hbm.HbmManagedJavaTypeDescriptorBinder;
import org.hibernate.boot.model.source.internal.hbm.HbmManagedJavaTypeIdentifierBinder;
import org.hibernate.boot.model.source.spi.IdentifierSourceAggregatedComposite;
import org.hibernate.boot.model.source.spi.IdentifierSourceNonAggregatedComposite;
import org.hibernate.boot.model.source.spi.IdentifierSourceSimple;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.cfg.NotYetImplementedException;

/**
 * @author Chris Cranford
 */
public class HbmMetadataSourceManagedTypeBuilder {

	private final HbmManagedJavaTypeDescriptorBinder descriptorBinder;
	private final HbmManagedJavaTypeAttributeBinder attributeBinder;
	private final HbmManagedJavaTypeIdentifierBinder identifierBinder;

	public HbmMetadataSourceManagedTypeBuilder(MetadataBuildingContext metadataBuildingContext) {
		this.descriptorBinder = new HbmManagedJavaTypeDescriptorBinder( metadataBuildingContext );
		this.attributeBinder = new HbmManagedJavaTypeAttributeBinder( metadataBuildingContext );
		this.identifierBinder = new HbmManagedJavaTypeIdentifierBinder( metadataBuildingContext );
	}

	public void buildEntityHierarchy(EntityHierarchySourceImpl hierarchySource) {
		descriptorBinder.bindDescriptors( hierarchySource );
		identifierBinder.bindIdentifier( hierarchySource );
		attributeBinder.bindAttributes( hierarchySource );
	}
}
