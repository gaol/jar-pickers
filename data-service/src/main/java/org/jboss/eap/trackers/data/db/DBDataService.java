/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.data.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jboss.eap.trackers.data.DataService;
import org.jboss.eap.trackers.data.DataServiceException;
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
@Path("/data/")
public class DBDataService implements DataService {

	@Inject
	private Logger logger;
	
	@Inject
	private EntityManager em;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public List<Product> loadAllProducts() throws DataServiceException {
		return em.createNamedQuery(Queries.QUERY_LOAD_PRODUCTS_NAME, Product.class).getResultList();
	}

	@Override
	public List<Product> saveProduct(Product product)
			throws DataServiceException {
		Session session = (Session)em.getDelegate();
		session.saveOrUpdate(product);
		return loadAllProducts();
	}

	@Override
	public List<Product> removeProduct(String productName)
			throws DataServiceException {
		if (productName == null) {
			throw new IllegalArgumentException("Product Name should be provided."); 
		}
		int result = em.createNamedQuery(Queries.DELETE_PROUDCT_BY_NAME).setParameter("name", productName).executeUpdate();
		if (result != 1) {
			logger.warn("No proudct be deleted using name: " + productName);
		}
		return loadAllProducts();
	}

	@Override
	public void removeProductVersion(String productName, String version)
			throws DataServiceException {
		Product prod = getProductByName(productName);
		if (prod == null) {
			throw new DataServiceException("Unknown Product name: " + productName);
		}
		if (version == null) {
			throw new IllegalArgumentException("Version can't be empty.");
		}
		int result = em.createNamedQuery(Queries.DELETE_PROUDCT_VERSION_BY_NAME_VER)
				.setParameter("name", productName)
				.setParameter("version", version)
				.executeUpdate();
		if (result != 1) {
			logger.warn("No proudct version be deleted using name: " + productName + ""
					+ " and version: " + version);
		}
	}
	
	@Override
	public Product getProductByName(String name) throws DataServiceException {
		if (name == null) {
			throw new IllegalArgumentException("Product Name should be provided.");
		}
		return this.em.createNamedQuery(Queries.QUERY_LOAD_PRODUCT_BY_NAME, Product.class).
				setParameter("name", name).getSingleResult();
	}

	@Override
	public void addProductVersions(String productName,
			Set<String> versions) throws DataServiceException {
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
			pvs.add(pv);
		}
		prod.setVersions(pvs);
		this.em.persist(prod);
	}

	@Override
	public List<Component> loadComponents(ProductVersion pv)
			throws DataServiceException {
		if (pv == null) {
			throw new IllegalArgumentException("ProductVersion can't be null.");
		}
		return loadComponents(pv.getProduct().getName(), pv.getVersion());
	}
	
	@Override
	public List<Component> loadComponents(String productName, String version)
			throws DataServiceException {
		//TODO
		if (productName == null) {
			throw new IllegalArgumentException("Product Name can't be null.");
		}
		if (version == null) {
			throw new IllegalArgumentException("version Name can't be null.");
		}
		
		return null;
	}

	@Override
	public List<Component> saveComponents(ProductVersion pv,
			List<Component> components) throws DataServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Component> saveComponent(ProductVersion pv, Component component)
			throws DataServiceException {
		// TODO Auto-generated method stub
		return null;
	}

}
