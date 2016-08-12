/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.test.integration.modifiedflags;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.envers.Audited;
import org.hibernate.envers.boot.internal.EnversService;
import org.hibernate.envers.configuration.EnversSettings;
import org.hibernate.envers.configuration.naming.DefaultModifiedFlagNamingStrategy;
import org.hibernate.envers.configuration.naming.ImprovedModifiedFlagNamingStrategy;
import org.hibernate.envers.internal.entities.EntityConfiguration;
import org.hibernate.envers.internal.entities.PropertyData;
import org.hibernate.envers.tools.Pair;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.testing.TestForIssue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Chris Cranford
 */
public class ModifiedFlagsDefaultNamingStrategyTest {

	@Test
	@TestForIssue(jiraKey = "HHH-10398")
	public void testDefaultNamingStrategyImplicit() {
		ServiceRegistry registry = buildServiceRegistry( null );
		try {
			buildMetadata( registry );

			assertPropertyNames(
					registry,
					Person.class,
					makePropertyPair( "realName", "realName_MOD" ),
					makePropertyPair( "nick", "nick_MOD" ),
					makePropertyPair( "addresses", "addresses_MOD" )
			);

			assertPropertyNames(
					registry,
					Address.class,
					makePropertyPair( "zipCode", "zipCode_MOD" ),
					makePropertyPair( "street", "street_MOD" ),
					makePropertyPair( "city", "city_MOD" )
			);

			assertPropertyNames(
					registry,
					Nickname.class,
					makePropertyPair( "established", "established_MOD" )
			);
		}
		finally {
			StandardServiceRegistryBuilder.destroy( registry );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HHH-10398")
	public void testDefaultNamingStrategyExplicit() {
		ServiceRegistry registry = buildServiceRegistry( DefaultModifiedFlagNamingStrategy.class.getName() );
		try {
			buildMetadata( registry );

			assertPropertyNames(
					registry,
					Person.class,
					makePropertyPair( "realName", "realName_MOD" ),
					makePropertyPair( "nick", "nick_MOD" ),
					makePropertyPair( "addresses", "addresses_MOD" )
			);

			assertPropertyNames(
					registry,
					Address.class,
					makePropertyPair( "zipCode", "zipCode_MOD" ),
					makePropertyPair( "street", "street_MOD" ),
					makePropertyPair( "city", "city_MOD" )
			);

			assertPropertyNames(
					registry,
					Nickname.class,
					makePropertyPair( "established", "established_MOD" )
			);
		}
		finally {
			StandardServiceRegistryBuilder.destroy( registry );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HHH-10398")
	public void testImprovedNamingStrategy() {
		ServiceRegistry registry = buildServiceRegistry( ImprovedModifiedFlagNamingStrategy.class.getName() );
		try {
			buildMetadata( registry );

			assertPropertyNames(
					registry,
					Person.class,
					makePropertyPair( "realName", "REAL_NAME_MOD" ),
					makePropertyPair( "nick", "nick_id_MOD" ),
					makePropertyPair( "addresses", "addresses_MOD" )
			);

			assertPropertyNames(
					registry,
					Address.class,
					makePropertyPair( "zipCode", "ZIPCODE_MOD" ),
					makePropertyPair( "street", "street_MOD" ),
					makePropertyPair( "city", "city_MOD" )
			);

			assertPropertyNames(
					registry,
					Nickname.class,
					makePropertyPair( "established", "established_MOD" )
			);
		}
		finally {
			StandardServiceRegistryBuilder.destroy( registry );
		}
	}

	private ServiceRegistry buildServiceRegistry(String strategyName) {
		StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder();
		ssrb.applySetting( EnversSettings.MODIFIED_FLAG_SUFFIX, "_MOD" );
		ssrb.applySetting( EnversSettings.GLOBAL_WITH_MODIFIED_FLAG, "true" );
		if ( strategyName != null ) {
			ssrb.applySetting( EnversSettings.MODIFIED_FLAG_NAMING_STRATEGY, strategyName );
		}
		return ssrb.configure().build();
	}

	private Metadata buildMetadata(ServiceRegistry registry) {
		return new MetadataSources( registry )
				.addAnnotatedClass( Person.class )
				.addAnnotatedClass( Address.class )
				.addAnnotatedClass( Nickname.class )
				.buildMetadata();
	}

	@SafeVarargs
	private final void assertPropertyNames(ServiceRegistry registry, Class<?> clazz, Pair<String, String>... pairs) {
		final EnversService enversService = registry.getService( EnversService.class );
		final EntityConfiguration config = enversService.getEntitiesConfigurations().get( clazz.getName() );
		for ( Pair<String, String> pair : pairs ) {
			boolean found = false;
			for ( PropertyData property : config.getPropertyMapper().getProperties().keySet() ) {
				if ( property.getName().equals( pair.getFirst() ) ) {
					found = true;
					assertEquals( pair.getSecond(), property.getModifiedFlagPropertyName() );
				}
			}
			if ( !found ) {
				throw new AssertionError(
						"No property named: " + pair.getFirst() + " found in class " + clazz.getName()
				);
			}
		}
	}

	private Pair<String, String> makePropertyPair(String propertyName, String modifiedPropertyName) {
		return new Pair<String, String>( propertyName, modifiedPropertyName );
	}

	@Entity(name = "Address")
	@Audited
	public static class Address {
		@Id
		@GeneratedValue
		@Column(name = "address_id")
		private Integer id;
		private String street;
		private String city;
		@Column(name = "ZIPCODE")
		private String zipCode;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getStreet() {
			return street;
		}

		public void setStreet(String street) {
			this.street = street;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getZipCode() {
			return zipCode;
		}

		public void setZipCode(String zipCode) {
			this.zipCode = zipCode;
		}
	}

	@Entity(name = "Nickname")
	@Audited
	public static class Nickname {
		@Id
		private String name;
		private Date established;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Date getEstablished() {
			return established;
		}

		public void setEstablished(Date established) {
			this.established = established;
		}
	}

	@Entity(name = "Person")
	@Audited
	public static class Person {
		@Id
		@GeneratedValue
		@Column(name = "person_id")
		private Integer id;

		@Column(name = "REAL_NAME")
		private String realName;

		@OneToOne
		@JoinColumn(name = "nick_id", referencedColumnName = "name")
		private Nickname nick;

		@OneToMany
		@JoinTable(joinColumns = @JoinColumn(name = "person_id"), inverseJoinColumns = @JoinColumn(name = "address_id"))
		private List<Address> addresses;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getRealName() {
			return realName;
		}

		public void setRealName(String realName) {
			this.realName = realName;
		}

		public Nickname getNick() {
			return nick;
		}

		public void setNick(Nickname nick) {
			this.nick = nick;
		}

		public List<Address> getAddresses() {
			return addresses;
		}

		public void setAddresses(List<Address> addresses) {
			this.addresses = addresses;
		}
	}
}
