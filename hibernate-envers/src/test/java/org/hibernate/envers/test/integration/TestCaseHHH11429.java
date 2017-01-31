/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.test.integration;

import java.util.Arrays;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.transaction.Transaction;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.test.BaseEnversJPAFunctionalTestCase;
import org.hibernate.envers.test.Priority;
import org.junit.Test;

import org.hibernate.testing.TestForIssue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Chris Cranford
 */
@TestForIssue(jiraKey = "HHH-11429")
public class TestCaseHHH11429 extends BaseEnversJPAFunctionalTestCase {
	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] { Appointment.class, Recognition.class };
	}

	@Test
	@Priority(10)
	public void initData() {
		final EntityManager entityManager = getEntityManager();
		final EntityTransaction trx = entityManager.getTransaction();
		try {
			// Revision 1
			trx.begin();
			Appointment appointment = new Appointment( 1, null );
			entityManager.persist( appointment );
			trx.commit();

			// Revision 2
			trx.begin();
			appointment = entityManager.find( Appointment.class, appointment.getId() );
			appointment.setRecognition( new Recognition( 1, null ) );
			appointment.getRecognition().setNursing( new Nursing( false, false ) );
			entityManager.persist( appointment.getRecognition() );
			entityManager.merge( appointment );
			trx.commit();

			// Revision 3
			trx.begin();
			appointment = entityManager.find( Appointment.class, appointment.getId() );
			appointment.getRecognition().getNursing().setDiabetes( true );
			entityManager.merge( appointment );
			trx.commit();
		}
		catch( Throwable t ) {
			if ( trx.isActive() ) {
				trx.rollback();
			}
			throw t;
		}
		finally {
			entityManager.close();
		}
	}

	@Test
	public void testRevisionCounts() {
		assertEquals( Arrays.asList( 1, 2 ), getAuditReader().getRevisions( Appointment.class, 1 ) );
	}

	@Test
	public void testRevisionHistory1() {
		final Appointment rev1 = getAuditReader().find( Appointment.class, 1, 1 );
		assertNull( rev1.getRecognition() );
	}

	@Test
	public void testRevisionHistory2() {
		final Appointment rev2 = getAuditReader().find( Appointment.class, 1, 2 );
		assertEquals( new Recognition( 1, new Nursing( true, false ) ), rev2.getRecognition() );
	}

	@Test
	public void testQuery() {
		final AuditQuery query = getAuditReader().createQuery()
				.forRevisionsOfEntity( Appointment.class, false, false )
				.add( AuditEntity.id().eq( 1 ) );

		for ( Object object : query.getResultList() ) {
			Appointment appointment = Appointment.class.cast( ( (Object[]) object)[0] );
			System.out.println( "--- Appointment ---" );
			if ( appointment.getRecognition() != null ) {
				if ( appointment.getRecognition().getNursing() != null ) {
					final Nursing nursing = appointment.getRecognition().getNursing();
					System.out.println( nursing.isDiabetes() + " " + nursing.isHighBloodPressure() );
				}
				else {
					System.out.println( "No Nursing" );
				}
			}
			else {
				System.out.println( "No Recognition" );
			}
		}
	}

	@Entity(name = "Appointment")
	@Audited
	public static class Appointment {
		@Id
		private Integer id;
		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "recognition_id")
		@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
		private Recognition recognition;

		Appointment() {

		}

		Appointment(Integer id, Recognition recognition) {
			this.id = id;
			this.recognition = recognition;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public Recognition getRecognition() {
			return recognition;
		}

		public void setRecognition(Recognition recognition) {
			this.recognition = recognition;
		}
	}

	@Entity(name = "Recognition")
	public static class Recognition {
		@Id
		private Integer id;
		@Embedded
		private Nursing nursing;

		Recognition() {

		}

		Recognition(Integer id, Nursing nursing) {
			this.id = id;
			this.nursing = nursing;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public Nursing getNursing() {
			return nursing;
		}

		public void setNursing(Nursing nursing) {
			this.nursing = nursing;
		}
	}

	@Embeddable
	public static class Nursing {
		private boolean diabetes;
		private boolean highBloodPressure;

		Nursing() {

		}

		Nursing(boolean diabetes, boolean highBloodPressure) {
			this.diabetes = diabetes;
			this.highBloodPressure = highBloodPressure;
		}

		public boolean isDiabetes() {
			return diabetes;
		}

		public void setDiabetes(boolean diabetes) {
			this.diabetes = diabetes;
		}

		public boolean isHighBloodPressure() {
			return highBloodPressure;
		}

		public void setHighBloodPressure(boolean highBloodPressure) {
			this.highBloodPressure = highBloodPressure;
		}
	}
}
