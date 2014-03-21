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
	   
	   Component comp = components.get(0);
	   Assert.assertEquals("jboss-modules", comp.getName());
	   Assert.assertEquals("1.2.2.Final-redhat-1", comp.getVersion());
	   Assert.assertEquals("org.jboss.modules", comp.getGroupId());
	   Assert.assertEquals("https://brewweb.devel.redhat.com/buildinfo?buildID=314067", comp.getBuildInfo());
	   
	   comp = components.get(1);
	   Assert.assertEquals("jgroups", comp.getName());
	   Assert.assertEquals("3.2.10.Final-redhat-2", comp.getVersion());
	   Assert.assertEquals("org.jgroups", comp.getGroupId());
	   Assert.assertEquals("https://brewweb.devel.redhat.com/buildinfo?buildID=314037", comp.getBuildInfo());
   }
   
   @Test
   public void testSaveComponents() throws Exception {
	   Product product = new Product();
	   product.setName("EAP");
	   ProductVersion pv = new ProductVersion();
	   pv.setVersion("6.1.x");
	   List<ProductVersion> pvs = new ArrayList<ProductVersion>();
	   pvs.add(pv);
	   product.setVersions(pvs);
	   
	   Component comp = new Component();
	   comp.setName("jboss-modules");
	   comp.setBuildInfo("https://brewweb.devel.redhat.com/buildinfo?buildID=314078");
	   comp.setVersion("1.2.1.Final-redhat-1");
	   
	   List<Component> comps = this.dataService.saveComponent(pv, comp);
	   Assert.assertNotNull(comps);
	   Assert.assertEquals(1, comps.size());
	   
	   Component c = comps.get(0);
	   Assert.assertEquals("jboss-modules", c.getName());
	   Assert.assertEquals("https://brewweb.devel.redhat.com/buildinfo?buildID=314078", c.getBuildInfo());
	   Assert.assertEquals("1.2.1.Final-redhat-1", c.getVersion());
	   
   }
}
