package org.jboss.eap.trackers.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.ejb.EJB;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.eap.trackers.data.DataService;
import org.jboss.eap.trackers.data.db.DBDataService;
import org.jboss.eap.trackers.data.db.DataServiceLocal;
import org.jboss.eap.trackers.model.Artifact;
import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;
import org.jboss.eap.trackers.utils.ArtifactsUtil;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.resteasy.util.Base64;
import org.jboss.resteasy.util.GenericType;
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
				.addClass(ArtifactsUtil.class)
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
		File webInfFile = new File("src/main/webapp/WEB-INF/web.xml");
		return ShrinkWrap.create(WebArchive.class, "test.war")
				.addAsResource("artis.txt", "artis.txt")
				.addAsResource("comps.txt", "comps.txt")
				.addAsWebInfResource(webInfFile, "web.xml")
				.addManifest()
				.addAsLibraries(ejb);
	}

	@EJB
	private DataServiceLocal dataService;

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
		Assert.assertEquals(4, prods.size());

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
		List<Artifact> artis = artisResp.getEntity();
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
		Assert.assertNull(artiComp);

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
		

		// get artifacts list by groupId
		ctxPath = "http://localhost:8080/test/api/groupids/artifacts/org.jboss.ironjacamar";
		request = new ClientRequest(ctxPath);
		request.accept(MediaType.APPLICATION_JSON_TYPE);
		artisResp = request.get(List.class);
		Assert.assertEquals(200, artisResp.getStatus());
		artis = artisResp.getEntity();
		Assert.assertNotNull(artis);
		Assert.assertEquals(2, artis.size());
		
		// get groupId by artifactId
		ctxPath = "http://localhost:8080/test/api/groupids/jsf-impl";
		request = new ClientRequest(ctxPath);
		request.accept(MediaType.APPLICATION_JSON_TYPE);
		artisResp = request.get(List.class);
		Assert.assertEquals(200, artisResp.getStatus());
		artis = artisResp.getEntity();
		Assert.assertNotNull(artis);
		Assert.assertEquals(2, artis.size());
		
		ctxPath = "http://localhost:8080/test/api/groupids/ironjacamar-common-api";
		request = new ClientRequest(ctxPath);
		request.accept(MediaType.APPLICATION_JSON_TYPE);
		artisResp = request.get(List.class);
		Assert.assertEquals(200, artisResp.getStatus());
		artis = artisResp.getEntity();
		Assert.assertNotNull(artis);
		Assert.assertEquals(1, artis.size());
		
		// load All artifacts
		ctxPath = "http://localhost:8080/test/api/a/all";
		request = new ClientRequest(ctxPath);
		request.accept(MediaType.TEXT_PLAIN_TYPE);
		GenericType<StreamingOutput> streamType = new GenericType<StreamingOutput>(){};
		ClientResponse<GenericType<StreamingOutput>> streamingOut = request.get(streamType);
		Assert.assertEquals(200, streamingOut.getStatus());
		String artisList = streamingOut.getEntity(String.class);
		String expectedList = "org.jboss.as:jboss-as-picketlink:7.2.0.Final-redhat-3:jar::0:\n"
				+ "org.jboss.as:jboss-as-security:7.2.0.Final-redhat-3:jar:::\n"
				+ "org.jboss.ironjacamar:ironjacamar-common-api:1.0.3.Final:jar:::\n"
				+ "org.jboss.ironjacamar:ironjacamar-common-api:1.0.2.Final:jar:::\n"
				+ "org.jboss.ironjacamar:ironjacamar-common-impl:1.0.2.Final:jar:::\n"
				+ "org.jboss.ironjacamar:ironjacamar-common-impl:1.0.3.Final:jar:::\n"
				+ "javax.jsf:jsf-impl:1.0.2:jar:::\n"
				+ "com.sun.jsf:jsf-impl:2.0.1:jar:::\n";
		Assert.assertEquals(expectedList, artisList);
		
		// load all components
		ctxPath = "http://localhost:8080/test/api/c/all";
		request = new ClientRequest(ctxPath);
		request.accept(MediaType.TEXT_PLAIN_TYPE);
		streamType = new GenericType<StreamingOutput>(){};
		streamingOut = request.get(streamType);
		Assert.assertEquals(200, streamingOut.getStatus());
		String compList = streamingOut.getEntity(String.class);
		String expectedCompList = "picketlink:7.2.0.Final:org.picketlink\n"
				+ "mod_cluster-native:1.2.9.Final-redhat-1:\n"
				+ "openssl:1.0.1:\n";
		Assert.assertEquals(expectedCompList, compList);
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
		List<Product> prds = request.post(List.class)
				.getEntity();
		Assert.assertNotNull(prds);
		Assert.assertEquals(5, prds.size());

		// remove a product
		ctxPath = "http://localhost:8080/test/api/p/Prod-Name";
		request = getClientRequest(ctxPath);
		prds = request.delete(List.class).getEntity();
		Assert.assertNotNull(prds);
		Assert.assertEquals(4, prds.size());

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
		Assert.assertEquals(3, artis.size());
		Artifact arti = artis.get(2);
		Assert.assertEquals("org.jboss.ironjacamar", arti.getGroupId());
		Assert.assertEquals("ironjacamar-core", arti.getArtifactId());
		Assert.assertEquals("1.1.3.Final", arti.getVersion());

		// add component
		Component comp = new Component();
		comp.setName("comp-name");
		comp.setVersion("1.1.1");
		comp.setGroupId("org.mygroupId");
		ctxPath = "http://localhost:8080/test/api/c";
		request = getClientRequest(ctxPath);
		request.body(MediaType.APPLICATION_JSON_TYPE, comp);
		resp = request.put();
		Assert.assertEquals(Status.OK, resp.getResponseStatus());
		comp = dataService.getComponent("comp-name", "1.1.1");
		Assert.assertNotNull(comp);
		Assert.assertEquals("comp-name", comp.getName());
		Assert.assertEquals("1.1.1", comp.getVersion());
		Assert.assertEquals("org.mygroupId", comp.getGroupId());
		
		// update component groupId
		ctxPath = "http://localhost:8080/test/api/cg/comp-name:1.1.1/New-Group_ID";
		request = getClientRequest(ctxPath);
		request.formParameter("buildInfo", "this is the build info");
		resp = request.post();
		Assert.assertEquals(Status.OK, resp.getResponseStatus());
		comp = dataService.getComponent("comp-name", "1.1.1");
		Assert.assertNotNull(comp);
		Assert.assertEquals("comp-name", comp.getName());
		Assert.assertEquals("1.1.1", comp.getVersion());
		Assert.assertEquals("New-Group_ID", comp.getGroupId());

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
		Assert.assertEquals(2, artis.size());

		// import artifacts
		URL artisURL = getClass().getClassLoader().getResource("artis.txt");
		Assert.assertNotNull(artisURL);
		ctxPath = "http://localhost:8080/test/api/ai/EAP:6.2.4?url=" + artisURL.toString();
		request = getClientRequest(ctxPath);
		resp = request.put();
		Assert.assertEquals(Status.OK, resp.getResponseStatus());
		List<Artifact> eap624Artis = dataService.loadArtifacts("EAP", "6.2.4");
		Assert.assertNotNull(eap624Artis);
		Assert.assertEquals(357, eap624Artis.size());
		
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
		
		// update product version parent, just for test: EAP-6.2.4 based on EAP-6.2.3
		ctxPath = "http://localhost:8080/test/api/pvp/EAP:6.2.4/EAP:6.2.3";
		request = getClientRequest(ctxPath);
		resp = request.post();
		Assert.assertEquals(Status.OK, resp.getResponseStatus());
		ProductVersion eap624 = dataService.getProductVersion("EAP", "6.2.4");
		Assert.assertNotNull(eap624);
		ProductVersion eap623 = eap624.getParent();
		Assert.assertNotNull(eap623);
		Assert.assertEquals("6.2.3", eap623.getVersion());
		
		// import native components to a PV: EAP-6.2.4
		URL compsURL = getClass().getClassLoader().getResource("comps.txt");
		Assert.assertNotNull(compsURL);
		ctxPath = "http://localhost:8080/test/api/ci/EAP:6.2.4?url=" + compsURL.toString();
		request = getClientRequest(ctxPath);
		resp = request.put();
		Assert.assertEquals(Status.OK, resp.getResponseStatus());
		List<Component> eap624Comps = dataService.loadComponents("EAP", "6.2.4");
		Assert.assertNotNull(eap624Comps);
		// eap624 has one predefined in import.sql
		Assert.assertEquals(17, eap624Comps.size());
		
		List<Component> eap624NativeComps = dataService.loadNativeComponents("EAP", "6.2.4");
        Assert.assertNotNull(eap624NativeComps);
        // eap624 has one predefined in import.sql
        Assert.assertEquals(2, eap624NativeComps.size());
		
		// import native components by uploading file to PV: EAP-6.2.3
		ctxPath = "http://localhost:8080/test/api/ciu/EAP:6.2.3";
		request = getClientRequest(ctxPath);
		out = new MultipartFormDataOutput();
		File compsFile = fileFromClassLoaderResource("comps.txt", getClass().getClassLoader());
		out.addFormData("file", compsFile, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		request.body(MediaType.MULTIPART_FORM_DATA_TYPE, out);
		
		resp = request.put();
		Assert.assertEquals(Status.OK, resp.getResponseStatus());
		List<Component> eap623Comps = dataService.loadComponents("EAP", "6.2.3");
		Assert.assertNotNull(eap623Comps);
		Assert.assertEquals(16, eap623Comps.size());
		
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
		String testUserName = System.getProperty("junit.test.username", "test");
		String testPassword = System.getProperty("junit.test.password", "test1#pwd");
		String userPwd = testUserName + ":" + testPassword;
		return "Basic " + new String(Base64.encodeBytes(userPwd.getBytes()));
	}

}
