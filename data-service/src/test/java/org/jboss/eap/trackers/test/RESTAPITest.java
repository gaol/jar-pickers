package org.jboss.eap.trackers.test;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.eap.trackers.data.DataService;
import org.jboss.eap.trackers.data.db.DBDataService;
import org.jboss.eap.trackers.model.Product;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test on Product and ProductVersion
 * 
 * @author lgao
 *
 */
@RunWith(Arquillian.class)
public class RESTAPITest {

	
	@Deployment
	   public static Archive<?> createTestArchive() {
		JavaArchive ejb = ShrinkWrap.create(JavaArchive.class, "ejb.jar")
				.addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
	            .addAsResource("META-INF/orm.xml", "META-INF/orm.xml")
	            .addAsResource("import.sql", "import.sql")
	            .addPackage(DataService.class.getPackage())
	            .addPackage(DBDataService.class.getPackage())
	            .addPackage(Product.class.getPackage())
	            .addPackage(RESTAPITest.class.getPackage())
	            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
		
	      return ShrinkWrap.create(WebArchive.class, "test.war")
	            .addAsLibraries(ejb);
	   }
	
   @SuppressWarnings({ "unchecked", "rawtypes" })
   @Test
   public void testRESTAPI() throws Exception {
	   String ctxPath = "http://localhost:8080/test/api/";
	   ClientRequest request = new ClientRequest(ctxPath);
	   request.setHttpMethod("GET");
	   request.accept(MediaType.APPLICATION_JSON_TYPE);
	   ClientResponse<List> prodsResp =request.get(List.class, List.class);
	   List<Product> prods = prodsResp.getEntity();
	   Assert.assertNotNull(prods);
	   Assert.assertEquals(3, prods.size());
   }
   
   
}
