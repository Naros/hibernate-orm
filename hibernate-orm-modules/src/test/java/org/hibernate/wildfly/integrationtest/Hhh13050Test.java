/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.wildfly.integrationtest;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.hibernate.Session;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.persistence10.PersistenceDescriptor;
import org.jboss.shrinkwrap.descriptor.api.persistence10.PersistenceUnitTransactionType;

/**
 * @author Chris Cranford
 */
@RunWith(Arquillian.class)
public class Hhh13050Test {
	private static final String ORM_VERSION = Session.class.getPackage().getImplementationVersion();
	private static final String ORM_MINOR_VERSION = ORM_VERSION.substring( 0, ORM_VERSION.indexOf( ".", ORM_VERSION.indexOf( "." ) + 1) );

	@Deployment
	public static WebArchive createDeployment() {
		return ShrinkWrap.create( WebArchive.class )
				.addClass( Comment.class )
				.addClass( EventLog.class )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" )
				.addAsResource( new StringAsset( persistenceXml().exportAsString() ), "META-INF/persistence.xml" );
	}

	private static PersistenceDescriptor persistenceXml() {
		return Descriptors.create( PersistenceDescriptor.class )
				.version( "2.1" )
				.createPersistenceUnit()
				.name( "templatePU" )
				.transactionType( PersistenceUnitTransactionType._JTA )
				.jtaDataSource( "java:jboss/datasources/ExampleDS" )
				.getOrCreateProperties()
				.createProperty().name( "jboss.as.jpa.providerModule" ).value( "org.hibernate:" + ORM_MINOR_VERSION ).up()
				.createProperty().name( "hibernate.hbm2ddl.auto" ).value( "create-drop" ).up()
				.createProperty().name( "hibernate.jdbc.batch_size" ).value( "500" ).up()
				//.createProperty().name( "hibernate.order_inserts" ).value( "true " ).up()
				.createProperty().name( "hibernate.show_sql" ).value( "true" ).up()
				.createProperty().name( "hibernate.format_sql" ).value( "true" ).up()
				.createProperty().name( "hibernate.generate_statistics" ).value( "true" ).up()
				.up()
				.up();
	}

	@PersistenceContext
	private EntityManager entityManager;

	@Inject
	private UserTransaction userTransaction;

	@Test
	public void testIt() {
		try {
			userTransaction.begin();

			entityManager.joinTransaction();

			entityManager.setFlushMode( FlushModeType.AUTO );

			// Persist entity with non-generated id
			EventLog eventLog1 = new EventLog();
			eventLog1.setMessage( "Foo1" );
			entityManager.persist( eventLog1 );

			// Persist entity with non-generated id
			EventLog eventLog2 = new EventLog();
			eventLog2.setMessage( "Foo2" );
			entityManager.persist( eventLog2 );

			Comment comment = new Comment();
			comment.setMessage( "Bar" );
			entityManager.persist( comment );

			userTransaction.commit();
		}
		catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}
}
