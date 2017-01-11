/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.test.integration.tools;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.Session;
import org.hibernate.envers.Audited;
import org.hibernate.envers.test.BaseEnversFunctionalTestCase;
import org.hibernate.envers.test.Priority;
import org.hibernate.testing.TestForIssue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Chris Cranford
 */
@TestForIssue(jiraKey = "HHH-4803")
public class ProxyTargetDuplicateTest extends BaseEnversFunctionalTestCase {
	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] { EntityX.class, EntityY.class, EntityZ.class };
	}

	@Test
	@Priority(10)
	public void initData() {
		final Session session1 = openSession();
		final Session session2 = openSession();
		try {
			// Session 1
			final EntityZ z = new EntityZ( 1 );
			final EntityY y = new EntityY( 1 );
			final EntityX x = new EntityX( 1 );

			y.link( z );
			x.link( y );

			session1.getTransaction().begin();
			session1.save( z );
			session1.save( y );
			session1.save( x );
			session1.getTransaction().commit();

			// Session 2
			session2.getTransaction().begin();
			final EntityX x2 = session2.find( EntityX.class, 1 );
			final EntityY yProxy = x2.getY();
			final EntityZ z2 = session2.find( EntityZ.class, 1 );
			z2.setName( "changed" );
			z2.link( x2 );

			session2.update( x2 );
			session2.getTransaction().commit();

			// z != yProxy.getZ()
			// !z.equals( yProxy.getZ() )

			// assertEquals( z, yProxy.getZ() );
		}
		catch ( Exception e ) {
			if ( session2.getTransaction().isActive() ) {
				session2.getTransaction().rollback();
			}
			if ( session1.getTransaction().isActive() ) {
				session1.getTransaction().rollback();
			}
			throw e;
		}
		finally {
			session2.close();
			session1.close();
		}
	}

	@Entity(name = "EntityX")
	@Audited
	public static class EntityX {
		@Id
		private Integer id;

		@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		private EntityY y;

		@ManyToOne(cascade = CascadeType.ALL)
		private EntityZ z;

		public EntityX() {

		}

		public EntityX(Integer id) {
			this.id = id;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public EntityY getY() {
			return y;
		}

		public void setY(EntityY y) {
			this.y = y;
		}

		public EntityZ getZ() {
			return z;
		}

		public void setZ(EntityZ z) {
			this.z = z;
		}

		public void link(EntityY y) {
			y.link( this );
		}

		public void link(EntityZ z) {
			z.link( this );
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			EntityX entityX = (EntityX) o;

			if ( id != null ? !id.equals( entityX.id ) : entityX.id != null ) {
				return false;
			}
			if ( y != null ? !y.equals( entityX.y ) : entityX.y != null ) {
				return false;
			}
			return z != null ? z.equals( entityX.z ) : entityX.z == null;
		}

		@Override
		public int hashCode() {
			int result = id != null ? id.hashCode() : 0;
			result = 31 * result + ( y != null ? y.hashCode() : 0 );
			result = 31 * result + ( z != null ? z.hashCode() : 0 );
			return result;
		}

		@Override
		public String toString() {
			return "EntityX{" +
					"id=" + id +
					'}';
		}
	}

	@Entity(name = "EntityY")
	@Audited
	public static class EntityY {
		@Id
		private Integer id;

		@OneToMany(mappedBy = "y")
		private List<EntityX> xList = new ArrayList<EntityX>();

		@ManyToOne(cascade = CascadeType.ALL)
		private EntityZ z;

		public EntityY() {

		}

		public EntityY(Integer id) {
			this.id = id;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public EntityZ getZ() {
			return z;
		}

		public void setZ(EntityZ z) {
			this.z = z;
		}

		public void link(EntityX x) {
			this.xList.add( x );
			x.setY( this );
		}

		public void link(EntityZ z) {
			z.link( this );
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			EntityY entityY = (EntityY) o;

			if ( id != null ? !id.equals( entityY.id ) : entityY.id != null ) {
				return false;
			}
			if ( xList != null ? !xList.equals( entityY.xList ) : entityY.xList != null ) {
				return false;
			}
			return z != null ? z.equals( entityY.z ) : entityY.z == null;
		}

		@Override
		public int hashCode() {
			int result = id != null ? id.hashCode() : 0;
			result = 31 * result + ( xList != null ? xList.hashCode() : 0 );
			result = 31 * result + ( z != null ? z.hashCode() : 0 );
			return result;
		}

		@Override
		public String toString() {
			return "EntityY{" +
					"id=" + id +
					", xList=" + xList +
					'}';
		}
	}

	@Entity(name = "EntityZ")
	@Audited
	public static class EntityZ {
		@Id
		private Integer id;
		private String name;

		@OneToMany(mappedBy = "z")
		private List<EntityX> xList = new ArrayList<EntityX>();

		@OneToMany(mappedBy = "z")
		private List<EntityY> yList = new ArrayList<EntityY>();

		public EntityZ() {

		}

		public EntityZ(Integer id) {
			this.id = id;
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

		public List<EntityX> getxList() {
			return xList;
		}

		public void setxList(List<EntityX> xList) {
			this.xList = xList;
		}

		public List<EntityY> getyList() {
			return yList;
		}

		public void setyList(List<EntityY> yList) {
			this.yList = yList;
		}

		public void link(EntityY y) {
			getyList().add( y );
			y.setZ( this );
		}

		public void link(EntityX x) {
			getxList().add( x );
			x.setZ( this );
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			EntityZ entityZ = (EntityZ) o;

			if ( id != null ? !id.equals( entityZ.id ) : entityZ.id != null ) {
				return false;
			}
			if ( name != null ? !name.equals( entityZ.name ) : entityZ.name != null ) {
				return false;
			}
			if ( xList != null ? !xList.equals( entityZ.xList ) : entityZ.xList != null ) {
				return false;
			}
			return yList != null ? yList.equals( entityZ.yList ) : entityZ.yList == null;
		}

		@Override
		public int hashCode() {
			int result = id != null ? id.hashCode() : 0;
			result = 31 * result + ( name != null ? name.hashCode() : 0 );
			result = 31 * result + ( xList != null ? xList.hashCode() : 0 );
			result = 31 * result + ( yList != null ? yList.hashCode() : 0 );
			return result;
		}

		@Override
		public String toString() {
			return "EntityZ{" +
					"id=" + id +
					", name='" + name + '\'' +
					", xList=" + xList +
					", yList=" + yList +
					'}';
		}
	}
}
