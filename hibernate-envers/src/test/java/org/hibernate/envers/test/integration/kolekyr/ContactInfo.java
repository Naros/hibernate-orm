/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.test.integration.kolekyr;

import javax.persistence.Embeddable;

/**
 * @author Chris Cranford
 */
@Embeddable
public class ContactInfo {
	private String emailAddress;
	private String phoneNumber;

	ContactInfo() {

	}

	public ContactInfo(String emailAddress, String phoneNumber) {
		this.emailAddress = emailAddress;
		this.phoneNumber = phoneNumber;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
}
