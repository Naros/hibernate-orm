/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hamcrest.CoreMatchers;
import org.hibernate.Metamodel;
import org.hibernate.boot.MetadataSources;
import org.hibernate.metamodel.model.domain.internal.MappedSuperclassImpl;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.metamodel.model.domain.spi.MappedSuperclassDescriptor;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Chris Cranford
 */
public class MappedSuperclassTest extends SessionFactoryBasedFunctionalTest {
	@MappedSuperclass
	public static class AbstractEntity {
		@Id
		private Integer id;
		private String info;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getInfo() {
			return info;
		}

		public void setInfo(String info) {
			this.info = info;
		}
	}

	@Entity(name = "EntityA")
	public static class EntityA extends AbstractEntity {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		super.applyMetadataSources( metadataSources );

		metadataSources.addAnnotatedClass( EntityA.class );
	}

	@Override
	protected boolean exportSchema() {
		return true;
	}

	@Test
	public void testMetamodel() {
		sessionFactoryScope().inSession(
				session -> {
					final Metamodel metamodel = session.getFactory().getMetamodel();

					final EntityDescriptor entityDescriptor = metamodel.getEntityDescriptor( EntityA.class );
					assertThat( entityDescriptor, CoreMatchers.notNullValue() );
					assertThat( entityDescriptor.getSuperclassType(), CoreMatchers.instanceOf( MappedSuperclassImpl.class ) );
					assertThat( entityDescriptor.getStateArrayContributors().size(), CoreMatchers.is( 2 ) );

					// this fails, likely because we are not registering it?
					final MappedSuperclassDescriptor mappedSuperClassDescriptor = metamodel.getMappedSuperclassDescriptor( EntityA.class );
					assertThat( mappedSuperClassDescriptor, CoreMatchers.notNullValue() );
					assertThat( mappedSuperClassDescriptor.getSuperclassType(), CoreMatchers.nullValue() );
					assertThat( mappedSuperClassDescriptor.getStateArrayContributors().size(), CoreMatchers.is( 1 ) );
				}
		);
	}

	@Test
	public void testSimpleInsert() {
		sessionFactoryScope().inTransaction(
				session -> {
					final EntityA a  = new EntityA();
					a.setId( 1 );
					a.setName( "name" );
					a.setInfo( "info" );
					session.save( a );
				}
		);

		sessionFactoryScope().inTransaction(
				session -> {
					final EntityA a = session.load( EntityA.class, 1 );
					assertThat( a, CoreMatchers.notNullValue() );
					assertThat( a.getName(), CoreMatchers.is( "name" ) );
					assertThat( a.getInfo(), CoreMatchers.is( "info" ) );
				}
		);
	}
}
