/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.model;



/**
 * @author lgao
 *
 */
public interface Queries {

	String SEQ_NAME = "trackerseq";
	
	/** load all products **/
	String QUERY_LOAD_PRODUCTS_NAME = "products.loadAll";
	String _QUERY_LOAD_PRODUCTS_NAME = "SELECT p FROM Product p";
	
	/** load product by name **/
	String QUERY_LOAD_PRODUCT_BY_NAME = "products.getByName";
	String _QUERY_LOAD_PRODUCT_BY_NAME = "SELECT p FROM Product p WHERE p.name = :name";
	
	/** load product version by name and version **/
	String QUERY_LOAD_PROD_VER_BY_NAME_VER = "products.getPVbyNameAndVer";
	String _QUERY_LOAD_PROD_VER_BY_NAME_VER = "SELECT pv FROM ProductVersion pv WHERE pv.product.name = :name AND pv.version = :version";
	
	/** load components by product name and version **/
	String QUERY_LOAD_COMPS_BY_NAME_VER = "products.getComponentsByNameVer";
	String _QUERY_LOAD_COMPS_BY_NAME_VER = "SELECT p FROM Product p WHERE p.name = :name";
	
	
	/////////////////////////////////////////////////
	/////    Below are Update Queries ///////////////
	////////////////////////////////////////////////
	
}
