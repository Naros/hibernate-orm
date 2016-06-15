/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.test.integration.kolekyr;

import javax.persistence.EntityManager;

import org.hibernate.envers.test.BaseEnversJPAFunctionalTestCase;
import org.hibernate.envers.test.Priority;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Chris Cranford
 */
public class PersonEmbeddableTest extends BaseEnversJPAFunctionalTestCase{
	private Integer personId;

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] {
				Person.class,
				ContactInfo.class
		};
	}

	@Test
	@Priority(10)
	public void initData() {
		EntityManager entityManager = getEntityManager();
		try {

			Person p = new Person();
			p.setName( "Naros" );
			p.setContactInfo( new ContactInfo( "naros at hibernate.org", "8885551212" ) );

			// revision 1
			entityManager.getTransaction().begin();
			entityManager.persist( p );
			entityManager.getTransaction().commit();
			entityManager.clear();

			// revision 2 - change embeddable
			entityManager.getTransaction().begin();
			p = entityManager.find( Person.class, p.getId() );
			p.getContactInfo().setEmailAddress( "chris at hibernate dot org" );
			entityManager.merge( p );
			entityManager.getTransaction().commit();
			entityManager.clear();

			// revision 3 - change main entity
			entityManager.getTransaction().begin();
			p = entityManager.find( Person.class, p.getId() );
			p.setName( "Chris" );
			entityManager.merge( p );
			entityManager.getTransaction().commit();
			entityManager.clear();

			// revision 4 - change both
			entityManager.getTransaction().begin();
			p = entityManager.find( Person.class, p.getId() );
			p.setName( "ChrisC" );
			p.getContactInfo().setPhoneNumber( "1-800-555-1212" );
			entityManager.merge( p );
			entityManager.getTransaction().commit();

			this.personId = p.getId();
		}
		finally {
			entityManager.close();
		}
	}

	@Test
	public void testRevisionCounts() {
		assertEquals( 4, getAuditReader().getRevisions( Person.class, personId ).size() );
	}

	@Test
	public void testRevisionHistory() {
		// verify revision 1
		final Person rev1 = getAuditReader().find( Person.class, personId, 1 );
		assertEquals( "Naros", rev1.getName() );
		assertEquals( "naros at hibernate.org", rev1.getContactInfo().getEmailAddress() );
		assertEquals( "8885551212", rev1.getContactInfo().getPhoneNumber() );
		// verify revision 2
		final Person rev2 = getAuditReader().find( Person.class, personId, 2 );
		assertEquals( "Naros", rev2.getName() );
		assertEquals( "chris at hibernate dot org", rev2.getContactInfo().getEmailAddress() );
		assertEquals( "8885551212", rev2.getContactInfo().getPhoneNumber() );
		// verify revision 3
		final Person rev3 = getAuditReader().find( Person.class, personId, 3 );
		assertEquals( "Chris", rev3.getName() );
		assertEquals( "chris at hibernate dot org", rev3.getContactInfo().getEmailAddress() );
		assertEquals( "8885551212", rev3.getContactInfo().getPhoneNumber() );
		// verify revision 4
		final Person rev4 = getAuditReader().find( Person.class, personId, 4 );
		assertEquals( "ChrisC", rev4.getName() );
		assertEquals( "chris at hibernate dot org", rev4.getContactInfo().getEmailAddress() );
		assertEquals( "1-800-555-1212", rev4.getContactInfo().getPhoneNumber() );
	}
}
