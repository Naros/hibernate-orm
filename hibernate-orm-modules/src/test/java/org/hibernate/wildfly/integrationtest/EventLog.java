/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.wildfly.integrationtest;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * @author Chris Cranford
 */
@Entity
public class EventLog {
	private Long id;
	private String message;

	@Id
	@GeneratedValue(generator = "eventLogIdGenerator")
	@GenericGenerator(name = "eventLogIdGenerator", strategy = "org.hibernate.id.enhanced.TableGenerator", parameters = { @Parameter(name = "table_name", value = "primaryKeyPools"),
			@Parameter(name = "segment_value", value = "eventLog"), @Parameter(name = "optimizer", value = "pooled"), @Parameter(name = "increment_size", value = "500"),
			@Parameter(name = "initial_value", value = "1") })
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
