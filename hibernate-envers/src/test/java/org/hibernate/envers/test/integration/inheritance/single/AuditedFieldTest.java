package org.hibernate.envers.test.integration.inheritance.single;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.envers.test.BaseEnversJPAFunctionalTestCase;
import org.hibernate.envers.test.Priority;
import org.hibernate.testing.TestForIssue;
import org.junit.Assert;
import org.junit.Test;

@TestForIssue(jiraKey = "HHH-9770")
public class AuditedFieldTest extends BaseEnversJPAFunctionalTestCase {

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] {
			AuditedFieldBaseEntity.class,
			AuditedFieldDerivedEntity.class
		};
	}

	@Test
	@Priority(10)
	public void initData() {
		
		AuditedFieldDerivedEntity derived = new AuditedFieldDerivedEntity();
		derived.setSomeProperty("someValue");
		
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		em.persist(derived);
		em.getTransaction().commit();
		
		derived.setSomeProperty("someValueChanged");
		em.getTransaction().begin();
		em.persist(derived);
		em.getTransaction().commit();
		
	}
	
	@Test
	public void auditFieldDerivedEntityAuditable() {
		Assert.assertTrue(getAuditReader().isEntityClassAudited(AuditedFieldDerivedEntity.class));		
	}
	
	@Test
	public void auditFieldDerivedEntityHasAuditedEntries() {
		
		EntityManager em = getEntityManager();
		AuditedFieldDerivedEntity derived = em
				.createQuery("FROM AuditedFieldDerivedEntity", AuditedFieldDerivedEntity.class)
				.setMaxResults(1)
				.getSingleResult();
		Assert.assertNotNull(derived);
		
		List<Number> revisions = getAuditReader()
				.getRevisions(AuditedFieldDerivedEntity.class, derived.getId());
		Assert.assertFalse(revisions.isEmpty());
		
	}
	
}
