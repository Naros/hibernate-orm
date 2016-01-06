package org.hibernate.envers.test.integration.inheritance.single;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "1")
public class AuditedFieldDerivedEntity extends AuditedFieldBaseEntity {
	
}
