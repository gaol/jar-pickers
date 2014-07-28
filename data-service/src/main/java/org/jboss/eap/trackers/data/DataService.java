/**
 * 
 */
package org.jboss.eap.trackers.data;

import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;

import org.jboss.eap.trackers.model.Artifact;
import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;

/**
 * @author lgao
 *
 * This is a facade interface for all data operations 
 */
@Remote
public interface DataService {
	
	String ARTI_STR_REGEX = "^[^\n|^:]+:[^\n|^:]+:[^\n|^:]+[^\n]*";
	
	String COMP_STR_REGEX = "^[^\n|^:]+:[^\n|^:]+[^\n]*";
	
	String RED_HAT_SUFFIX = "-redhat-[^\n].*$";
	
	String DEFAULT_ARTIFACT_TYPE = "jar";

	/**
	 * Loads all Product information including all ProductVersion information.
	 * 
	 */
	List<Product> loadAllProducts() throws DataServiceException;
	
	/**
	 * Create or Update a Product.
	 * 
	 * Client code must provide full information of the product.
	 * 
	 * Versions list won't be affected during create/update.
	 * 
	 */
	List<Product> saveProduct(Product product) throws DataServiceException;
	
	/**
	 * Removes the product from the data store.
	 * 
	 * All versions will be deleted also.
	 * 
	 */
	List<Product> removeProduct(String productName) throws DataServiceException;
	
	/**
	 * Gets Product by Name.
	 */
	public Product getProductByName(String name) throws DataServiceException;
	
	/**
	 * Gets versions list by product name
	 */
	public List<String> getVersions(String productName) throws DataServiceException;
	
	/**
	 * Gets ProductVersion by product name and version
	 */
	public ProductVersion getProductVersion(String productName, String version) throws DataServiceException;
	
	/**
	 * Removes a ProductVersion out of the Product.
	 * 
	 */
	void removeProductVersion(String productName, String version) throws DataServiceException;
	
	/**
	 * Create ProductVersions and associated it to the Product.
	 * 
	 */
	void addProductVersions(String productName, Set<String> versions) throws DataServiceException;
	
	/**
	 * Updates parent information to a specific product version.
	 */
	void updateProductVersionParent(String productName, String version, String parentProductName, String parentPrdVersion) throws DataServiceException;
	
	/**
	 * Loads all artifacts from a product version
	 */
	List<Artifact> loadArtifacts(String productName, String version) throws DataServiceException;
	
	/**
	 * Adds an Artifact to a product version
	 */
	void addArtifact(String productName, String version, String groupId, String artifactId, String artiVersion) throws DataServiceException;
	
	/**
	 * Updates build information to the artifact.
	 * 
	 * @param groupId can't be null
	 * @param artifactId if it is null, then update all artifacts of under groupId. 
	 * @param artiVersion can't be null. a build information can only apply for one version
	 * @param buildInfo which build produces this artifact.
	 * @throws DataServiceException
	 */
	void updateArtifactBuildInfo(String groupId, String artifactId, String artiVersion, String buildInfo) throws DataServiceException;
	
	/**
	 * Updates component information of an Artifact
	 * 
	 * @param groupId can't be null
	 * @param artifactId if null, all artifacts in that groupId:version will relate to the proposed Component
	 * @param artiVersion can't be null
	 * @param compName can't be null
	 * @param compVer can't be null
	 * @throws DataServiceException
	 */
	void updateArtifactComponent(String groupId, String artifactId, String artiVersion, String compName, String compVer) throws DataServiceException;
	
	/**
	 * Removes Artifacts from product version.
	 * 
	 * @param productName the product name
	 * @param version which version the product is
	 * @param groupId the groupId of the artifact to be deleted. Not-Null
	 * @param artifactId the artifactId of the artifact to be deleted, if null, all artifacts in groupId will be deleted.
	 * @param artiVersion the version of the artifacts, Not-null
	 * @throws DataServiceException
	 */
	void removeArtifacts(String productName, String version, String groupId, String artifactId, String artiVersion) throws DataServiceException;
	
	/**
	 * Imports artifacts list from a stream to a product version 
	 */
	void importArtifacts(String productName, String version, URL artifactListURL) throws DataServiceException;
	
	
	/**
	 * Imports artifacts list from a composed string list to a product version 
	 */
	void importArtifacts(String productName, String version, List<String> artis) throws DataServiceException;
	
	/**
	 * Gets the Artifact by groupId, artifactId and version.
	 */
	Artifact getArtifact(String groupId, String artifactId, String version) throws DataServiceException;
	
	/**
	 * Loads native components of a specific product version
	 * 
	 */
	List<Component> loadNativeComponents(String productName, String version) throws DataServiceException;
	
	/**
	 * Add a native component to a specific product version
	 * 
	 */
	void addNativeComponent(String productName, String version, String compName, String compVer) throws DataServiceException;
	
	/**
	 * Removes a native component from a specific product version
	 */
	void removeNativeComponent(String productName, String version, String compName, String compVer) throws DataServiceException;
	
	/**
	 * Imports native components to a specific product version from an URL
	 */
	void importNativeComponents(String productName, String version, URL compListURL) throws DataServiceException;
	
	/**
	 * Imports native components to a specific product version by specifying the component list
	 * 
	 * Format of the component list:
	 *  1). Each component matches in each line
	 *  2). For each line: ComponentName:ComponentVersion
	 */
	void importNativeComponents(String productName, String version, List<String> componentList) throws DataServiceException;
	
	/**
	 * Imports components by an URL
	 */
	void importComponents(URL compListURL) throws DataServiceException;
	
	/**
	 * Imports components by a components list
	 * 
	 * Format of the component list:
	 *  1). Each component matches in each line
	 *  2). For each line: ComponentName:ComponentVersion
	 */
	void importComponents(List<String> componentList) throws DataServiceException;
	
	/**
	 * Gets Component by name and version
	 */
	Component getComponent(String name, String version) throws DataServiceException;
	
	/**
	 * Guess the Component of the Artifact.
	 */
	Component guessComponent(String groupId, String artifactId,
			String artiVersion) throws DataServiceException;
	
	/**
	 * Create/Update Component
	 */
	void saveComponent(Component comp) throws DataServiceException;
	
	/**
	 * Updates note field if any.
	 * 
	 * @param id the identifier
	 * @param type can be: <b>ProductVersion</b> | <b>Artifact</b>
	 * @param note the note information to be updated
	 * @throws DataServiceException if type is not ProductVersion | Artifact
	 */
	void updateNote(Long id, String type, String note) throws DataServiceException;
}
