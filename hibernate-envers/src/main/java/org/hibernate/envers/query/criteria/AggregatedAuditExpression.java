/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.query.criteria;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.envers.internal.tools.query.Parameters;
import org.hibernate.envers.internal.tools.query.QueryBuilder;
import org.hibernate.envers.query.criteria.internal.AbstractExpression;
import org.hibernate.envers.query.internal.property.PropertyNameGetter;

/**
 * @author Adam Warski (adam at warski dot org)
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 * @author Chris Cranford
 */
public class AggregatedAuditExpression extends AbstractExpression implements ExtendableCriterion {
	private String alias;
	private PropertyNameGetter propertyNameGetter;
	private AggregatedMode mode;
	// Correlate subquery with outer query by entity id.
	private boolean correlate;
	private List<AuditCriterion> criterions;

	public AggregatedAuditExpression(String alias, PropertyNameGetter propertyNameGetter, AggregatedMode mode) {
		this.alias = alias;
		this.propertyNameGetter = propertyNameGetter;
		this.mode = mode;
		criterions = new ArrayList<>();
	}

	public enum AggregatedMode {
		MAX,
		MIN
	}

	@Override
	public AggregatedAuditExpression add(AuditCriterion criterion) {
		criterions.add( criterion );
		return this;
	}

	@Override
	public void addToQuery(String baseAlias, QueryBuilder qb, Parameters parameters) {
		String effectiveAlias = this.alias == null ? baseAlias : this.alias;
		String entityName = qb.getEntityNameByAlias( effectiveAlias );
		String propertyName = qb.resolvePropertyName( entityName, propertyNameGetter );
		String componentPrefix = qb.resolveComponentPropertyPrefix( effectiveAlias );
		String prefixedPropertyName = componentPrefix.concat( propertyName );

		checkPropertyNotRelation( qb.getEnversService(), entityName, prefixedPropertyName );

		// Make sure our conditions are ANDed together even if the parent Parameters have a different connective
		Parameters subParams = parameters.addSubParameters( Parameters.AND );
		// This will be the aggregated query, containing all the specified conditions
		String auditEntityName = qb.getEnversService().getConfig().getAuditEntityName( entityName );
		String subQueryAlias = qb.generateAlias();
		QueryBuilder subQb = qb.newSubQueryBuilder( auditEntityName, subQueryAlias );
		qb.registerEntityAlias( entityName, subQueryAlias );

		// Adding all specified conditions both to the main query, as well as to the
		// aggregated one.
		for ( AuditCriterion versionsCriteria : criterions ) {
			versionsCriteria.addToQuery( effectiveAlias, qb, subParams );
			versionsCriteria.addToQuery( subQueryAlias, subQb, subQb.getRootParameters() );
		}

		// Setting the desired projection of the aggregated query
		switch ( mode ) {
			case MIN:
				subQb.addProjection( "min", subQb.getAlias(), prefixedPropertyName, false );
				break;
			case MAX:
				subQb.addProjection( "max", subQb.getAlias(), prefixedPropertyName, false );
		}

		// Correlating subquery with the outer query by entity id. See JIRA HHH-7827.
		if ( correlate ) {
			final String originalIdPropertyName = qb.getEnversService().getConfig().getOriginalIdPropertyName();
			qb.getEnversService().getEntitiesConfigurations().get( entityName ).getIdMapper().addIdsEqualToQuery(
					subQb.getRootParameters(),
					subQb.getRootAlias() + "." + originalIdPropertyName,
					effectiveAlias + "." + originalIdPropertyName
			);
		}

		// Adding the constrain on the result of the aggregated criteria
		subParams.addWhere( effectiveAlias, prefixedPropertyName, "=", subQb );
	}

	/**
	 * Compute aggregated expression in the context of each entity instance separately. Useful for retrieving latest
	 * revisions of all entities of a particular type.<br/>
	 * Implementation note: Correlates subquery with the outer query by entity id.
	 *
	 * @return this (for method chaining).
	 */
	public AggregatedAuditExpression computeAggregationInInstanceContext() {
		correlate = true;
		return this;
	}
}
