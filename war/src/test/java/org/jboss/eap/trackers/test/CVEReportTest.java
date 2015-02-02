package org.jboss.eap.trackers.test;

import java.io.File;

import javax.ejb.EJB;
import javax.ws.rs.core.MediaType;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.eap.trackers.data.DataService;
import org.jboss.eap.trackers.data.db.CVEReport;
import org.jboss.eap.trackers.data.db.CVEReport.AffectedProduct;
import org.jboss.eap.trackers.data.db.DBDataService;
import org.jboss.eap.trackers.data.db.DataServiceLocal;
import org.jboss.eap.trackers.data.versioning.VersionRanges;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.utils.ArtifactsUtil;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
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
public class CVEReportTest {

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
				.addPackage(CVEReportTest.class.getPackage())
				.addPackage(VersionRanges.class.getPackage())
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

	@Test
	public void testGetCVEReport() throws Exception {
		// CVE Report
		String ctxPath = "http://localhost:8080/test/api/cvereport";
		ClientRequest request = new ClientRequest(ctxPath);
		request.accept(MediaType.APPLICATION_JSON_TYPE);
		ClientResponse<CVEReport> resp = request.get(CVEReport.class, CVEReport.class);
		CVEReport report = resp.getEntity();
		Assert.assertNotNull(report);
		Assert.assertNotNull(report.getCveAffectedProducts());
		Assert.assertEquals(2, report.getCveAffectedProducts().size());
		
		// order by cve name desc
		AffectedProduct affPrd = report.getCveAffectedProducts().get(0);
		Assert.assertEquals("CVE-2014-3566", affPrd.getCve());
		Assert.assertEquals("1182872", affPrd.getBugzilla());
		Assert.assertEquals("123457", affPrd.getErrata());
		Assert.assertEquals("openssl-1.0.1k.el6", affPrd.getBuild());
		Assert.assertEquals("native example", affPrd.getNote());
		Assert.assertEquals("EWS", affPrd.getName());
		Assert.assertEquals("2.1.0", affPrd.getVersion());
		
		affPrd = report.getCveAffectedProducts().get(1);
        Assert.assertEquals("CVE-2014-3547", affPrd.getCve());
        Assert.assertEquals("1182871", affPrd.getBugzilla());
        Assert.assertEquals("123456", affPrd.getErrata());
        Assert.assertEquals("commons-codec-commons-codec-1.4.0.redhat_4-1", affPrd.getBuild());
        Assert.assertEquals("java artifacts example", affPrd.getNote());
        Assert.assertEquals("EAP", affPrd.getName());
        Assert.assertEquals("6.2.4", affPrd.getVersion());
	}

}
