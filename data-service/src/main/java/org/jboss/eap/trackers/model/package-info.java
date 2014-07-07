/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
@NamedQueries({
	@NamedQuery(name = Queries.QUERY_LOAD_PRODUCTS_NAME, query = Queries._QUERY_LOAD_PRODUCTS_NAME),
	@NamedQuery(name = Queries.QUERY_LOAD_PRODUCT_BY_NAME, query = Queries._QUERY_LOAD_PRODUCT_BY_NAME),
	@NamedQuery(name = Queries.QUERY_LOAD_COMPS_BY_NAME_VER, query = Queries._QUERY_LOAD_COMPS_BY_NAME_VER),
	@NamedQuery(name = Queries.QUERY_LOAD_PROD_VER_BY_NAME_VER, query = Queries._QUERY_LOAD_PROD_VER_BY_NAME_VER),
	
	})

package org.jboss.eap.trackers.model;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
