/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.process.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.jaxb.spi.Binding;
import org.hibernate.boot.model.convert.spi.ConverterDescriptor;
import org.hibernate.boot.model.process.spi.ManagedResources;
import org.hibernate.boot.spi.BootstrapContext;

/**
 * @author Steve Ebersole
 */
public class ManagedResourcesImpl implements ManagedResources {
	private Map<Class, ConverterDescriptor> attributeConverterDescriptorMap = new HashMap<>();
	private Set<Class> annotatedClassReferences = new LinkedHashSet<>();
	private Set<String> annotatedClassNames = new LinkedHashSet<>();
	private Set<String> annotatedPackageNames = new LinkedHashSet<>();
	private List<Binding> mappingFileBindings = new ArrayList<>();

	public static ManagedResourcesImpl baseline(MetadataSources sources, BootstrapContext bootstrapContext) {
		final ManagedResourcesImpl impl = new ManagedResourcesImpl();
		bootstrapContext.getAttributeConverters().forEach( impl::addAttributeConverterDefinition );
		impl.annotatedClassReferences.addAll( sources.getAnnotatedClasses() );
		impl.annotatedClassNames.addAll( sources.getAnnotatedClassNames() );
		impl.annotatedPackageNames.addAll( sources.getAnnotatedPackages() );
		impl.mappingFileBindings.addAll( sources.getXmlBindings() );
		return impl;
	}

	private ManagedResourcesImpl() {
	}

	@Override
	public Collection<ConverterDescriptor> getAttributeConverterDescriptors() {
		return Collections.unmodifiableCollection( attributeConverterDescriptorMap.values() );
	}

	@Override
	public Collection<Class> getAnnotatedClassReferences() {
		return Collections.unmodifiableSet( annotatedClassReferences );
	}

	@Override
	public Collection<String> getAnnotatedClassNames() {
		return Collections.unmodifiableSet( annotatedClassNames );
	}

	@Override
	public Collection<String> getAnnotatedPackageNames() {
		return Collections.unmodifiableSet( annotatedPackageNames );
	}

	@Override
	public Collection<Binding> getXmlMappingBindings() {
		return Collections.unmodifiableList( mappingFileBindings );
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// package private

	void addAttributeConverterDefinition(ConverterDescriptor descriptor) {
		attributeConverterDescriptorMap.put( descriptor.getAttributeConverterClass(), descriptor );
	}

	void addAnnotatedClassReference(Class annotatedClassReference) {
		annotatedClassReferences.add( annotatedClassReference );
	}

	void addAnnotatedClassName(String annotatedClassName) {
		annotatedClassNames.add( annotatedClassName );
	}

	void addAnnotatedPackageName(String annotatedPackageName) {
		annotatedPackageNames.add( annotatedPackageName );
	}

	void addXmlBinding(Binding binding) {
		mappingFileBindings.add( binding );
	}
}
