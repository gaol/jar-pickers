/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.test;

import org.jboss.eap.trackers.data.DataService;
import org.jboss.eap.trackers.data.versioning.VersionRanges;
import org.jboss.eap.trackers.model.ArtifactCVEs;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests on some common utils.
 * 
 * @author lgao
 *
 */
public class CommmonUtilsTest {

	@Test
	public void testArtifactStringRegex() throws Exception{
		String line = "antlr:antlr:2.7.7.redhat-4";
		Assert.assertTrue(line.matches(DataService.ARTI_STR_REGEX));
		
		line = "antlr:antlr:ver";
		Assert.assertTrue(line.matches(DataService.ARTI_STR_REGEX));
		
		line = "antlr:antlr:2.7.7.redhat-4::";
		Assert.assertTrue(line.matches(DataService.ARTI_STR_REGEX));

		line = "antlr:antdlr:2.7.7.redhat-4:";
		Assert.assertTrue(line.matches(DataService.ARTI_STR_REGEX));
		
		
		// False matches:
		
		line = "antrl:antrl-version";
		Assert.assertFalse(line.matches(DataService.ARTI_STR_REGEX));
		
		line = "::::";
		Assert.assertFalse(line.matches(DataService.ARTI_STR_REGEX));
		
		line = ":antlr::antd::";
		Assert.assertFalse(line.matches(DataService.ARTI_STR_REGEX));
		
		line = "antlr::antd::";
		Assert.assertFalse(line.matches(DataService.ARTI_STR_REGEX));
		
		line = "antlr:antd::";
		Assert.assertFalse(line.matches(DataService.ARTI_STR_REGEX));
		
	}
	
	
	@Test
	public void testComponentStringRegex() throws Exception{
		String line = "antlr:2.7.7.redhat-4";
		Assert.assertTrue(line.matches(DataService.COMP_STR_REGEX));
		
		line = "antlr:2.7.7.redhat-4:mygroupId";
		Assert.assertTrue(line.matches(DataService.COMP_STR_REGEX));
		
		line = "antlr:2.7.7.redhat-4:mygroupId:true";
		Assert.assertTrue(line.matches(DataService.COMP_STR_REGEX));
		
		// False matches:
		
		line = "::::";
		Assert.assertFalse(line.matches(DataService.COMP_STR_REGEX));
		
		line = ":antlr::antd::";
		Assert.assertFalse(line.matches(DataService.COMP_STR_REGEX));
		
		line = "antlr::antd::";
		Assert.assertFalse(line.matches(DataService.COMP_STR_REGEX));
		
	}
	
	@Test
	public void testRemoveRedhatSuffix() throws Exception {
		String line = "test-redhat-1";
		Assert.assertEquals("test", line.replaceAll(DataService.RED_HAT_SUFFIX, ""));
		
		line = "2.3.21.Final-redhat-4";
		Assert.assertEquals("2.3.21.Final", line.replaceAll(DataService.RED_HAT_SUFFIX, ""));
	}
	
	@Test
	public void testVersionScope() throws Exception {
	   VersionRanges verRanges = new VersionRanges("2.2.21");
	   
	   Assert.assertTrue(verRanges.isCaptured("2.2.21"));
	   Assert.assertFalse(verRanges.isCaptured("2.2.20"));
	   
	   verRanges = new VersionRanges("[3.2.2,)");
       Assert.assertTrue(verRanges.isCaptured("3.2.2"));
       Assert.assertTrue(verRanges.isCaptured("3.2.3"));
       Assert.assertTrue(verRanges.isCaptured("4.0.0"));
	   
       Assert.assertFalse(verRanges.isCaptured("3.2.1"));
       Assert.assertFalse(verRanges.isCaptured("2.6.6"));
       
       // scope with string pre-release
       verRanges = new VersionRanges("[1.0.27.Final-redhat-2, 1.1.2.Final-redhat-4]");
       
       Assert.assertTrue(verRanges.isCaptured("1.0.27.Final-redhat-3"));
       Assert.assertTrue(verRanges.isCaptured("1.0.27.Final-redhat-10"));
       Assert.assertTrue(verRanges.isCaptured("1.0.28.Final-redhat-1"));
       
       Assert.assertTrue(verRanges.isCaptured("1.1.2.Final-redhat-3"));
       Assert.assertTrue(verRanges.isCaptured("1.1.2.Final-redhat-2"));
       Assert.assertTrue(verRanges.isCaptured("1.1.2.Final-redhat-1"));
       
       Assert.assertFalse(verRanges.isCaptured("1.1.2.Final-redhat-5"));
       Assert.assertFalse(verRanges.isCaptured("1.1.2.Final-redhat-10"));
       
       verRanges = new VersionRanges("(, 7.5.0.Final-redhat-17] : [7.6.3.Final, 7.7.Final-redhat-2]");
       
       Assert.assertTrue(verRanges.isCaptured("7.4.0.Final"));
       Assert.assertTrue(verRanges.isCaptured("7.4.7.Final-redhat-1"));
       Assert.assertTrue(verRanges.isCaptured("7.5.0.Final-redhat-16"));
       Assert.assertTrue(verRanges.isCaptured("7.3.4.Final-redhat-1"));
       
       Assert.assertTrue(verRanges.isCaptured("7.6.3.Final-redhat-1"));
       Assert.assertTrue(verRanges.isCaptured("7.6.3.Final-redhat-2"));
       Assert.assertTrue(verRanges.isCaptured("7.6.4"));
       Assert.assertTrue(verRanges.isCaptured("7.7.Final-redhat-1"));
       Assert.assertTrue(verRanges.isCaptured("7.7.Final-redhat-2"));
      
       Assert.assertFalse(verRanges.isCaptured("7.5.4"));
       Assert.assertFalse(verRanges.isCaptured("7.5.7.Final"));
       Assert.assertFalse(verRanges.isCaptured("7.6.0.Final-redhat"));
       
       Assert.assertFalse(verRanges.isCaptured("7.6.3.beta"));
       Assert.assertFalse(verRanges.isCaptured("7.6.3.alpha"));
       Assert.assertFalse(verRanges.isCaptured("7.6.3.cr1"));
       Assert.assertFalse(verRanges.isCaptured("7.7.Final-redhat-10"));
       
	}
	
	@Test
	public void testArtiCVEsPattern() throws Exception
	{
	   ArtifactCVEs artiCVEs = new ArtifactCVEs();
	   artiCVEs.setIdentifier("java:org.jgroup:jgroup");
	   
	   Assert.assertTrue(artiCVEs.isJavaArtifact());
	   Assert.assertEquals("org.jgroup", artiCVEs.getJavaGroupId());
	   Assert.assertEquals("jgroup", artiCVEs.getJavaArtifactId());
	   
	   artiCVEs.setIdentifier("native:openssl");
	   Assert.assertFalse(artiCVEs.isJavaArtifact());
       Assert.assertEquals("openssl", artiCVEs.getNativeName());
	   
	}

}
