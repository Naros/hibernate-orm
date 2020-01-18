/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.test.integration.query;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.criteria.JoinType;

import org.hibernate.envers.Audited;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.test.BaseEnversFunctionalTestCase;
import org.hibernate.envers.test.Priority;
import org.junit.Test;

import org.hibernate.testing.TestForIssue;

import static org.hibernate.testing.junit4.ExtraAssertions.assertTyping;
import static org.hibernate.testing.transaction.TransactionUtil.doInHibernate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Chris Cranford
 */
@TestForIssue(jiraKey = "HHH-13817")
public class AssociationRevisionsOfEntitiesQueryTest extends BaseEnversFunctionalTestCase {
    @Entity(name = "TemplateType")
    @Audited
    public static class TemplateType {
        @Id
        private Integer id;
        private String name;

        TemplateType() {
            this( null, null );
        }

        TemplateType(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Entity(name = "Template")
    @Audited
    public static class Template {
        @Id
        private Integer id;
        private String name;
        @ManyToOne
        private TemplateType templateType;

        Template() {
            this( null, null, null );
        }

        Template(Integer id, String name, TemplateType type) {
            this.id = id;
            this.name = name;
            this.templateType = type;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public TemplateType getTemplateType() {
            return templateType;
        }

        public void setTemplateType(TemplateType templateType) {
            this.templateType = templateType;
        }
    }

    @Override
    public Class<?>[] getAnnotatedClasses() {
        return new Class<?>[] { Template.class, TemplateType.class };
    }

    @Test
    @Priority(10)
    public void initData() {
        doInHibernate( this::sessionFactory, session -> {
            final TemplateType type1 = new TemplateType( 1, "Type1" );
            final TemplateType type2 = new TemplateType( 2, "Type2" );
            session.save( type1 );
            session.save( type2 );

            final Template template = new Template( 1, "Template1", type1 );
            session.save( template );
        } );

        doInHibernate( this::sessionFactory, session -> {
            final TemplateType type = session.find( TemplateType.class, 2 );
            final Template template = session.find( Template.class, 1 );
            template.setName( "Template1-Updated" );
            template.setTemplateType( type );
            session.update( template );
        } );

        doInHibernate( this::sessionFactory, session -> {
            final Template template = session.find( Template.class, 1 );
            session.remove( template );
        } );
    }

    @Test
    public void testRevisionsOfEntityWithAssociationQueries() {
        doInHibernate( this::sessionFactory, session -> {
            List<?> results = getAuditReader().createQuery()
                    .forRevisionsOfEntity( Template.class, true, true )
                    .add( AuditEntity.id().eq( 1 ) )
                    .traverseRelation( "templateType", JoinType.INNER )
                    .add( AuditEntity.property( "name" ).eq( "Type1" ) )
                    .up()
                    .getResultList();
            assertEquals( 1, results.size() );
            assertEquals( "Template1", ( (Template) results.get( 0 ) ).getName() );
        } );

        doInHibernate( this::sessionFactory, session -> {
            List<?> results = getAuditReader().createQuery()
                    .forRevisionsOfEntity( Template.class, true, true )
                    .add( AuditEntity.id().eq( 1 ) )
                    .traverseRelation( "templateType", JoinType.INNER )
                    .add( AuditEntity.property( "name" ).eq( "Type2" ) )
                    .up()
                    .getResultList();

            assertEquals( getGlobalConfiguration().isStoreDataAtDelete() ? 2 : 1, results.size() );
            for ( Object result : results ) {
                assertEquals( "Template1-Updated", ( (Template) result ).getName() );
            }
        } );
    }

    @Test
    public void testAssociationQueriesNotAllowedWhenNotSelectingJustEntities() {
        try {
            doInHibernate( this::sessionFactory, session -> {
                getAuditReader().createQuery()
                        .forRevisionsOfEntity( Template.class, false, true )
                        .add( AuditEntity.id().eq( 1 ) )
                        .traverseRelation( "templateType", JoinType.INNER )
                        .add( AuditEntity.property( "name" ).eq( "Type1" ) )
                        .up()
                        .getResultList();
            } );

            fail( "Test should have thrown IllegalStateException due to selectEntitiesOnly=false" );
        }
        catch ( Exception e ) {
            assertTyping( IllegalStateException.class, e );
        }
    }
}
