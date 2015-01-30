/**
 * 
 */
package org.jboss.eap.trackers.data;

import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Pattern;

import org.jboss.eap.trackers.model.ArtifactCVEs;
import org.jboss.eap.trackers.model.Artifact;
import org.jboss.eap.trackers.model.CVE;
import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;

/**
 * @author lgao
 *
 * This is a facade interface for all data operations 
 */
public interface DataService extends Constants
{

   String ARTI_STR_REGEX = "^[^\n|^:]+:[^\n|^:]+:[^\n|^:]+[^\n]*";

   String COMP_STR_REGEX = "^[^\n|^:]+:[^\n|^:]+[^\n]*";

   String RED_HAT_SUFFIX = "-redhat-[^\n].*$";

   String DEFAULT_ARTIFACT_TYPE = "jar";

   Pattern CVE_NAME_PATTERN = Pattern.compile("CVE-([0-9]{4})-([0-9]{4})");

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

   
   /** Save or update the entity using JPA service **/
   <T extends Serializable> void saveEntity(T t);

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
   void updateProductVersionParent(String productName, String version, String parentProductName, String parentPrdVersion)
         throws DataServiceException;

   /**
    * Loads all artifacts from a product version
    */
   List<Artifact> loadArtifacts(String productName, String version) throws DataServiceException;

   /**
    * Adds an Artifact
    */
   void saveArtifact(Artifact artifact) throws DataServiceException;

   /**
    * Adds an Artifact to a product version
    */
   void addArtifact(String productName, String version, String groupId, String artifactId, String artiVersion)
         throws DataServiceException;

   /**
    * Updates build information to the artifact.
    * 
    * @param groupId can't be null
    * @param artifactId if it is null, then update all artifacts of under groupId. 
    * @param artiVersion can't be null. a build information can only apply for one version
    * @param buildInfo which build produces this artifact.
    * @throws DataServiceException
    */
   void updateArtifactBuildInfo(String groupId, String artifactId, String artiVersion, String buildInfo)
         throws DataServiceException;

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
   void updateArtifactComponent(String groupId, String artifactId, String artiVersion, String compName, String compVer)
         throws DataServiceException;

   /**
    * Updates MD5 checksum of the artifact.
    * 
    * @param groupId the groupId
    * @param artifactId the artifactId
    * @param artiVersion the version of the artifact
    * @param checksum md5 checksum
    * @throws DataServiceException
    */
   void updateArtifactChecksum(String groupId, String artifactId, String artiVersion, String checksum)
         throws DataServiceException;

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
   void removeArtifacts(String productName, String version, String groupId, String artifactId, String artiVersion)
         throws DataServiceException;

   /**
    * Imports artifacts list from a stream to a product version 
    */
   void importArtifacts(String productName, String version, URL artifactListURL) throws DataServiceException;

   /**
    * Imports artifacts list from a composed string list to a product version 
    */
   void importArtifacts(String productName, String version, List<String> artis) throws DataServiceException;

   /**
    * Imports artifacts from a URL
    */
   void importArtifacts(URL artifactListURL) throws DataServiceException;

   /**
    * Gets the Artifact by groupId, artifactId and version.
    */
   Artifact getArtifact(String groupId, String artifactId, String version) throws DataServiceException;

   /**
    * Loads components of a specific product version
    * 
    */
   List<Component> loadComponents(String productName, String version) throws DataServiceException;

   /**
    * Loads native components of a specific product version
    * 
    */
   List<Component> loadNativeComponents(String productName, String version) throws DataServiceException;

   /**
    * Removes a native component from a specific product version
    */
   void removeComponent(String productName, String version, String compName, String compVer)
         throws DataServiceException;

   /**
    * Imports native components to a specific product version from an URL
    */
   void importComponents(String productName, String version, URL compListURL) throws DataServiceException;

   /**
    * Imports native components to a specific product version by specifying the component list
    * 
    * Format of the component list:
    *  1). Each component matches in each line
    *  2). For each line: ComponentName:ComponentVersion[:[groupId][:true|false]]
    */
   void importComponentsFromList(String productName, String version, List<String> componentList)
         throws DataServiceException;

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
   void importComponentsFromList(List<String> componentList) throws DataServiceException;

   /**
    * Gets Component by name and version
    */
   Component getComponent(String name, String version) throws DataServiceException;

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

   /**
    * Gets the CVE according to the cve name.
    * 
    * @param cveName the cve name
    * @return the CVE entity
    * @throws DataServiceException the exception
    */
   CVE getCVE(String cveName) throws DataServiceException;

   List<ArtifactCVEs> getAffectedArtis(String grpId, String artiId) throws DataServiceException;

   /**
    * Gets affected product versions according to CVE Name
    * 
    * @param cveName the cve name
    * @return the set of affected ProductVersion
    * @throws DataServiceException exception
    */
   Set<ProductVersion> affectedProducts(String cveName) throws DataServiceException;

   /**
    * Gets affected artifacts according to CVE name
    * 
    * @param cveName the cve name
    * @return the set of affected Artifacts
    * @throws DataServiceException exception
    */
   Set<Artifact> affectedArtifacts(String cveName) throws DataServiceException;

   /**
    * Gets affected native components according to CVE name
    * 
    * @param cveName the CVE name
    * @return the set of affected native components
    * @throws DataServiceException exception
    */
   Set<Component> affectedCompoents(String cveName) throws DataServiceException;

   /**
    * Gets a sorted set of CVEs according to product name and version.
    * 
    * @param prdName the product name
    * @param prdVersion the product version
    * @return the sorted set of CVEs it has
    * @throws DataServiceException the exception
    */
   SortedSet<CVE> productCVEs(String prdName, String prdVersion) throws DataServiceException;

   /**
    * Gets a sorted set of CVEs according to the artifact's groupId, artifactId and version
    * 
    * @param groupId the groupId of the artifact
    * @param artifactId the artifactId of the artifact
    * @param version the version of the artifact
    * @return a sorted set of CVEs it has
    * @throws DataServiceException the exception
    */
   SortedSet<CVE> artifactCVEs(String groupId, String artifactId, String version) throws DataServiceException;

   /**
    * Gets a sorted set of CVEs according to the local component name and version
    * 
    * @param compName local component name
    * @param version version of the component
    * @return a sorted set of CVEs it has
    * @throws DataServiceException the exception
    */
   SortedSet<CVE> componentCVEs(String compName, String version) throws DataServiceException;

   /**
    * Track new CVE into the system. Nothing happens if it is tracked already.
    * 
    * @param cveName the CVE name, must follow the CVE name format: CVE-XXXX-YYYY, where X and Y are all numbers.
    * @return CVE entity after tracking.
    * @throws IllegalArgumentException if the @cveName does not follow the format.
    * @throws DataServiceException the exception
    */
   CVE newCVE(String cveName) throws IllegalArgumentException, DataServiceException;

   /**
    * Updates CVE, including the affected artifacts.
    * 
    * The associated AffectedArtifact must be provided to avoid data missing.
    * 
    * @param cve the CVE
    * @return the CVE after modification
    * @throws DataServiceException the exception
    */
   CVE updateCVE(CVE cve) throws DataServiceException;

   /**
    * Adds new CVE affected artifacts.
    * 
    * @param cveName the CVE name, must follow CVE name format.
    * @param groupId groupId of the Artifact
    * @param artiId artifactId of the Artifact
    * @return the CVE entity
    * @param versionScope version scope of the Artifact
    * @throws IllegalArgumentException
    * @throws DataServiceException
    */
   CVE cveAffected(String cveName, String groupId, String artiId, String versionScope) throws IllegalArgumentException,
         DataServiceException;

}
