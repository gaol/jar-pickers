package org.jboss.eap.trackers.test;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.eap.trackers.data.DataService;
import org.jboss.eap.trackers.data.DataServiceException;
import org.jboss.eap.trackers.data.db.DBDataService;
import org.jboss.eap.trackers.model.Artifact;
import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * Test on Product and ProductVersion
 * 
 * @author lgao
 *
 */
@RunWith(Arquillian.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProductVersionDataTest {
	
   @Deployment
   public static Archive<?> createTestArchive() {
      return ShrinkWrap.create(JavaArchive.class, "test.jar")
            .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
            .addAsResource("META-INF/orm.xml", "META-INF/orm.xml")
            .addAsResource("artis.txt", "artis.txt")
            .addAsResource("import.sql", "import.sql")
            .addPackage(DataService.class.getPackage())
            .addPackage(DBDataService.class.getPackage())
            .addPackage(Product.class.getPackage())
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
   }

   @EJB
   DataService dataService;

   @Inject
   Logger log;

   @Test
   public void testLoadProducts() throws Exception {
	   List<Product> allProds = dataService.loadAllProducts();
	   Assert.assertNotNull(allProds);
	   Assert.assertEquals(3, allProds.size());
	   
	   // EAP
	   Product prd = allProds.get(0);
	   Assert.assertNotNull(prd);
	   Assert.assertEquals("EAP", prd.getName());
	   Assert.assertEquals("JBoss Enterprise Application Platform", prd.getFullName());
	   Assert.assertEquals("EAP product seria", prd.getDescription());
	   List<ProductVersion> pvs = prd.getVersions();
	   Assert.assertNotNull(pvs);
	   Assert.assertEquals(8, pvs.size());
	   for (ProductVersion pv: pvs) {
		   Assert.assertNotNull(pv);
		   if (pv.getId().intValue() == 0) {
			   Assert.assertEquals("5.2.0", pv.getVersion());
		   } 
		   else if (pv.getId().intValue() == 1) {
			   Assert.assertEquals("6.0.1", pv.getVersion());
		   }
		   else if (pv.getId().intValue() == 2) {
			   Assert.assertEquals("6.1.1", pv.getVersion());
		   }
		   else if (pv.getId().intValue() == 3) {
			   Assert.assertEquals("6.2.0", pv.getVersion());
		   }
		   else if (pv.getId().intValue() == 4) {
			   Assert.assertEquals("6.2.2", pv.getVersion());
		   }
		   else if (pv.getId().intValue() == 5) {
			   Assert.assertEquals("6.2.3", pv.getVersion());
		   }
		   else if (pv.getId().intValue() == 6) {
			   Assert.assertEquals("6.2.3.ER4", pv.getVersion());
		   }
		   else if (pv.getId().intValue() == 7) {
			   Assert.assertEquals("6.2.4", pv.getVersion());
		   }
		   else
		   {
			   throw new RuntimeException("Unkown ProductVersion id: " + pv.getId());
		   }
	   }
	   
	   // EWS
	   prd = allProds.get(1);
	   Assert.assertNotNull(prd);
	   Assert.assertEquals("EWS", prd.getName());
	   Assert.assertEquals("JBoss Enterprise Web Server", prd.getFullName());
	   Assert.assertEquals("EWS product seria", prd.getDescription());
	   pvs = prd.getVersions();
	   Assert.assertNotNull(pvs);
	   Assert.assertEquals(2, pvs.size());
	   for (ProductVersion pv: pvs) {
		   Assert.assertNotNull(pv);
		   if (pv.getId().intValue() == 8) {
			   Assert.assertEquals("2.0.1", pv.getVersion());
		   }
		   else if (pv.getId().intValue() == 9) {
			   Assert.assertEquals("2.1.0", pv.getVersion());
		   }
	   }
	   
	   // EWP
	   prd = allProds.get(2);
	   Assert.assertNotNull(prd);
	   Assert.assertEquals("EWP", prd.getName());
	   Assert.assertEquals("JBoss Enterprise Web Platform", prd.getFullName());
	   Assert.assertEquals("EWP product seria", prd.getDescription());
	   pvs = prd.getVersions();
	   Assert.assertNotNull(pvs);
	   Assert.assertEquals(1, pvs.size());
	   ProductVersion pv = pvs.get(0);
	   Assert.assertNotNull(pv);
	   Assert.assertEquals("5.2.0", pv.getVersion());
    }
   
   @Test
   public void testUpdateProduct() throws Exception {
	   List<Product> prods = dataService.loadAllProducts();
	   Assert.assertEquals(3, prods.size()); 
	   
	   // save a new product
	   Product prod = new Product();
	   prod.setName("New-Prod");
	   prod.setFullName("Test-Full-Name");
	   prod.setDescription("Desc");
	   
	   dataService.saveProduct(prod);
	   prods = dataService.loadAllProducts();
	   Assert.assertEquals(4, prods.size());
	   Product prodDB = prods.get(3);
	   Assert.assertNotNull(prodDB);
	   Assert.assertEquals(prod, prodDB);
	   Assert.assertEquals("New-Prod", prodDB.getName());
	   Assert.assertEquals("Test-Full-Name", prodDB.getFullName());
	   Assert.assertEquals("Desc", prodDB.getDescription());
	   
	   // add some versions for FK check
	   Set<String> verSet = new HashSet<String>();
	   verSet.add("2.2.0");
	   verSet.add("2.3.0");
	   dataService.addProductVersions("New-Prod", verSet);
	   
	   // remove the new product
	   dataService.removeProduct("New-Prod"); // this should delete all versions together
	   prods = dataService.loadAllProducts();
	   Assert.assertEquals(3, prods.size());
	   
	   // update a product
	   Product firstProd = prods.get(0);
	   Long id = firstProd.getId();
	   String newName = "This is my New Name";
	   firstProd.setName(newName);
	   dataService.saveProduct(firstProd);
	   
	   Product newPrd = dataService.getProductByName(newName);
	   Assert.assertNotNull(newPrd);
	   Assert.assertEquals(id, newPrd.getId());
	   Assert.assertEquals(8, newPrd.getVersions().size()); // EAP versions are not affected.
	   
	   // change it back
	   newPrd.setName("EAP");
	   dataService.saveProduct(newPrd);
   }
   
   @Test
   public void testUpdateProductVersion() throws Exception {
	   Product prod = dataService.getProductByName("EWS");
	   Assert.assertNotNull(prod);
	   // 2.0.1 and 2.1.0
	   List<String> ewsVersions = dataService.getVersions("EWS");
	   Assert.assertNotNull(ewsVersions);
	   Assert.assertEquals(2, ewsVersions.size());
	   Assert.assertTrue(ewsVersions.contains("2.0.1"));
	   Assert.assertTrue(ewsVersions.contains("2.1.0"));
	   
	   // add new versions
	   Set<String> verSet = new HashSet<String>();
	   verSet.add("2.2.0");
	   verSet.add("2.3.0");
	   dataService.addProductVersions("EWS", verSet);
	   
	   ewsVersions = dataService.getVersions("EWS");
	   Assert.assertNotNull(ewsVersions);
	   Assert.assertEquals(4, ewsVersions.size());
	   Assert.assertTrue(ewsVersions.contains("2.0.1"));
	   Assert.assertTrue(ewsVersions.contains("2.1.0"));
	   Assert.assertTrue(ewsVersions.contains("2.2.0"));
	   Assert.assertTrue(ewsVersions.contains("2.3.0"));
	   
	   // remove 2.2.0
	   dataService.removeProductVersion("EWS", "2.2.0");
	   ewsVersions = dataService.getVersions("EWS");
	   Assert.assertNotNull(ewsVersions);
	   Assert.assertEquals(3, ewsVersions.size());
	   Assert.assertTrue(ewsVersions.contains("2.0.1"));
	   Assert.assertTrue(ewsVersions.contains("2.1.0"));
	   Assert.assertTrue(ewsVersions.contains("2.3.0"));
	   
	   // add already existed versions
	   verSet = new HashSet<String>();
	   verSet.add("2.2.0");
	   verSet.add("2.1.0"); // this one exited already
	   
	   try
	   {
		   dataService.addProductVersions("EWS", verSet);
		   Assert.fail("Can't access here.");
	   } catch (DataServiceException e) {
		   Assert.assertEquals("Version: 2.1.0 has already in product: EWS", e.getMessage());
		   ewsVersions = dataService.getVersions("EWS");
		   Assert.assertNotNull(ewsVersions);
		   Assert.assertEquals(3, ewsVersions.size());
		   Assert.assertTrue(ewsVersions.contains("2.0.1"));
		   Assert.assertTrue(ewsVersions.contains("2.1.0"));
		   Assert.assertTrue(ewsVersions.contains("2.3.0"));
	   }
	   
   }
   
   
   
   /**
    * Makes it run at last
    */
   @Test
   public void testzArtifacts() throws Exception {
	   
	   // adds Artifact to EWS:2.1.0
	   dataService.addArtifact("EWS", "2.1.0", "org.apache.tomcat", "bootstrap", "6.0.37");
	   dataService.addArtifact("EWS", "2.1.0", "org.apache.tomcat", "catalina", "6.0.37");
	   
	   // then check the artifacts
	   List<Artifact> artifacts = dataService.loadArtifacts("EWS", "2.1.0");
	   Assert.assertNotNull(artifacts);
	   Assert.assertEquals(2, artifacts.size());
	   Artifact arti = artifacts.get(0);
	   Assert.assertEquals("org.apache.tomcat", arti.getGroupId());
	   
	   // update artifact build information to all artifacts in groupId
	   dataService.updateArtifactBuildInfo("org.apache.tomcat", null, "6.0.37", "build url in MEAD");
	   arti = dataService.getArtifact("org.apache.tomcat", "bootstrap", "6.0.37");
	   Assert.assertEquals("build url in MEAD", arti.getBuildInfo());
	   arti = dataService.getArtifact("org.apache.tomcat", "catalina", "6.0.37");
	   Assert.assertEquals("build url in MEAD", arti.getBuildInfo());
	   
	   // update artifact build information to single artifact
	   dataService.updateArtifactBuildInfo("org.apache.tomcat", "bootstrap", "6.0.37", "another build url in MEAD");
	   arti = dataService.getArtifact("org.apache.tomcat", "bootstrap", "6.0.37");
	   Assert.assertEquals("another build url in MEAD", arti.getBuildInfo());
	   arti = dataService.getArtifact("org.apache.tomcat", "catalina", "6.0.37");
	   Assert.assertNotEquals("another build url in MEAD", arti.getBuildInfo());
	   
	   Component newComp = new Component();
	   newComp.setName("tomcat6");
	   newComp.setVersion("6.0.37");
	   dataService.saveComponent(newComp);
	   
	   // update artifact component to all artifacts in groupId
	   dataService.updateArtifactComponent("org.apache.tomcat", null, "6.0.37", "tomcat6", "6.0.37");
	   arti = dataService.getArtifact("org.apache.tomcat", "bootstrap", "6.0.37");
	   Component artiComp = arti.getComponent();
	   Assert.assertNotNull(artiComp);
	   Assert.assertEquals("tomcat6", artiComp.getName());
	   Assert.assertEquals("6.0.37", artiComp.getVersion());
	   arti = dataService.getArtifact("org.apache.tomcat", "catalina", "6.0.37");
	   artiComp = arti.getComponent();
	   Assert.assertNotNull(artiComp);
	   Assert.assertEquals("tomcat6", artiComp.getName());
	   Assert.assertEquals("6.0.37", artiComp.getVersion());
	   
	   // update component
	   artiComp.setDescription("Update description to tomcat6");
	   dataService.saveComponent(artiComp);
	   artiComp = dataService.getComponent("tomcat6", "6.0.37");
	   Assert.assertEquals("Update description to tomcat6", artiComp.getDescription());
	   
	   // update note
	   dataService.updateNote(arti.getId(), "Artifact", "Note for Artifact");
	   arti = dataService.getArtifact("org.apache.tomcat", "catalina", "6.0.37");
	   Assert.assertEquals("Note for Artifact", arti.getNote());
	   
	   dataService.removeArtifacts("EWS", "2.1.0", "org.apache.tomcat", null, "6.0.37");
	   artifacts = dataService.loadArtifacts("EWS", "2.1.0");
	   Assert.assertNotNull(artifacts);
	   Assert.assertEquals(0, artifacts.size());
	   
	   // guess component
	   Component comp = dataService.guessComponent("org.jboss.as", null, "7.2.0.Final-redhat-3");
	   Assert.assertNotNull(comp);
	   Assert.assertEquals("picketlink", comp.getName());
	   Assert.assertEquals("7.2.0.Final", comp.getVersion());
	   
	   
	   //import artifacts to EAP 6.2.4
	   URL artisURL = getClass().getClassLoader().getResource("artis.txt");
	   Assert.assertNotNull(artisURL);
	   dataService.importArtifacts("EAP", "6.2.4", artisURL);
	   List<Artifact> eap624Artis = dataService.loadArtifacts("EAP", "6.2.4");
	   Assert.assertNotNull(eap624Artis);
	   Assert.assertEquals(355, eap624Artis.size());
	   
	   
	   // EWP delete
	   List<Artifact> artis = dataService.loadArtifacts("EWP", "5.2.0");
	   Assert.assertNotNull(artis);
	   Assert.assertEquals(2, artis.size());
	   
	   // try to delete product version with foreign keys
	   dataService.removeProductVersion("EWP", "5.2.0");
	   artis = dataService.loadArtifacts("EWP", "5.2.0");
	   Assert.assertNotNull(artis);
	   Assert.assertEquals(0, artis.size());
   }
   
   
}
