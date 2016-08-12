/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.configuration.naming;

/**
 * The strategy used to determine the naming of the modified columns.
 *
 * @see DefaultModifiedFlagNamingStrategy
 * @see ImprovedModifiedFlagNamingStrategy
 *
 * @author Chris Cranford
 */
public interface ModifiedFlagNamingStrategy {
	/**
	 * Get the column name to be used for the modified column.
	 *
	 * @param modifiedColumnName The modified column name provided on the {@code Audited} annotation.
	 * @param propertyName The property name.
	 * @param suffix The configured modified flag suffix.
	 * @param columnName The column name detected on {@code Column} or {@code JoinColumn} annotations.
	 *
	 * @return the column name to be used for the modified column.
	 */
	String propertyToModifiedFlagColumnName(
			String modifiedColumnName,
			String propertyName,
			String suffix,
			String columnName);
}
