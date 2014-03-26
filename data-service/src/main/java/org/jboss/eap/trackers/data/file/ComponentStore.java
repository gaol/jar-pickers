/**
 * 
 */
package org.jboss.eap.trackers.data.file;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author lgao
 * 
 * ComponentStore stores component list for a specific ProductVersion in a separate file.
 * 
 * <p>
 *   File Name is: &lt;PROD-NAME&gt;-&lt;PROD-VERSION&gt;.json within the data store directory.
 * </p>
 * 
 * <p>
 *   The format of the file is:
 *   <pre>
 *     {
 *       "name": "PROD-NAME",
 *       "version": "PROD-VERSION",
 *       "components": [
 *          "name": "",
 *          "version": "",
 *          "buildInfo": "",
 *          "groupId":"",
 *          "scm":"",
 *          "pkg":""
 *       ]
 *     }
 *   </pre>
 * </p>
 * 
 */
public class ComponentStore {
	
	private final File dir;

	private ConcurrentHashMap<String, FileLock> componentFiles = new ConcurrentHashMap<String, FileLock>();
	
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	ComponentStore(File dir) throws IOException {
		super();
		this.dir = dir;
	}
	
	private FileLock initComponentStore(ProductVersion pv) throws IOException {
		Product product = pv.getProduct();
		if (product == null)
		{
			throw new IllegalArgumentException("Product of the product version: " + pv + " is null.");
		}
		String fileName = product.getName() + "-" + pv.getVersion() + ".json";
		FileLock compFile = componentFiles.get(fileName);
		if (compFile == null)
		{
			compFile = initStoreFile(fileName);
			componentFiles.putIfAbsent(fileName, compFile);
		}
		return compFile;
	}

	private FileLock initStoreFile(String fileName) throws IOException {
		File compFile = new File(dir, fileName);
		if (!compFile.exists() && !compFile.createNewFile())
		{
			throw new IllegalStateException("Can't init component store file: " + compFile.getAbsolutePath());
		}
		return new FileLock(new Object(), compFile);
	}
	
	private class FileLock {
		private final Object lock;
		private final File file;
		FileLock(Object lock, File file)
		{
			super();
			this.lock = lock;
			this.file = file;
		}
	}
	
	
	List<Component> loadAllComponents(ProductVersion pv) throws IOException {
		FileLock fileLock = initComponentStore(pv);
		synchronized (fileLock.lock) {
			Reader reader = null;
			try
			{
				reader = new FileReader(fileLock.file);
				ProductVersionComponents pvComps = gson.fromJson(reader, ProductVersionComponents.class);
				if (pvComps != null)
				{
					return pvComps.getComponents();
				}
				return null;
			}
			finally
			{
				if (reader != null)
				{
					reader.close();
				}
				fileLock.lock.notify();
			}
		}
	}

	public List<Component> saveComponents(ProductVersion pv,
			List<Component> components) throws IOException {
		FileLock fileLock = initComponentStore(pv);
		synchronized (fileLock.lock) {
			ProductVersionComponents pvComps = new ProductVersionComponents();
			pvComps.setComponents(components);
			pvComps.setName(pv.getProduct().getName());
			pvComps.setVersion(pv.getVersion());
			String json = gson.toJson(pvComps);
			
			FileWriter writer = null;
			try
			{
				writer = new FileWriter(fileLock.file);
				writer.write(json);
				writer.flush();
			}
			finally
			{
				if (writer != null)
				{
					writer.close();
				}
				fileLock.lock.notify();
				
			}
		}
		return components;
	}
	
	
	
}
