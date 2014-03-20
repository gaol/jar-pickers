/**
 * 
 */
package org.jboss.eap.trackers.data.file;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.eap.trackers.data.DataService;
import org.jboss.eap.trackers.data.DataServiceException;
import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;

/**
 * @author lgao
 *
 * This implements a DataService with file backend as the data store.
 * 
 * This requires that the read/modify permissions are granted for the configured directory.
 * 
 */
public class FileBasedDataService implements DataService {

	private static final String KEY_FILE_STORE_DIR = "file.data.store.dir";
	
	private static final String VALUE_FILE_STORE_DIR_DEFAULT = "/tmp/eap-trackers/";
	
	private static FileBasedDataService INSTANCE;
	
	private final Logger logger = Logger.getLogger(FileBasedDataService.class.getName());
	
	/**
	 * Singleton FileBasedDataService
	 */
	public synchronized static FileBasedDataService instance() {
		if (INSTANCE == null)
		{
			INSTANCE = new FileBasedDataService();
		}
		return INSTANCE;
	}
	
	private final File dataStoreDir;
	
	private final ProductStore productStore;
	
	private final ComponentStore componentStore;
	
	private FileBasedDataService()
	{
		super();
		String storeDir = getConfigedStoreDir();
		if (storeDir == null)
		{
			throw new IllegalStateException("Store Directory is null. set it using System property key: -D" + KEY_FILE_STORE_DIR + "=XXX");
		}
		dataStoreDir = new File(storeDir);
		initDB();
		try {
			this.productStore = new ProductStore(this.dataStoreDir);
			this.componentStore = new ComponentStore(dataStoreDir);
		} catch (IOException e) {
			throw new RuntimeException("Error when initing data store services.", e);
		}
	}
	

	private String getConfigedStoreDir() {
		SecurityManager sm = System.getSecurityManager();
		if (sm == null)
		{
			return System.getProperty(KEY_FILE_STORE_DIR, VALUE_FILE_STORE_DIR_DEFAULT);
		}
		return AccessController.doPrivileged(new PrivilegedAction<String>() {
			@Override
			public String run() {
				return System.getProperty(KEY_FILE_STORE_DIR, VALUE_FILE_STORE_DIR_DEFAULT);
			}
		}); 
	}
	
	private void initDB() {
		assert this.dataStoreDir != null : "Wrong state. this.dataStoreDir should be assigned first.";
		if (!this.dataStoreDir.exists())
		{
			logger.info("Create data store at: " + this.dataStoreDir.getAbsolutePath());
			if (!this.dataStoreDir.mkdirs())
			{
				throw new IllegalStateException("Can't create datastore directory: " + this.dataStoreDir.getAbsolutePath());
			}
		}
		if (!this.dataStoreDir.canRead())
		{
			throw new IllegalStateException("Can't read from datastore directory: " + this.dataStoreDir.getAbsolutePath());
		}
		if (!this.dataStoreDir.canWrite())
		{
			throw new IllegalStateException("Can't write to datastore directory: " + this.dataStoreDir.getAbsolutePath());
		}
	}

	@Override
	public List<Product> loadAllProducts() throws DataServiceException {
		try {
			return this.productStore.loadAllProducts();
		} catch (IOException e) {
			throw new DataServiceException("Can't load all Products information", e);
		}
	}
	
	@Override
	public List<Product> saveProduct(Product product) throws DataServiceException {
		try {
			return this.productStore.saveProduct(product);
		} catch (IOException e) {
			throw new DataServiceException("Can't save product: " + product.toString(), e);
		}
	}
	
	@Override
	public List<Product> removeProduct(Product product)
			throws DataServiceException {
		try {
			return this.productStore.removeProduct(product);
		} catch (IOException e) {
			throw new DataServiceException("Can't remove product: " + product.toString(), e);
		}
	}
	
	@Override
	public List<Product> removeProductVersion(Product product, ProductVersion pv)
			throws DataServiceException {
		if (!loadAllProducts().contains(product))
		{
			throw new DataServiceException("Product: " + product + " does not exist.");
		}
		List<ProductVersion> pvs = product.getVersions();
		if (pvs != null)
		{
			pvs.remove(pv);
		}
		return saveProduct(product);
	}

	@Override
	public List<Product> saveProductVersions(Product product, List<ProductVersion> pvs) throws DataServiceException {
		for (ProductVersion pv: pvs)
		{
			pv.setProduct(product);
		}
		product.setVersions(pvs);
		return saveProduct(product);
	}

	@Override
	public List<Component> loadComponent(ProductVersion pv) throws DataServiceException {
		try {
			return this.componentStore.loadAllComponents(pv);
		} catch (IOException e) {
			throw new DataServiceException("Can't load Components", e);
		}
	}

	@Override
	public List<Component> saveComponents(ProductVersion pv, List<Component> components) throws DataServiceException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Component> saveComponent(ProductVersion pv, Component component)
			throws DataServiceException {
		List<Component> components = loadComponent(pv);
		boolean update = false;
		for (int i = 0; i < components.size(); i ++)
		{
			Component comp = components.get(i);
			if (comp.getName().equals(component.getName()) && comp.getVersion().equals(component.getVersion()))
			{
				components.set(i, component);
				update = true;
				break;
			}
		}
		if (!update)
		{
			components.add(component);
		}
		return saveComponents(pv, components);
	}

}
