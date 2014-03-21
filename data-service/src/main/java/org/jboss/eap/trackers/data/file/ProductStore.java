/**
 * 
 */
package org.jboss.eap.trackers.data.file;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * @author lgao
 * 
 * <p>
 * 
 * ProductStore reads/writes to a json file, where all product and its versions are stored.
 * 
 * A single ProductStore instance is used by FileBasedDataService instance. All operations are thread-safe.
 * 
 * <p>
 * 
 *  Format of the json file is defined as following:
 *   <pre>
 *  [
 *    {
 *      "name": "",
 *      "full-name": "",
 *      "description": "",
 *      "note": "",
 *      "versions": [
 *        {
 *          "version": "",
 *          "note": ""
 *        }
 *      ]
 *    }
 *  ]
 * 
 * 
 * </pre>
 * 
 */
class ProductStore {

	private final File dir;

	private static final String PRDUCTS_FILE = "products.json";

	private final File prodFile;
	
	private final Object lock = new Object();
	
	private final Gson gson = new GsonBuilder().
			setPrettyPrinting().
			setExclusionStrategies(new ProductExclusionStrategy()).
			create();

	ProductStore(File dir) throws IOException {
		super();
		this.dir = dir;
		this.prodFile = new File(this.dir, PRDUCTS_FILE);
		initDB();
	}

	private void initDB() throws IOException {
		synchronized (lock) {
			if (!this.prodFile.exists()) {
				if (!this.prodFile.createNewFile()) {
					throw new IllegalStateException("Can't create data store file.");
				}
			}
			this.lock.notify();
		}
	}

	List<Product> loadAllProducts() throws IOException {
		List<Product> products = new ArrayList<Product>();
		synchronized (this.lock) {
			JsonReader reader = null;
			try
			{
				reader = new JsonReader(new FileReader(this.prodFile));
				reader.beginArray();
				while (reader.hasNext())
				{
					Product prd = gson.fromJson(reader, Product.class);
					for (ProductVersion pv: prd.getVersions())
					{
						pv.setProduct(prd);
					}
					products.add(prd);
				}
				reader.endArray();
			}
			finally
			{
				if (reader != null)
				{
					reader.close();
				}
				this.lock.notify();
			}
		}
		return products;
	}
	
	List<Product> saveProduct(Product product) throws IOException {
		List<Product> products = loadAllProducts();
		synchronized (this.lock) {
			boolean update = false;
			for (int i = 0; i < products.size(); i ++)
			{
				Product prdInStore = products.get(i);
				if (prdInStore.getName().equals(product.getName())) // name is the same.
				{
					products.set(i, product); // update to the new product 
					update = true;
					break;
				}
			}
			if (!update) // not update, so it is create.
			{
				products.add(product);
			}
			store(products);
			this.lock.notify();
		}
		return products;
	}

	private void store(List<Product> products) throws IOException {
		JsonWriter writer = null;
		try
		{
			writer = new JsonWriter(new FileWriter(prodFile));
			writer.setIndent("  ");
			writer.beginArray();
	        for (Product prd : products) {
	            gson.toJson(prd, Product.class, writer);
	        }
	        writer.endArray();
		}
		finally
		{
			if (writer != null)
			{
				writer.close();
			}
		}
	}

	List<Product> removeProduct(Product product) throws IOException {
		List<Product> products = loadAllProducts();
		synchronized (this.lock) {
			for (int i = 0; i < products.size(); i ++)
			{
				Product prdInStore = products.get(i);
				if (prdInStore.getName().equals(product.getName()))
				{
					products.remove(i);
					break;
				}
			}
			store(products);
			this.lock.notify();
		}
		return products;
	}
	
	private static class ProductExclusionStrategy implements ExclusionStrategy {

		/* (non-Javadoc)
		 * @see com.google.gson.ExclusionStrategy#shouldSkipField(com.google.gson.FieldAttributes)
		 */
		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			Class<?> cls = f.getDeclaringClass();
			if (cls.equals(ProductVersion.class) && 
					f.getName().equals("product") && 
					f.getDeclaredClass().equals(Product.class))
			{
				return true;
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see com.google.gson.ExclusionStrategy#shouldSkipClass(java.lang.Class)
		 */
		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			return false;
		}

	}

}
