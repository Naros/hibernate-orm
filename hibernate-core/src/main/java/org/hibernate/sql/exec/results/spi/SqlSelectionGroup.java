/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

package org.hibernate.sql.exec.results.spi;

import java.util.List;

/**
 * Represents a grouping of SqlSelection references, generally related to a
 * single Navigable
 *
 * @author Steve Ebersole
 */
public interface SqlSelectionGroup {
	List<SqlSelection> getSqlSelections();

}
