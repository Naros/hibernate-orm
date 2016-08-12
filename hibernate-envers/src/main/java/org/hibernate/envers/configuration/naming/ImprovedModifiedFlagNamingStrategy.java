/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.configuration.naming;

import org.hibernate.Incubating;
import org.hibernate.envers.internal.tools.StringTools;

/**
 * An improved naming strategy that prioritizes column naming as follows:
 *
 * <ul>
 *     <li>Explicitly defined {@code modifiedColumnName} on {@link org.hibernate.envers.Audited}.</li>
 *     <li>Column name supplied through {@link javax.persistence.Column} or {@link javax.persistence.JoinColumn}.</li>
 *     <li>Property name.</li>
 * </ul>
 *
 * NOTE: The configured {@link org.hibernate.envers.configuration.EnversSettings#MODIFIED_FLAG_SUFFIX} will be
 * appended to all naming options excluding the explicitly defined {@code modifiedColumnName}.
 * <br/><br/>
 * This strategy is considered experimental and is subject to change.
 *
 * @author Chris Cranford
 */
@Incubating
public class ImprovedModifiedFlagNamingStrategy implements ModifiedFlagNamingStrategy {
	@Override
	public String propertyToModifiedFlagColumnName(
			String modifiedColumnName,
			String propertyName,
			String suffix,
			String columnName) {
		if ( !StringTools.isEmpty( modifiedColumnName ) ) {
			return modifiedColumnName;
		}
		if ( !StringTools.isEmpty( columnName ) ) {
			return columnName + suffix;
		}
		return propertyName + suffix;
	}
}
