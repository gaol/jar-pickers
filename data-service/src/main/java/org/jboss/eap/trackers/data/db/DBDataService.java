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
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.hibernate.Session;
import org.jboss.eap.trackers.data.DataService;
import org.jboss.eap.trackers.data.DataServiceException;
import org.jboss.eap.trackers.model.Artifact;
import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;
import org.jboss.eap.trackers.model.Queries;
import org.jboss.logging.Logger;

/**
 * @author lgao
 *
 */
@Stateless
@Path("/")
@PermitAll
public class DBDataService implements DataService {

	@Inject
	private Logger logger;
	
	@Inject
	private EntityManager em;
	
	@Resource
	private UserTransaction trans;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public List<Product> loadAllProducts() throws DataServiceException {
		return em.createNamedQuery(Queries.QUERY_LOAD_PRODUCTS_NAME, Product.class).getResultList();
	}

	@POST
	@Path("/p")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed("tracker")
	@Override
	public List<Product> saveProduct(Product product)
			throws DataServiceException {
		Session session = (Session)em.getDelegate();
		session.saveOrUpdate(product);
		return loadAllProducts();
	}

	@DELETE
	@Path("/p/{productName}")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("tracker")
	@Override
	public List<Product> removeProduct(@PathParam("productName") String productName)
			throws DataServiceException {
		Product prod = getProductByName(productName);
		if (prod != null) {
			em.remove(prod);
		}
		return loadAllProducts();
	}

	@Override
	@DELETE
	@Path("/pv/{productName}:{version}")
	@RolesAllowed("tracker")
	public void removeProductVersion(@PathParam("productName") String productName, 
			@PathParam("version") String version)
			throws DataServiceException {
		ProductVersion pv = getProductVersion(productName, version);
		if (pv == null) {
			throw new DataServiceException("Unkown product version: " + productName + ":" + version);
		}
		this.em.remove(pv);
	}
	
	@GET
	@Path("/pv/{productName}:{version}")
	@Produces(MediaType.APPLICATION_JSON)
	public ProductVersion getProductVersion(@PathParam("productName") String productName, 
			@PathParam("version") String version) throws DataServiceException {
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
	
	@GET
	@Path("/p/{productName}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Product getProductByName(@PathParam("productName") String name) throws DataServiceException {
		if (name == null) {
			throw new IllegalArgumentException("Product Name should be provided.");
		}
		List<Product> prodList = this.em.createNamedQuery(Queries.QUERY_LOAD_PRODUCT_BY_NAME, Product.class)
				.setParameter("name", name)
				.getResultList();
		return prodList.size() > 0 ? prodList.get(0) : null;
	}
	
	@GET
	@Path("/p/vers/{productName}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public List<String> getVersions(@PathParam("productName") String productName)
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

	@PUT
	@Path("/pv/{productName}/{versions}")
	@RolesAllowed("tracker")
	@Override
	public void addProductVersions(@PathParam("productName") String productName,
			@PathParam("versions") Set<String> versions) throws DataServiceException {
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
	@GET
	@Path("/a/{productName}:{version}")
	public List<Artifact> loadArtifacts(@PathParam("productName") String productName, 
			@PathParam("version") String version)
			throws DataServiceException {
		if (productName == null || version == null) {
			throw new IllegalArgumentException("Both productName and version can't be null.");
		}
		return this.em.createNamedQuery(Queries.QUERY_LOAD_ARTIFACTS_BY_PV, Artifact.class)
				.setParameter("name", productName)
				.setParameter("version", version)
				.getResultList();
	}

	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#addArtifact(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	@PUT
	@Path("/a/{productName}:{version}/{groupId}:{artifactId}:{artiVersion}")
	@RolesAllowed("tracker")
	public void addArtifact(@PathParam("productName") String productName, @PathParam("version") String version,
			@PathParam("groupId") String groupId, @PathParam("artifactId") String artifactId, 
			@PathParam("artiVersion") String artiVersion) throws DataServiceException {
		addArtifact(productName, version, groupId, artifactId, artiVersion, DEFAULT_ARTIFACT_TYPE);
	}
	
	private void addArtifact(@PathParam("productName") String productName, @PathParam("version") String version,
			@PathParam("groupId") String groupId, @PathParam("artifactId") String artifactId, 
			@PathParam("artiVersion") String artiVersion, @PathParam("type") String type) throws DataServiceException {
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
			logger.info("Artifact: " + arti.toString() + " has been added already. Add it to " + 
					productName + ":" + version); 
		}
		else {
			arti = new Artifact();
			arti.setArtifactId(artifactId);
			arti.setGroupId(groupId);
			arti.setVersion(artiVersion);
			arti.setType(type == null ? DEFAULT_ARTIFACT_TYPE : type);
			Component component = guessComponent(groupId, artifactId, artiVersion);
			if (component != null) {
				arti.setComponent(component);
			}
			this.em.persist(arti);
		}
		if (artifacts.contains(arti)) {
			throw new DataServiceException("Artifact: " + arti.toString() + " has been added to the Product Version: " + pv.toString());
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
		this.em.persist(arti);
	}
	
	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#getArtifact(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	@GET
	@Path("/a/{groupId}:{artifactId}:{artiVersion}")
	public Artifact getArtifact(@PathParam("groupId") String groupId, 
			@PathParam("artifactId") String artifactId, 
			@PathParam("artiVersion") String version) throws DataServiceException {
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
	@GET
	@Path("/c/{groupId}:{artifactId}:{artiVersion}")
	@Override
	public Component guessComponent(@PathParam("groupId") String groupId, 
			@PathParam("artifactId") String artifactId, 
			@PathParam("artiVersion") String artiVersion) throws DataServiceException {
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
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#updateArtifactBuildInfo(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	@POST
	@Path("/ab/{groupId}:{artifactId}:{artiVersion}/{buildInfo}")
	@RolesAllowed("tracker")
	public void updateArtifactBuildInfo(@PathParam("groupId") String groupId, 
			@PathParam("artifactId") String artifactId, 
			@PathParam("artiVersion") String artiVersion, @PathParam("buildInfo") String buildInfo) throws DataServiceException {
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
	@POST
	@Path("/ac/{groupId}:{artifactId}:{artiVersion}/{compName}:{compVer}")
	@RolesAllowed("tracker")
	public void updateArtifactComponent(@PathParam("groupId") String groupId, 
			@PathParam("artifactId") String artifactId, 
			@PathParam("artiVersion") String artiVersion, @PathParam("compName") String compName, 
			@PathParam("compVer") String compVer)
			throws DataServiceException {
		Component comp = getComponent(compName, compVer);
		if (comp == null) {
			throw new DataServiceException("No component found: " + compName + ":" + compVer);
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

	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#removeArtifacts(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@DELETE
	@Path("/a/{productName}:{version}/{groupId}:{artifactId}:{artiVersion}")
	@Override
	@RolesAllowed("tracker")
	public void removeArtifacts(@PathParam("productName") String productName, @PathParam("version") String version,
			@PathParam("groupId") String groupId, @PathParam("artifactId") String artifactId, 
			@PathParam("artiVersion") String artiVersion)
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
		this.em.merge(pv); // is it OK ?
		
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
	@PUT
	@Path("/ai/{productName}:{version}/{url}")
	@RolesAllowed("tracker")
	public void importArtifacts(@PathParam("productName") String productName, @PathParam("version") String version,
			@PathParam("url") URL artifactListURL) throws DataServiceException {
		ProductVersion pv = getProductVersion(productName, version);
		if (pv == null) {
			throw new DataServiceException("No ProductVersion found: " + productName + ":" + version);
		}
		try {
			List<String> artiStrs = getArtiStrings(artifactListURL);
			for (String artiStr: artiStrs) {
				
				// Format: groupId:artifactId:version
				String[] artiArray = artiStr.split(":");
				if (artiArray.length < 3) {
					throw new DataServiceException("Wrong Format of Artifact String: " + artiStr);
				}
				String groupId = artiArray[0];
				String artifactId = artiArray[1];
				String artiVersion = artiArray[2];
				String type = DEFAULT_ARTIFACT_TYPE;
				if (artiArray.length >= 4 && artiArray[3] != null && artiArray[3].length() > 0) {
					type = artiArray[3];
				}
				addArtifact(productName, version, groupId, artifactId, artiVersion, type);
			}
		} catch (IOException e) {
			throw new DataServiceException("Can't read artifacts information from URL: " + artifactListURL, e);
		}
		
	}

	private List<String> getArtiStrings(URL artifactListURL) throws IOException{
		InputStream input = null;
		List<String> artis = new ArrayList<String>();
		try {
			input = artifactListURL.openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#")) {
					continue;
				}
				line = line.replace("::", "");
				if (line.matches(ARTI_STR_REGEX)) {
					artis.add(line);
				}
			}
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
		return artis;
	}

	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#getComponent(java.lang.String, java.lang.String)
	 */
	@Override
	@GET
	@Path("/c/{name}:{version}")
	@Produces(MediaType.APPLICATION_JSON)
	public Component getComponent(@PathParam("name") String name, @PathParam("version") String version)
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
	 * @see org.jboss.eap.trackers.data.DataService#saveComponent(org.jboss.eap.trackers.model.Component)
	 */
	@Override
	@PUT
	@Path("/c")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed("tracker")
	public void saveComponent(Component comp) throws DataServiceException {
		Session session = (Session)em.getDelegate();
		session.saveOrUpdate(comp);
	}

	/* (non-Javadoc)
	 * @see org.jboss.eap.trackers.data.DataService#updateNote(java.io.Serializable, java.lang.String, java.lang.String)
	 */
	@Override
	@POST
	@Path("/n/{type}-{id}/{note}")
	@RolesAllowed("tracker")
	public void updateNote(@PathParam("id") Long id, @PathParam("type") String type, @PathParam("note") String note)
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
	
	@Override
	@RolesAllowed("tracker")
	public void authenticate() throws SecurityException {
		logger.info("Welcome to Trackers, Now you have the 'tracker' role.");
	}

}
