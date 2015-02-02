package org.jboss.eap.trackers.test;

import java.io.File;

import javax.ejb.EJB;
import javax.ws.rs.core.MediaType;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.eap.trackers.data.DataService;
import org.jboss.eap.trackers.data.db.CVEReport;
import org.jboss.eap.trackers.data.db.CVEReport.CVEReportElement;
import org.jboss.eap.trackers.data.db.CVEReport.CVEReportSection;
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
				.addAsManifestResource("jboss-deployment-structure.xml")
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
		Assert.assertNotNull(report.getSections());
		Assert.assertEquals(2, report.getSections().size());
		
		// order by cve name desc
		CVEReportSection section = report.getSections().get(0);
		Assert.assertEquals("CVE-2014-3566", section.getCve());
		
		Assert.assertNotNull(section.getElements());
		Assert.assertEquals(1, section.getElements().size());
		
		CVEReportElement element = section.getElements().get(0);
		Assert.assertEquals("1182872", element.getBugzilla());
		Assert.assertEquals("123457", element.getErrata());
		Assert.assertEquals("openssl-1.0.1k.el6", element.getBuild());
		Assert.assertEquals("native example", element.getNote());
		Assert.assertEquals("EWS2", element.getName());
		Assert.assertEquals("2.1.0", element.getVersion());
		
		section = report.getSections().get(1);
        Assert.assertEquals("CVE-2014-3547", section.getCve());
        
        element = section.getElements().get(0);
        Assert.assertNotNull(section.getElements());
        Assert.assertEquals(1, section.getElements().size());
        
        Assert.assertEquals("1182871", element.getBugzilla());
        Assert.assertEquals("123456", element.getErrata());
        Assert.assertEquals("commons-codec-commons-codec-1.4.0.redhat_4-1", element.getBuild());
        Assert.assertEquals("java artifacts example", element.getNote());
        Assert.assertEquals("EAP6", element.getName());
        Assert.assertEquals("6.2.4", element.getVersion());
	}

}
