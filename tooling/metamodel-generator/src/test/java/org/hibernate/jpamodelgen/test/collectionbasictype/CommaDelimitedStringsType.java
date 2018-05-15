/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.jpamodelgen.test.collectionbasictype;

import java.util.List;

import org.hibernate.type.internal.BasicTypeImpl;
import org.hibernate.type.descriptor.sql.spi.VarcharSqlDescriptor;

/**
 * @author Vlad Mihalcea
 */
public class CommaDelimitedStringsType extends BasicTypeImpl<List> {

    public CommaDelimitedStringsType() {
        super(
                new CommaDelimitedStringsJavaTypeDescriptor(),
                VarcharSqlDescriptor.INSTANCE
        );
    }
}