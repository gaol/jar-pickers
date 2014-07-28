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
	
	
	/** load artifacts by groupid, artifactid and version **/
	String QUERY_LOAD_ARTIFACTS = "artifacts.loadArtifacts";
	String _QUERY_LOAD_ARTIFACTS = "SELECT a FROM Artifact a"
			+ " WHERE a.groupId = :groupId"
			+ " AND a.artifactId = :artifactId"
			+ " AND a.version = :version";
	
	/** load components by name and version **/
	String QUERY_LOAD_COMP_BY_NAME_AND_VER = "components.loadCompsByNameAndVer";
	String _QUERY_LOAD_COMP_BY_NAME_AND_VER = "SELECT c FROM Component c WHERE c.name = :name AND c.version = :version";
	
	/** load components by artifacts information **/
	String QUERY_LOAD_COMP_BY_ARTIFACT = "components.loadCompsByArtifactInfo";
	String _QUERY_LOAD_COMP_BY_ARTIFACT = "SELECT c FROM Component c WHERE c.name = :name AND c.version = :version";
	
	/** load artifacts by product name and version **/
	String QUERY_LOAD_ARTIFACTS_BY_PV = "artifacts.loadArtifactsByPV";
	String _QUERY_LOAD_ARTIFACTS_BY_PV = "SELECT a FROM Artifact a"
			+ " INNER JOIN a.pvs pv"
			+ " INNER JOIN pv.product p"
			+ " WHERE p.name = :name"
			+ " AND pv.version = :version";
	
	/** load native components by product name and version **/
	String QUERY_LOAD_COMPS_BY_PV = "components.loadComponentsByPV";
	String _QUERY_LOAD_COMPS_BY_PV = "SELECT c FROM Component c"
			+ " INNER JOIN c.pvs pv"
			+ " INNER JOIN pv.product p"
			+ " WHERE p.name = :name"
			+ " AND pv.version = :version";
	
	/** load components by groupId if any **/
	String QUERY_LOAD_COMPS_BY_GROUPID = "components.loadComponentsByGroupId";
	String _QUERY_LOAD_COMPS_BY_GROUPID = "SELECT c FROM Component c"
			+ " WHERE c.groupId = :groupId"
			+ " AND c.version like :version";
	
}
