package org.jboss.eap.trackers.data.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jboss.eap.trackers.data.DataService;
import org.jboss.eap.trackers.data.DataServiceFactory;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProductStoreTest {

	DataService dataService;
	
	@Before
	public void setUp()
	{
		String fileName = this.getClass().getClassLoader().getResource("products.json").getFile();
		System.setProperty("file.data.store.dir", fileName.substring(0, fileName.lastIndexOf(File.separator)));
		this.dataService = DataServiceFactory.createFileDataService();
	}
	
   @Test
   public void testLoadProducts() throws Exception {
	   List<Product> products = this.dataService.loadAllProducts();
	   Assert.assertNotNull(products);
	   Assert.assertEquals(2, products.size());
	   Product prodEAP = products.get(0);
	   Assert.assertNotNull(prodEAP);
	   Assert.assertEquals("EAP", prodEAP.getName());
	   Assert.assertEquals("EAP", prodEAP.getShortName());
	   Assert.assertEquals("JBoss EAP", prodEAP.getDescription());
	   Assert.assertEquals("JBoss Enterprise Application Platform", prodEAP.getFullName());
	   
	   List<ProductVersion> versions = prodEAP.getVersions();
	   Assert.assertNotNull(versions);
	   Assert.assertEquals(1, versions.size());
	   
	   ProductVersion version = versions.get(0);
	   Assert.assertNotNull(version);
	   Assert.assertEquals("6.2.1", version.getVersion());
	   Assert.assertEquals("eap 6.2.1", version.getNote());
//	   Assert.assertEquals(prodEAP, version.getProduct());
	   
	   Product prodEWS = products.get(1);
	   Assert.assertNotNull(prodEWS);
	   Assert.assertEquals("EWS", prodEWS.getName());
	   Assert.assertEquals("EWS", prodEWS.getShortName());
	   Assert.assertEquals("JBoss Enterprise Web Server", prodEWS.getFullName());
	   Assert.assertEquals("JBoss EWS", prodEWS.getDescription());
	   
	   List<ProductVersion> ewsVersions = prodEWS.getVersions();
	   Assert.assertNotNull(ewsVersions);
	   Assert.assertEquals(2, ewsVersions.size());
	   
	   ProductVersion ewsV1 = ewsVersions.get(0);
	   Assert.assertNotNull(ewsV1);
	   Assert.assertEquals(prodEWS, ewsV1.getProduct());
	   Assert.assertEquals("1.0.2", ewsV1.getVersion());
	   Assert.assertEquals("ews 1.0.2", ewsV1.getNote());
	   
	   ProductVersion ewsV2 = ewsVersions.get(1);
	   Assert.assertNotNull(ewsV2);
	   Assert.assertEquals(prodEWS, ewsV2.getProduct());
	   Assert.assertEquals("2.0.1", ewsV2.getVersion());
	   Assert.assertEquals("ews 2.0.1", ewsV2.getNote());
	   
   }
   
   @Test
   public void testSaveProduct() throws Exception
   {
	   Product prod = new Product();
	   prod.setName("New Product");
	   prod.setFullName("new product full name");
	   List<ProductVersion> pvs = new ArrayList<ProductVersion>();
	   ProductVersion pv = new ProductVersion();
	   pv.setProduct(prod);
	   pv.setVersion("1.0.0");
	   pv.setNote("demo note");
	   pvs.add(pv);
	   prod.setVersions(pvs);
	   
	   // add a new product
	   List<Product> products = this.dataService.saveProduct(prod);
	   
	   Assert.assertNotNull(products);
	   Assert.assertEquals(3, products.size());
	   
	   Product prd3 = products.get(2);
	   Assert.assertEquals("New Product", prd3.getName());
	   Assert.assertEquals("new product full name", prd3.getFullName());
	   Assert.assertNull(prd3.getDescription());
	   List<ProductVersion> pvsInPrd3 = prd3.getVersions();
	   Assert.assertEquals(1, pvsInPrd3.size());
	   ProductVersion pv3 = pvsInPrd3.get(0);
	   Assert.assertEquals("1.0.0", pv3.getVersion());
	   Assert.assertEquals("demo note", pv3.getNote());
	   
	   // remove the product version from the new product
	   products = this.dataService.removeProductVersion(prd3, pv3);
	   Assert.assertEquals(3, products.size());
	   prd3 = products.get(2);
	   Assert.assertTrue(prd3.getVersions().isEmpty());
	   
	   // remove the new created product
	   products = this.dataService.removeProduct(prod);
	   Assert.assertNotNull(products);
	   Assert.assertEquals(2, products.size());
	   
	   
   }
   
}
