package org.hibernate.envers.test.integration.inheritance.single;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.envers.Audited;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "discriminator", discriminatorType = DiscriminatorType.INTEGER)
public abstract class AuditedFieldBaseEntity {

	@Id
	@GeneratedValue
	private Integer id;
	
	@Audited
	private String someProperty;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSomeProperty() {
		return someProperty;
	}

	public void setSomeProperty(String someProperty) {
		this.someProperty = someProperty;
	}
	
	public int hashCode() {
		int result;
		result = (id != null ? id.hashCode() : 0);
		result = 31 * result + (someProperty != null ? someProperty.hashCode() : 0);
		return result;
	}
	
	public boolean equals(Object o) {
		
		if(this == o) {
			return true;
		}
		
		if(! (o instanceof AuditedFieldBaseEntity)) {
			return false;
		}
		
		AuditedFieldBaseEntity other = (AuditedFieldBaseEntity) o;
		
		if(id != null ? !id.equals(other.id) : other.id != null) {
			return false;
		}
		
		if(someProperty != null ? !someProperty.equals(other.someProperty) : other.someProperty != null) {
			return false;
		}
		
		return true;
	}
	
}
