package org.jboss.eap.trackers.data.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jboss.eap.trackers.data.DataService;
import org.jboss.eap.trackers.data.DataServiceFactory;
import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ComponentStoreTest {

	DataService dataService;
	
	@Before
	public void setUp()
	{
		String fileName = this.getClass().getClassLoader().getResource("products.json").getFile();
		System.setProperty("file.data.store.dir", fileName.substring(0, fileName.lastIndexOf(File.separator)));
		this.dataService = DataServiceFactory.createFileDataService();
	}
	
   @Test
   public void testLoadComponents() throws Exception {
	   
	   Product product = new Product();
	   product.setName("EAP");
	   ProductVersion pv = new ProductVersion();
	   pv.setVersion("6.1.1"); // file does not exist yet.
	   List<ProductVersion> pvs = new ArrayList<ProductVersion>();
	   pvs.add(pv);
	   product.setVersions(pvs);
	   
	   List<Component> components = this.dataService.loadComponent(pv);
	   Assert.assertNull(components);
	   
	   pv.setVersion("6.2.1"); // yes, it exist.
	   components = this.dataService.loadComponent(pv);
	   Assert.assertNotNull(components);
	   
	   Assert.assertEquals(2, components.size());
	   
	   
   }
}
