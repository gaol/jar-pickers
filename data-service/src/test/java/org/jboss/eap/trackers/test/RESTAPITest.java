package org.jboss.eap.trackers.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.ejb.EJB;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.eap.trackers.data.DataService;
import org.jboss.eap.trackers.data.db.DBDataService;
import org.jboss.eap.trackers.model.Artifact;
import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.resteasy.util.Base64;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
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
public class RESTAPITest {

	@Deployment
	public static Archive<?> createTestArchive() {
		JavaArchive ejb = ShrinkWrap
				.create(JavaArchive.class, "ejb.jar")
				.addAsResource("META-INF/test-persistence.xml",
						"META-INF/persistence.xml")
				.addAsResource("META-INF/orm.xml", "META-INF/orm.xml")
				.addAsResource("import.sql", "import.sql")
				.addPackage(DataService.class.getPackage())
				.addPackage(DBDataService.class.getPackage())
				.addPackage(Product.class.getPackage())
				.addPackage(RESTAPITest.class.getPackage())
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
		return ShrinkWrap.create(WebArchive.class, "test.war")
				.addAsResource("artis.txt", "artis.txt")
				.addAsWebInfResource("WEB-INF/web.xml", "web.xml")
				.addAsLibraries(ejb);
	}

	@EJB
	private DataService dataService;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testReadOnlyGETRESTAPI() throws Exception {
		// list products
		String ctxPath = "http://localhost:8080/test/api/";
		ClientRequest request = new ClientRequest(ctxPath);
		request.accept(MediaType.APPLICATION_JSON_TYPE);
		ClientResponse<List> prodsResp = request.get(List.class, List.class);
		List<Product> prods = prodsResp.getEntity();
		Assert.assertNotNull(prods);
		Assert.assertEquals(3, prods.size());

		// get product
		ctxPath = "http://localhost:8080/test/api/p/EAP";
		request = new ClientRequest(ctxPath);
		request.accept(MediaType.APPLICATION_JSON_TYPE);
		ClientResponse<Product> prdResp = request.get(Product.class);
		Product prd = prdResp.getEntity();
		Assert.assertNotNull(prd);
		Assert.assertEquals("EAP", prd.getName());

		// get versions of EAP
		ctxPath = "http://localhost:8080/test/api/p/vers/EAP";
		request = new ClientRequest(ctxPath);
		request.accept(MediaType.APPLICATION_JSON_TYPE);
		ClientResponse<List> versResp = request.get(List.class, List.class);
		List<String> vers = versResp.getEntity();
		Assert.assertNotNull(vers);
		Assert.assertEquals(8, vers.size());

		// get product version
		ctxPath = "http://localhost:8080/test/api/pv/EAP:6.2.4";
		request = new ClientRequest(ctxPath);
		request.accept(MediaType.APPLICATION_JSON_TYPE);
		ClientResponse<ProductVersion> pvResp = request
				.get(ProductVersion.class);
		ProductVersion pv = pvResp.getEntity();
		Assert.assertNotNull(pv);
		Assert.assertEquals("6.2.4", pv.getVersion());

		// load artifacts of a product version
		ctxPath = "http://localhost:8080/test/api/a/EWP:5.2.0";
		request = new ClientRequest(ctxPath);
		request.accept(MediaType.APPLICATION_JSON_TYPE);
		ClientResponse<List> artisResp = request.get(List.class);
		List<Artifact> artis = (List<Artifact>) artisResp.getEntity();
		Assert.assertNotNull(artis);
		Assert.assertEquals(2, artis.size());

		// get single Artifact
		ctxPath = "http://localhost:8080/test/api/a/org.jboss.as:jboss-as-picketlink:7.2.0.Final-redhat-3";
		request = new ClientRequest(ctxPath);
		request.accept(MediaType.APPLICATION_JSON_TYPE);
		Artifact arti = request.get(Artifact.class).getEntity();
		Assert.assertNotNull(arti);
		Assert.assertEquals("jboss-as-picketlink", arti.getArtifactId());
		Assert.assertEquals("org.jboss.as", arti.getGroupId());
		Assert.assertEquals("7.2.0.Final-redhat-3", arti.getVersion());

		Component artiComp = arti.getComponent();
		Assert.assertNotNull(artiComp);
		Assert.assertEquals("picketlink", artiComp.getName());
		Assert.assertEquals("7.2.0.Final", artiComp.getVersion());
		Assert.assertEquals("https://github.com/jbossas/picketlink",
				artiComp.getScm());

		// guess component
		ctxPath = "http://localhost:8080/test/api/c/org.jboss.as:jboss-as-picketlink:7.2.0.Final-redhat-3";
		request = new ClientRequest(ctxPath);
		request.accept(MediaType.APPLICATION_JSON_TYPE);
		artiComp = request.get(Component.class).getEntity();
		Assert.assertNotNull(artiComp);
		Assert.assertEquals("picketlink", artiComp.getName());
		Assert.assertEquals("7.2.0.Final", artiComp.getVersion());
		Assert.assertEquals("https://github.com/jbossas/picketlink",
				artiComp.getScm());

		// get component by name & version
		ctxPath = "http://localhost:8080/test/api/c/picketlink:7.2.0.Final";
		request = new ClientRequest(ctxPath);
		request.accept(MediaType.APPLICATION_JSON_TYPE);
		artiComp = request.get(Component.class).getEntity();
		Assert.assertNotNull(artiComp);
		Assert.assertEquals("picketlink", artiComp.getName());
		Assert.assertEquals("7.2.0.Final", artiComp.getVersion());
		Assert.assertEquals("https://github.com/jbossas/picketlink",
				artiComp.getScm());
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testUpdateRESTAPI() throws Exception {
		// create a new product
		String ctxPath = "http://localhost:8080/test/api/p";
		ClientRequest request = getClientRequest(ctxPath);
		Product prd = new Product();
		prd.setName("Prod-Name");
		prd.setFullName("This is a test product");
		request.body(MediaType.APPLICATION_JSON_TYPE, prd);
		List<Product> prds = (List<Product>) request.post(List.class)
				.getEntity();
		Assert.assertNotNull(prds);
		Assert.assertEquals(4, prds.size());

		// remove a product
		ctxPath = "http://localhost:8080/test/api/p/Prod-Name";
		request = getClientRequest(ctxPath);
		prds = (List<Product>) request.delete(List.class).getEntity();
		Assert.assertNotNull(prds);
		Assert.assertEquals(3, prds.size());

		// remove product version
		ctxPath = "http://localhost:8080/test/api/pv/EWP:5.2.0";
		request = getClientRequest(ctxPath);
		ClientResponse<Object> resp = request.delete();
		Assert.assertEquals(Status.OK, resp.getResponseStatus());
		List<String> vers = dataService.getVersions("EWP");
		Assert.assertEquals(0, vers.size());

		// add product versions
		ctxPath = "http://localhost:8080/test/api/pv/EWP/5.2.0,5.2.1";
		request = getClientRequest(ctxPath);
		resp = request.put();
		Assert.assertEquals(Status.OK, resp.getResponseStatus());
		vers = dataService.getVersions("EWP");
		 Assert.assertEquals(2, vers.size());
		 Assert.assertTrue(vers.contains("5.2.0"));
		 Assert.assertTrue(vers.contains("5.2.1")); 

		// add artifacts
		ctxPath = "http://localhost:8080/test/api/a/EAP:6.2.4/org.jboss.ironjacamar:ironjacamar-core:1.1.3.Final";
		request = getClientRequest(ctxPath);
		resp = request.put();
		Assert.assertEquals(Status.OK, resp.getResponseStatus());
		List<Artifact> artis = dataService.loadArtifacts("EAP", "6.2.4");
		Assert.assertNotNull(artis);
		Assert.assertEquals(1, artis.size());
		Artifact arti = artis.get(0);
		Assert.assertEquals("org.jboss.ironjacamar", arti.getGroupId());
		Assert.assertEquals("ironjacamar-core", arti.getArtifactId());
		Assert.assertEquals("1.1.3.Final", arti.getVersion());

		// add component
		Component comp = new Component();
		comp.setName("comp-name");
		comp.setVersion("1.1.1");
		ctxPath = "http://localhost:8080/test/api/c";
		request = getClientRequest(ctxPath);
		request.body(MediaType.APPLICATION_JSON_TYPE, comp);
		resp = request.put();
		Assert.assertEquals(Status.OK, resp.getResponseStatus());
		comp = dataService.getComponent("comp-name", "1.1.1");
		Assert.assertNotNull(comp);

		// update artifact build info
		ctxPath = "http://localhost:8080/test/api/ab/org.jboss.ironjacamar:ironjacamar-core:1.1.3.Final";
		request = getClientRequest(ctxPath);
		request.formParameter("buildInfo", "this is the build info");
		resp = request.post();
		Assert.assertEquals(Status.OK, resp.getResponseStatus());
		arti = dataService.getArtifact("org.jboss.ironjacamar",
				"ironjacamar-core", "1.1.3.Final");
		Assert.assertEquals("this is the build info", arti.getBuildInfo());
		
		// update artifacts build info without specifying the artifactId
		ctxPath = "http://localhost:8080/test/api/ab/org.jboss.ironjacamar:1.1.3.Final";
		request = getClientRequest(ctxPath);
		request.formParameter("buildInfo", "this is the build info 2");
		resp = request.post();
		Assert.assertEquals(Status.OK, resp.getResponseStatus());
		arti = dataService.getArtifact("org.jboss.ironjacamar",
				"ironjacamar-core", "1.1.3.Final");
		Assert.assertEquals("this is the build info 2", arti.getBuildInfo());

		// update artifact component
		ctxPath = "http://localhost:8080/test/api/ac/org.jboss.ironjacamar:ironjacamar-core:1.1.3.Final/comp-name:1.1.1";
		request = getClientRequest(ctxPath);
		resp = request.post();
		Assert.assertEquals(Status.OK, resp.getResponseStatus());
		arti = dataService.getArtifact("org.jboss.ironjacamar",
				"ironjacamar-core", "1.1.3.Final");
		Component upComp = arti.getComponent();
		Assert.assertNotNull(upComp);
		Assert.assertEquals("comp-name", upComp.getName());
		Assert.assertEquals("1.1.1", upComp.getVersion());

		// update note of Artifact
		ctxPath = "http://localhost:8080/test/api/n/Artifact-" + arti.getId();
		request = getClientRequest(ctxPath);
		request.formParameter("note", "My Note");
		resp = request.post();
		Assert.assertEquals(Status.OK, resp.getResponseStatus());
		arti = dataService.getArtifact("org.jboss.ironjacamar",
				"ironjacamar-core", "1.1.3.Final");
		Assert.assertEquals("My Note", arti.getNote());

		// remove artifact
		ctxPath = "http://localhost:8080/test/api/a/EAP:6.2.4/org.jboss.ironjacamar:ironjacamar-core:1.1.3.Final";
		request = getClientRequest(ctxPath);
		resp = request.delete();
		Assert.assertEquals(Status.OK, resp.getResponseStatus());
		artis = dataService.loadArtifacts("EAP", "6.2.4");
		Assert.assertEquals(0, artis.size());

		// import artifacts
		URL artisURL = getClass().getClassLoader().getResource("artis.txt");
		Assert.assertNotNull(artisURL);
		ctxPath = "http://localhost:8080/test/api/ai/EAP:6.2.4?url=" + artisURL.toString();
		request = getClientRequest(ctxPath);
		resp = request.put();
		Assert.assertEquals(Status.OK, resp.getResponseStatus());
		List<Artifact> eap624Artis = dataService.loadArtifacts("EAP", "6.2.4");
		Assert.assertNotNull(eap624Artis);
		Assert.assertEquals(355, eap624Artis.size());
		
		// import artifacts by uploading file
		ctxPath = "http://localhost:8080/test/api/aiu/EAP:6.2.3";
		request = getClientRequest(ctxPath);
		MultipartFormDataOutput out = new MultipartFormDataOutput();
		File artisFile = fileFromClassLoaderResource("artis.txt", getClass().getClassLoader());
		out.addFormData("file", artisFile, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		request.body(MediaType.MULTIPART_FORM_DATA_TYPE, out);
		
		resp = request.put();
		Assert.assertEquals(Status.OK, resp.getResponseStatus());
		List<Artifact> eap623Artis = dataService.loadArtifacts("EAP", "6.2.3");
		Assert.assertNotNull(eap623Artis);
		Assert.assertEquals(355, eap623Artis.size());
		
	}
	
	private File fileFromClassLoaderResource(String resource, ClassLoader loader) throws Exception {
		InputStream input = loader.getResourceAsStream(resource);
		if (input == null) {
			return null;
		}
		File tmpFile = File.createTempFile("arq-test", "trackers");
		tmpFile.deleteOnExit();
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(tmpFile);
			byte[] buffer = new byte[4096];
			int length = 0;
			while ((length = input.read(buffer)) != -1) {
				out.write(buffer, 0, length);
			}
		}
		finally {
			input.close();
			out.close();
		}
		
		return tmpFile;
	}

	private ClientRequest getClientRequest(String url) {
		ClientRequest request = new ClientRequest(url);
		request.header("Authorization", getTrackerLogin());
		request.accept(MediaType.APPLICATION_JSON_TYPE);
		return request;
	}

	private Object getTrackerLogin() {
		String userPwd = "test:test1#pwd";
		return "Basic " + new String(Base64.encodeBytes(userPwd.getBytes()));
	}

}
