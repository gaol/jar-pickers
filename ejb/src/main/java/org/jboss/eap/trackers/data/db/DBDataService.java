/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.data.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.Session;
import org.jboss.eap.trackers.data.DataService;
import org.jboss.eap.trackers.data.DataServiceException;
import org.jboss.eap.trackers.data.VersionScopes;
import org.jboss.eap.trackers.model.AffectedArtifact;
import org.jboss.eap.trackers.model.Artifact;
import org.jboss.eap.trackers.model.CVE;
import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;
import org.jboss.eap.trackers.model.Queries;
import org.jboss.eap.trackers.utils.ArtifactsUtil;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.jboss.logging.Logger;

/**
 * @author lgao
 *
 */
@Stateless
@PermitAll
@Local(DataServiceLocal.class)
@Remote(DataService.class)
@SecurityDomain("other")
public class DBDataService implements DataServiceLocal {

	@Inject
	private Logger logger;
	
	@Inject
	private EntityManager em;
	
	@Override
	public List<Product> loadAllProducts() throws DataServiceException {
		return em.createNamedQuery(Queries.QUERY_LOAD_PRODUCTS_NAME, Product.class).getResultList();
	}

	@RolesAllowed("tracker")
	@Override
	public List<Product> saveProduct(Product product)
			throws DataServiceException {
		Session session = (Session)em.getDelegate();
		session.saveOrUpdate(product);
		return loadAllProducts();
	}

	@RolesAllowed("tracker")
	@Override
	public List<Product> removeProduct(String productName)
			throws DataServiceException {
		Product prod = getProductByName(productName);
		if (prod != null) {
			em.remove(prod);
		}
		return loadAllProducts();
	}

	@Override
	@RolesAllowed("tracker")
	public void removeProductVersion(String productName, String version)
			throws DataServiceException {
		ProductVersion pv = getProductVersion(productName, version);
		if (pv == null) {
			throw new DataServiceException("Unkown product version: " + productName + ":" + version);
		}
		this.em.remove(pv);
	}
	
	@Override
	public ProductVersion getProductVersion(String productName, String version) throws DataServiceException {
		if (productName == null || version == null) {
			throw new IllegalArgumentException("Both productName and version can't be null.");
		}
		List<ProductVersion> pvs = em.createNamedQuery(Queries.QUERY_LOAD_PROD_VER_BY_NAME_VER, 
				ProductVersion.class)
				.setParameter("name", productName)
				.setParameter("version", version)
				.getResultList();
		return pvs.size() > 0 ? pvs.get(0) : null;
	}
	
	@Override
	@RolesAllowed("tracker")
	public void updateProductVersionParent(String productName, String version,
			String parentProductName, String parentPrdVersion)
			throws DataServiceException {
		ProductVersion pv = getProductVersion(productName, version);
		if (pv == null)
			throw new DataServiceException("There is no ProductVersion with productName: " + productName + ", Version: " + version);
		ProductVersion parentPV = getProductVersion(parentProductName, parentPrdVersion);
		if (parentPV == null)
			throw new DataServiceException("There is no ProductVersion with productName: " + parentProductName + ", Version: " + parentPrdVersion);
		pv.setParent(parentPV);
		this.em.merge(pv);
	}
	
	@Override
	public Product getProductByName(String name) throws DataServiceException {
		if (name == null) {
			throw new IllegalArgumentException("Product Name should be provided.");
		}
		List<Product> prodList = this.em.createNamedQuery(Queries.QUERY_LOAD_PRODUCT_BY_NAME, Product.class)
				.setParameter("name", name)
				.getResultList();
		return prodList.size() > 0 ? prodList.get(0) : null;
	}
	
	@Override
	public List<String> getVersions(String productName)
			throws DataServiceException {
		Product prod = getProductByName(productName);
		if (prod == null) {
			throw new DataServiceException("No product found with name: " + productName);
		}
		List<ProductVersion> pvs = prod.getVersions();
		List<String> versions = new ArrayList<String>();
		if (pvs != null) {
			for (ProductVersion pv: pvs) {
				versions.add(pv.getVersion());
			}
		}
		return versions;
	}

	@RolesAllowed("tracker")
	@Override
	public void addProductVersions(String productName, Set<String> versions) throws DataServiceException {
		Product prod = getProductByName(productName);
		if (prod == null) {
			throw new DataServiceException("Unknown Product name: " + productName);
		}
		if (versions == null || versions.size() == 0) {
			throw new IllegalArgumentException("Versions can't be empty.");
		}
		List<ProductVersion> pvs = prod.getVersions();
		if (pvs == null) {
			pvs = new ArrayList<ProductVersion>();
		}
		for (String version: versions) {
			ProductVersion pv = new ProductVersion();
			pv.setProduct(prod);
			pv.setVersion(version);
			if (pvs.contains(pv)) {
				throw new DataServiceException("Version: " + version + " has already in product: " + productName);
			}
			this.em.persist(pv);
			pvs.add(pv);
		}
		prod.setVersions(pvs);
		this.em.persist(prod);
	}

	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#loadArtifacts(java.lang.String, java.lang.String)
	 */
	@Override
	public List<Artifact> loadArtifacts(String productName, String version)
			throws DataServiceException {
		if (productName == null || version == null) {
			throw new IllegalArgumentException("Both productName and version can't be null.");
		}
		return this.em.createNamedQuery(Queries.QUERY_LOAD_ARTIFACTS_BY_PV, Artifact.class)
				.setParameter("name", productName)
				.setParameter("version", version)
				.getResultList();
	}

	@Override
    @RolesAllowed("tracker")
    public void addArtifact(Artifact artifact) throws DataServiceException {
	    if (artifact == null) {
	        throw new IllegalArgumentException("Artifact can't be null.");
	    }
	    Session session = this.em.unwrap(Session.class);
	    session.saveOrUpdate(artifact);
    }
	
	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#addArtifact(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	@RolesAllowed("tracker")
	public void addArtifact(String productName, String version, String groupId, String artifactId, 
			String artiVersion) throws DataServiceException {
		addArtifact(productName, version, groupId, artifactId, artiVersion, DEFAULT_ARTIFACT_TYPE, null);
	}
	
	public void addArtifact(String productName, String version, String groupId, String artifactId, 
			String artiVersion, String type, String checksum) throws DataServiceException {
		ProductVersion pv = getProductVersion(productName, version);
		if (pv == null) {
			throw new DataServiceException("No ProductVersion found: " + productName + ":" + version);
		}
		List<Artifact> artifacts = pv.getArtifacts();
		if (artifacts == null) {
			artifacts = new ArrayList<Artifact>();
			pv.setArtifacts(artifacts);
		}
		Artifact arti = getArtifact(groupId, artifactId, artiVersion);
		if (arti != null) {
			logger.debug("Artifact: " + arti.toString() + " has been added already. Associate it to " + 
					productName + ":" + version); 
		}
		else {
			arti = new Artifact();
			arti.setArtifactId(artifactId);
			arti.setGroupId(groupId);
			arti.setVersion(artiVersion);
			arti.setType(type == null ? DEFAULT_ARTIFACT_TYPE : type);
			arti.setChecksum(checksum);
			Component component = guessComponent(groupId, null, artiVersion);
			if (component == null) { // create one if does not found.
			    component = new Component();
			    component.setGroupId(groupId);
			    component.setVersion(artiVersion);
			    component.setName(ArtifactsUtil.guessComponentNameFromAritifact(groupId, artifactId, artiVersion));
			    this.em.persist(component);
			}
			arti.setComponent(component);
			this.em.persist(arti);
		}
		if (artifacts.contains(arti)) {
			throw new DataServiceException("Artifact: " + arti.toString() + " has been associated with the Product Version: " + pv.toString());
		}
		artifacts.add(arti);
		this.em.merge(pv);
		List<ProductVersion> artiVers = arti.getPvs();
		if (artiVers == null) {
			artiVers = new ArrayList<ProductVersion>();
			arti.setPvs(artiVers);
		}
		if (artiVers.contains(pv)) {
			throw new DataServiceException("Wrong state");
		}
		artiVers.add(pv);
		this.em.merge(arti);
	}
	
	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#getArtifact(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Artifact getArtifact(String groupId, String artifactId, String version) throws DataServiceException {
		if (groupId == null || artifactId == null || version == null ) {
			throw new IllegalArgumentException("groupId, artifactId and version can't be null.");
		}
		List<Artifact> artis = em.createNamedQuery(Queries.QUERY_LOAD_ARTIFACTS, Artifact.class)
				.setParameter("groupId", groupId)
				.setParameter("artifactId", artifactId)
				.setParameter("version", version)
				.getResultList();
		return artis.size() > 0 ? artis.get(0) : null;
	}

	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#guessComponent(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Component guessComponent(String groupId, String artifactId, String artiVersion) throws DataServiceException {
		if (groupId == null || artiVersion == null) {
			throw new IllegalArgumentException("groupId and version of the Artifact can't be null.");
		}
		String HQL = "SELECT a.component FROM Artifact a WHERE a.groupId = :groupId AND a.version = :version";
		if (artifactId != null && artifactId.length() > 0) {
			HQL = HQL + " AND a.artifactId = :artifactId";
		}
		TypedQuery<Component> query = this.em.createQuery(HQL,Component.class).setParameter("groupId", groupId)
				.setParameter("version", artiVersion);
		if (artifactId != null && artifactId.length() > 0) {
			query.setParameter("artifactId", artifactId);
		}
		List<Component> comps = query.getResultList();
		Component comp = comps.size() > 0 ? comps.get(0) : null;
		if (comp != null) {
			return comp;
		}
		// guess by groupId and version
		comps = this.em.createNamedQuery(Queries.QUERY_LOAD_COMPS_BY_GROUPID, Component.class)
				.setParameter("groupId", groupId)
				.setParameter("version", artiVersion)
				.getResultList();
		comp = comps.size() > 0 ? comps.get(0) : null;
		if (comp != null) {
			return comp;
		}
		// remove -redhat-X suffix
		String version = artiVersion.replaceAll(RED_HAT_SUFFIX, "");
		comps = this.em.createNamedQuery(Queries.QUERY_LOAD_COMPS_BY_GROUPID, Component.class)
				.setParameter("groupId", groupId)
				.setParameter("version", version + "%")
				.getResultList();
		comp = comps.size() > 0 ? comps.get(0) : null;
		if (comp != null) {
			return comp;
		}
		
//		int idx = version.indexOf(".");
//		if (idx > 0) {
//			version = version.substring(0, version.indexOf("."));
//			comps = this.em.createNamedQuery(Queries.QUERY_LOAD_COMPS_BY_GROUPID, Component.class)
//					.setParameter("groupId", groupId)
//					.setParameter("version", version + "%")
//					.getResultList();
//			comp = comps.size() > 0 ? comps.get(0) : null;
//			if (comp != null) {
//				return comp;
//			}
//		}
//		return comps.size() > 0 ? comps.get(0) : null;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#updateArtifactBuildInfo(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	@RolesAllowed("tracker")
	public void updateArtifactBuildInfo(String groupId, String artifactId, String artiVersion, String buildInfo) throws DataServiceException {
		if (groupId == null || artiVersion == null) {
			throw new IllegalArgumentException("groupId and version of the Artifact can't be null.");
		}
		String HQL = "UPDATE Artifact a SET a.buildInfo = :buildInfo WHERE a.groupId = :groupId AND a.version = :version";
		if (artifactId != null && artifactId.length() > 0) {
			HQL = HQL + " AND a.artifactId = :artifactId";
		}
		Query query = this.em.createQuery(HQL)
				.setParameter("buildInfo", buildInfo)
				.setParameter("groupId", groupId)
				.setParameter("version", artiVersion);
		if (artifactId != null && artifactId.length() > 0) {
			query.setParameter("artifactId", artifactId);
		}
		query.executeUpdate();
	}
	

	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#updateArtifactComponent(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	@RolesAllowed("tracker")
	public void updateArtifactComponent(String groupId, String artifactId, String artiVersion, String compName, 
			String compVer)
			throws DataServiceException {
		Component comp = getComponent(compName, compVer);
		if (comp == null) {
			comp = new Component();
			comp.setName(compName);
			comp.setVersion(compVer);
			comp.setGroupId(groupId);
			this.em.persist(comp);
			logger.debug("No component found: " + compName + ":" + compVer + ", Create one.");
		}
		if (groupId == null || artiVersion == null) {
			throw new IllegalArgumentException("groupId and version of the Artifact can't be null.");
		}
		String HQL = "UPDATE Artifact a SET a.component = :component WHERE a.groupId = :groupId AND a.version = :version";
		if (artifactId != null && artifactId.length() > 0) {
			HQL = HQL + " AND a.artifactId = :artifactId";
		}
		Query query = this.em.createQuery(HQL)
				.setParameter("component", comp)
				.setParameter("groupId", groupId)
				.setParameter("version", artiVersion);
		if (artifactId != null && artifactId.length() > 0) {
			query.setParameter("artifactId", artifactId);
		}
		query.executeUpdate();
	}
	
	@RolesAllowed("tracker")
	@Override
	public void updateArtifactChecksum(String groupId, String artifactId,
			String version, String checksum) throws DataServiceException {
		Artifact arti = getArtifact(groupId, artifactId, version);
		if (arti == null) {
			throw new IllegalArgumentException("Artifact is Not found");
		}
		arti.setChecksum(checksum);
		this.em.merge(arti);
	}

	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#removeArtifacts(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	@RolesAllowed("tracker")
	public void removeArtifacts(String productName, String version, String groupId, String artifactId, String artiVersion)
			throws DataServiceException {
		ProductVersion pv = getProductVersion(productName, version);
		if (pv == null) {
			throw new DataServiceException("No ProductVersion found: " + productName + ":" + version);
		}
		if (groupId == null || artiVersion == null) {
			throw new IllegalArgumentException("groupId and version of the Artifact can't be null.");
		}
		List<Artifact> artis = pv.getArtifacts();
		if (artis == null) {
			logger.warn("No artifacts found in Product version: " + pv.toString());
			return;
		}
		List<Artifact> artisToDelete = new ArrayList<Artifact>();
		for (Artifact arti: artis) {
			if (arti.getGroupId().equals(groupId) && arti.getVersion().equals(artiVersion)) {
				if (artifactId != null && arti.getArtifactId().equals(artifactId)) {
					artisToDelete.add(arti);
				}
				else if (artifactId == null) {
					artisToDelete.add(arti);
				}
			}
		}
		if (artisToDelete.isEmpty()) {
			logger.warn("No artifacts are removed from product version: " + pv.toString());
			return;
		}
		artis.removeAll(artisToDelete);
		this.em.merge(pv);
		
		// remove pv from all the artifacts from links in memory
		for (Artifact arti: artisToDelete) {
			List<ProductVersion> pvs = arti.getPvs();
			if (pvs != null && pvs.contains(pv)) {
				pvs.remove(pv);
				this.em.merge(arti);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#importArtifacts(java.lang.String, java.lang.String, java.net.URL)
	 */
	@Override
	@RolesAllowed("tracker")
	public void importArtifacts(String productName, String version, URL artifactListURL) throws DataServiceException {
		if (artifactListURL == null) {
			throw new IllegalArgumentException("URL of the artifact lists can't be null.");
		}
		try {
			List<String> artiStrs = ArtifactsUtil.getMatchRegexLines(artifactListURL.openStream(), ARTI_STR_REGEX);
			if (artiStrs == null || artiStrs.size() == 0) {
				logger.warn("No Artifacts will be imported, because there are no artifacts in url: " + artifactListURL);
			}
			importArtifacts(productName, version, artiStrs);
		} catch (IOException e) {
			throw new DataServiceException("Can't read artifacts information from URL: " + artifactListURL, e);
		}
	}
	
	@Override
	@RolesAllowed("tracker")
	public void importArtifacts(String productName, String version,
			List<String> artiStrs) throws DataServiceException {
		if (artiStrs != null && artiStrs.size() > 0) {
			for (String artiStr: artiStrs) {
				// Format: groupId:artifactId:version:type:checksum
				String[] artiArray = artiStr.split(":");
				if (artiArray.length < 3) {
					throw new DataServiceException("Wrong Format of Artifact String: " + artiStr);
				}
				String groupId = artiArray[0].trim();
				String artifactId = artiArray[1].trim();
				String artiVersion = artiArray[2].trim();
				String type = DEFAULT_ARTIFACT_TYPE;
				if (artiArray.length >= 4 && artiArray[3] != null && artiArray[3].length() > 0) {
					type = artiArray[3].trim();
				}
				String checksum = null;
				if (artiArray.length >= 5 && artiArray[4] != null && artiArray[4].length() > 0) {
					checksum = artiArray[4].trim();
				}
				addArtifact(productName, version, groupId, artifactId, artiVersion, type, checksum);
			}
		}
	}
	
	@Override
	@RolesAllowed("tracker")
	public void importArtifacts(URL artifactListURL) throws DataServiceException {
		if (artifactListURL == null) {
			throw new IllegalArgumentException("URL of the artifact lists can't be null.");
		}
		try {
			importArtifactsFromInput(artifactListURL.openStream());
		} catch (IOException e) {
			throw new DataServiceException("Can't read artifacts information from URL: " + artifactListURL, e);
		}
	}
	
	@Override
	@RolesAllowed("tracker")
	@TransactionTimeout(unit = TimeUnit.MINUTES, value = 30L)
	public void importArtifactsFromInput(InputStream input) throws DataServiceException {
		if (input == null) {
			throw new NullPointerException("InputStream is null");
		}
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#")) {
					continue;
				}
				if (line.matches(ARTI_STR_REGEX)) {
					String[] artiArray = line.split(":");
					String groupId = artiArray[0].trim();
					String artifactId = artiArray[1].trim();
					String artiVersion = artiArray[2].trim();
					String type = DEFAULT_ARTIFACT_TYPE;
					if (artiArray.length >= 4 && artiArray[3] != null && artiArray[3].length() > 0) {
						type = artiArray[3].trim();
					}
					String checksum = null;
					if (artiArray.length >= 5 && artiArray[4] != null && artiArray[4].length() > 0) {
						checksum = artiArray[4].trim();
					}
					Artifact arti = getArtifact(groupId, artifactId, artiVersion);
					if (arti != null) {
						logger.debug("Artifact: " + arti.toString() + " has been added already."); 
					}
					else {
						arti = new Artifact();
						arti.setArtifactId(artifactId);
						arti.setGroupId(groupId);
						arti.setVersion(artiVersion);
						arti.setType(type == null ? DEFAULT_ARTIFACT_TYPE : type);
						arti.setChecksum(checksum);
						Component component = guessComponent(groupId, null, artiVersion);
						if (component != null) {
							arti.setComponent(component);
						}
						this.em.persist(arti);
					}
				}
			}
		} catch (IOException e) {
			throw new DataServiceException("Can't read artifacts information.", e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#getComponent(java.lang.String, java.lang.String)
	 */
	@Override
	public Component getComponent(String name, String version)
			throws DataServiceException {
		if (name == null || version == null) {
			throw new IllegalArgumentException("Both name and version can't be null.");
		}
		List<Component> comps = this.em.createNamedQuery(Queries.QUERY_LOAD_COMP_BY_NAME_AND_VER, Component.class)
				.setParameter("name", name)
				.setParameter("version", version)
				.getResultList();
		return comps.size() > 0 ? comps.get(0) : null;
	}
	
	

	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#loadNativeComponents(java.lang.String, java.lang.String)
	 */
	@Override
	public List<Component> loadNativeComponents(String productName,
			String version) throws DataServiceException {
		if (productName == null || version == null) {
			throw new IllegalArgumentException("Both productName and version can't be null.");
		}
		return this.em.createNamedQuery(Queries.QUERY_LOAD_COMPS_BY_PV, Component.class)
				.setParameter("name", productName)
				.setParameter("version", version)
				.getResultList();
	}

	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#addNativeComponent(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void addNativeComponent(String productName, String version, String compName, String compVer)
			throws DataServiceException {
		ProductVersion pv = getProductVersion(productName, version);
		if (pv == null) {
			throw new DataServiceException("No ProductVersion found: " + productName + ":" + version);
		}
		List<Component> nativeComps = pv.getNativeComps();
		if (nativeComps == null) {
			nativeComps = new ArrayList<Component>();
			pv.setNativeComps(nativeComps);
		}
		Component comp = getComponent(compName, compVer);
		if (comp != null) {
			logger.debug("Component: " + comp.toString() + " has been added already. Associate it to " + 
					productName + ":" + version); 
		} else {
			comp = new Component();
			comp.setName(compName);
			comp.setVersion(compVer);
			this.em.persist(comp);
		}
		if (nativeComps.contains(comp)) {
			throw new DataServiceException("Component: " + comp.toString() + " has been associated with the Product Version: " + pv.toString());
		}
		nativeComps.add(comp);
		this.em.merge(pv);
		List<ProductVersion> compPVs = comp.getPvs();
		if (compPVs == null) {
			compPVs = new ArrayList<ProductVersion>();
			comp.setPvs(compPVs);
		}
		if (compPVs.contains(pv)) {
			throw new DataServiceException("Wrong state");
		}
		compPVs.add(pv);
		this.em.merge(comp);
		
	}

	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#removeNativeComponent(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void removeNativeComponent(String productName, String version,
			String compName, String compVer) throws DataServiceException {
		ProductVersion pv = getProductVersion(productName, version);
		if (pv == null) {
			throw new DataServiceException("No ProductVersion found: " + productName + ":" + version);
		}
		Component comp = getComponent(compName, compVer);
		if (comp == null) {
			throw new DataServiceException("No Component found: " + compName + ":" + compVer);
		}
		
		List<Component> comps = pv.getNativeComps();
		if (comps == null) {
			logger.warn("No native components found in Product version: " + pv.toString());
			return;
		}
		if (comps.remove(comp)) {
			this.em.merge(pv);
			// remove pv from all the components from links in memory
			List<ProductVersion> pvs = comp.getPvs();
			if (pvs != null && pvs.contains(pv)) {
				pvs.remove(pv);
				this.em.merge(comp);
			}
		} else {
			logger.warn("No Components are removed from product version: " + pv.toString());
		}
	}

	
	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#importComponents(java.net.URL)
	 */
	@Override
	public void importComponents(URL compListURL) throws DataServiceException {
		if (compListURL == null) {
			throw new IllegalArgumentException("URL of the Component lists can't be null.");
		}
		try {
			List<String> compList = ArtifactsUtil.getMatchRegexLines(compListURL.openStream(), COMP_STR_REGEX);
			if (compList == null || compList.size() == 0) {
				logger.warn("No Components will be imported, because there are no components in url: " + compListURL);
			}
			importComponents(compList);
		} catch (IOException e) {
			throw new DataServiceException("Can't read components information from URL: " + compListURL, e);
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#importComponents(java.util.List)
	 */
	@Override
	public void importComponents(List<String> componentList)
			throws DataServiceException {
		if (componentList == null || componentList.size() == 0) {
			logger.warn("No components are provided.");
		}
		for (String compStr: componentList) {
			String[] artiArray = compStr.split(":");
			if (artiArray.length < 2) {
				throw new DataServiceException("Wrong Format of Component String: " + compStr);
			}
			String compName = artiArray[0].trim();
			String compVer = artiArray[1].trim();
			String groupId = null;
			if (artiArray.length >= 3 && artiArray[2] != null) {
				groupId = artiArray[2].trim();
			}
			Component comp = getComponent(compName, compVer);
			if (comp != null){
				logger.debug("Component: " + comp.toString() + " has been added already!");
			} else {
				comp = new Component();
				comp.setName(compName);
				comp.setVersion(compVer);
				if (groupId != null) {
					comp.setGroupId(groupId);
				}
				this.em.persist(comp);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#importNativeComponents(java.lang.String, java.lang.String, java.net.URL)
	 */
	@Override
	@RolesAllowed("tracker")
	public void importNativeComponents(String productName, String version,
			URL compListURL) throws DataServiceException {
		if (compListURL == null) {
			throw new IllegalArgumentException("URL of the Component lists can't be null.");
		}
		try {
			List<String> compList = ArtifactsUtil.getMatchRegexLines(compListURL.openStream(), COMP_STR_REGEX);
			if (compList == null || compList.size() == 0) {
				logger.warn("No Components will be imported, because there are no components in url: " + compListURL);
			}
			importNativeComponents(productName, version, compList);
		} catch (IOException e) {
			throw new DataServiceException("Can't read components information from URL: " + compListURL, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#importNativeComponents(java.lang.String, java.lang.String, java.util.List)
	 */
	@Override
	@RolesAllowed("tracker")
	public void importNativeComponents(String productName, String version,
			List<String> compList) throws DataServiceException {
		if (compList != null && compList.size() > 0) {
			for (String compStr: compList) {
				// Format: name:version
				String[] artiArray = compStr.split(":");
				if (artiArray.length < 2) {
					throw new DataServiceException("Wrong Format of Component String: " + compStr);
				}
				String compName = artiArray[0].trim();
				String compVer = artiArray[1].trim();
				addNativeComponent(productName, version, compName, compVer);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#saveComponent(org.jboss.eap.trackers.model.Component)
	 */
	@Override
	@RolesAllowed("tracker")
	public void saveComponent(Component comp) throws DataServiceException {
		Session session = (Session)em.getDelegate();
		session.saveOrUpdate(comp);
	}

	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#updateNote(java.io.Serializable, java.lang.String, java.lang.String)
	 */
	@Override
	@RolesAllowed("tracker")
	public void updateNote(Long id, String type, String note)
			throws DataServiceException {
		if (id == null || type == null || note == null) {
			throw new IllegalArgumentException("id, type and note can't be null");
		}
		if (!(type.equals("ProdcutVersion")) && !(type.equals("Artifact"))) {
			throw new IllegalArgumentException("Type can only be one of: ProductVersion | Artifact");
		}
		String HQL = "UPDATE " + type + " entity SET entity.note = :note WHERE entity.id = :id";
		int result = this.em.createQuery(HQL).setParameter("id", id).setParameter("note", note).executeUpdate();
		if (result != 1) {
			throw new DataServiceException("Error to update note of " + type + ":" + id);
		}
	}

    /* (non-Javadoc)
     * @see org.jboss.eap.trackers.data.DataService#affectedProducts(java.lang.String)
     */
    @Override
    public Set<ProductVersion> affectedProducts(String cveName) throws DataServiceException {
        Set<Artifact> artis = affectedArtifacts(cveName);
        Set<ProductVersion> pvs = new HashSet<ProductVersion>();
        for (Artifact arti: artis) {
            if (arti.getPvs() != null) {
                pvs.addAll(arti.getPvs());
            }
        }
        // look up for native components
        Set<Component> nativeComps = affectedNativeCompoents(cveName);
        for (Component comp: nativeComps) {
            if (comp.getPvs() != null) {
                pvs.addAll(comp.getPvs());
            }
        }
        return pvs;
    }
    
    List<Artifact> getArtifacts(String grpId, String artiId) throws DataServiceException {
        return this.em.createNamedQuery(Queries.QUERY_LOAD_ARTIS_BY_GRP_AND_ARTIID, Artifact.class)
                .setParameter("groupId", grpId)
                .setParameter("artifactId", artiId)
                .getResultList();
    }
    
    @Override
    public CVE getCVE(String cveName) throws DataServiceException {
        List<CVE> cves = this.em.createNamedQuery(Queries.QUERY_GET_CVE_BY_NAME, CVE.class)
          .setParameter("name", cveName).getResultList();
        return cves.size() > 0 ? cves.get(0) : null;
    }

    /* (non-Javadoc)
     * @see org.jboss.eap.trackers.data.DataService#affectedArtifacts(java.lang.String)
     */
    @Override
    public Set<Artifact> affectedArtifacts(String cveName) throws DataServiceException {
        CVE cve = getCVE(cveName);
        if (cve == null) {
            return Collections.emptySet();
        }
        Set<AffectedArtifact> artis = cve.getAffectedArtis();
        if (artis != null) {
            Set<Artifact> result = new HashSet<Artifact>();
            for (AffectedArtifact arti: artis) {
                if (!arti.isNativeComponent()) {
                    String grpId = arti.getArtiGrpId();
                    String artiId = arti.getArtiId();
                    VersionScopes versionScopes = arti.getVersionScopes();
                    for (Artifact a: getArtifacts(grpId, artiId)) {
                        if (versionScopes.isCaptured(a.getVersion())) {
                            result.add(a);
                        }
                    }
                }
            }
            return result;
        }
        return Collections.emptySet();
    }
    
    @Override
    public Set<Component> affectedNativeCompoents(String cveName) throws DataServiceException {
        CVE cve = getCVE(cveName);
        if (cve == null) {
            return Collections.emptySet();
        }
        Set<AffectedArtifact> artis = cve.getAffectedArtis();
        if (artis != null) {
            Set<Component> result = new HashSet<Component>();
            for (AffectedArtifact arti: artis) {
                if (arti.isNativeComponent()) {
                    String grpId = arti.getArtiGrpId();
                    String artiId = arti.getArtiId();
                    VersionScopes versionScopes = arti.getVersionScopes();
                    for (Component c: getNativeComponets(grpId, artiId)) {
                        if (versionScopes.isCaptured(c.getVersion())) {
                            result.add(c);
                        }
                    }
                }
            }
            return result;
        }
        return Collections.emptySet();
    }

    private List<Component> getNativeComponets(String grpId, String artiId) {
        String hql = "SELECT c FROM Component c WHERE c.name = :name";
        if (grpId != null && grpId.length() > 0) {
            hql = hql + " AND c.groupId = :groupId";
        }
        TypedQuery<Component> query = this.em.createQuery(hql, Component.class)
                .setParameter("name", artiId);
        if (grpId != null && grpId.length() > 0) {
            query.setParameter("groupId", grpId);
        }
        return query.getResultList();
    }

    /* (non-Javadoc)
     * @see org.jboss.eap.trackers.data.DataService#productCVEs(java.lang.String, java.lang.String)
     */
    @Override
    public SortedSet<CVE> productCVEs(String prdName, String prdVersion) throws DataServiceException {
        ProductVersion pv = getProductVersion(prdName, prdVersion);
        SortedSet<CVE> cves = new TreeSet<CVE>();
        if (pv != null) {
            if (pv.getArtifacts() != null) {
                for (Artifact arti: pv.getArtifacts()) {
                    for (AffectedArtifact affectedArti: getAffectedArtis(arti.getGroupId(), arti.getArtifactId())) {
                        if (affectedArti.getVersionScopes().isCaptured(arti.getVersion())) {
                            cves.addAll(affectedArti.getCves());
                        }
                    }
                }
            }
            if (pv.getNativeComps() != null) {
                for (Component comp: pv.getNativeComps()) {
                    for (AffectedArtifact affectedArti: getAffectedArtisNativeComps(null, comp.getName())) {
                        if (affectedArti.getVersionScopes().isCaptured(comp.getVersion())) {
                            cves.addAll(affectedArti.getCves());
                        }
                    }
                }
            }
        }
        return cves;
    }
    
    @Override
    public List<AffectedArtifact> getAffectedArtis(String grpId, String artiId) throws DataServiceException {
        return this.em.createNamedQuery(Queries.QUERY_LOAD_AFFECTED_ARTIS_BY_GRP_AND_ARTIID, AffectedArtifact.class)
                .setParameter("groupId", grpId)
                .setParameter("artifactId", artiId)
                .getResultList();
    }
    
    public List<AffectedArtifact> getAffectedArtisNativeComps(String grpId, String name) throws DataServiceException {
        String hql = "SELECT a FROM AffectedArtifact a WHERE a.nativeComponent = true AND a.artiId = :name";
        if (grpId != null && grpId.length() > 0) {
            hql = hql + " AND a.artiGrpId = :groupId";
        }
        TypedQuery<AffectedArtifact> query = this.em.createQuery(hql, AffectedArtifact.class).setParameter("name", name);
        if (grpId != null && grpId.length() > 0) {
            query.setParameter("groupId", grpId);
        }
        return query.getResultList();
    }
    
    /* (non-Javadoc)
     * @see org.jboss.eap.trackers.data.DataService#artifactCVEs(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public SortedSet<CVE> artifactCVEs(String groupId, String artifactId, String version) throws DataServiceException {
        Artifact arti = getArtifact(groupId, artifactId, version);
        if (arti == null) {
            return new TreeSet<CVE>();
        }
        SortedSet<CVE> cves = new TreeSet<CVE>();
        for (AffectedArtifact affectedArti: getAffectedArtis(arti.getGroupId(), arti.getArtifactId())) {
            if (affectedArti.getVersionScopes().isCaptured(arti.getVersion())) {
                cves.addAll(affectedArti.getCves());
            }
        }
        return cves;
    }
    
    @Override
    public SortedSet<CVE> nativeComponentCVEs(String compName, String version) throws DataServiceException {
        Component nativeComp = getComponent(compName, version);
        if (nativeComp == null) {
            return new TreeSet<CVE>();
        }
        SortedSet<CVE> cves = new TreeSet<CVE>();
        for (AffectedArtifact affectedArti: getAffectedArtisNativeComps(null, nativeComp.getName())) {
            if (affectedArti.getVersionScopes().isCaptured(nativeComp.getVersion())) {
                cves.addAll(affectedArti.getCves());
            }
        }
        return cves;
    }

    /* (non-Javadoc)
     * @see org.jboss.eap.trackers.data.DataService#newCVE(java.lang.String)
     */
    @Override
    @RolesAllowed("tracker")
    public CVE newCVE(String cveName) throws IllegalArgumentException, DataServiceException {
        if (cveName == null || cveName.length() == 0) {
            throw new IllegalArgumentException("CVE Name must be provided.");
        }
        if (!CVE_NAME_PATTERN.matcher(cveName).matches()) {
            throw new IllegalArgumentException("Illegal CVE name, must follow: CVE-XXXX-XXXX, X is the number.");
        }
        CVE cve = getCVE(cveName);
        if (cve != null) {
            return cve;
        }
        cve = new CVE();
        cve.setName(cveName);
        this.em.persist(cve);
        return cve;
    }

    /* (non-Javadoc)
     * @see org.jboss.eap.trackers.data.DataService#updateCVE(org.jboss.eap.trackers.model.CVE)
     */
    @Override
    @RolesAllowed("tracker")
    public CVE updateCVE(CVE cve) throws DataServiceException {
        if (cve == null) {
            throw new IllegalArgumentException("CVE is null");
        }
        Session session = (Session)em.getDelegate();
        session.saveOrUpdate(cve);
        return getCVE(cve.getName());
    }

    /* (non-Javadoc)
     * @see org.jboss.eap.trackers.data.DataService#cveAffected(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    @RolesAllowed("tracker")
    public CVE cveAffected(String cveName, String groupId, String artiId, String versionScope, boolean component) throws IllegalArgumentException,
            DataServiceException {
        CVE cve = newCVE(cveName);
        AffectedArtifact affectedArti = new AffectedArtifact();
        affectedArti.setArtiGrpId(groupId);
        affectedArti.setArtiId(artiId);
        affectedArti.setNativeComponent(component);
        affectedArti.setVersionScopes(new VersionScopes(versionScope));
        SortedSet<CVE> cveSet = new TreeSet<CVE>();
        cveSet.add(cve);
        affectedArti.setCves(cveSet);
        
        Set<AffectedArtifact> affectedArtis = cve.getAffectedArtis();
        if (affectedArtis == null) {
            affectedArtis = new HashSet<AffectedArtifact>();
            cve.setAffectedArtis(affectedArtis);
        }
        affectedArtis.add(affectedArti);
        
        Session session = (Session)em.getDelegate();
        session.saveOrUpdate(cve);
        return getCVE(cveName);
    }
	
    
}
;