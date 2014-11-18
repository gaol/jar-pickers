/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.test;

import java.util.regex.Matcher;

import org.jboss.eap.trackers.data.DataService;
import org.jboss.eap.trackers.data.VersionScopes;
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
	public void testVersionRegexp()
	{
	   String ver = "2.3.21";
       Matcher matcher = VersionScopes.VERSION_PATTERN.matcher(ver);
       Assert.assertTrue(matcher.matches());
       Assert.assertEquals(Integer.valueOf(2), Integer.valueOf(matcher.group(1)));
       Assert.assertEquals(Integer.valueOf(3), Integer.valueOf(matcher.group(2)));
       Assert.assertEquals(Integer.valueOf(21), Integer.valueOf(matcher.group(3)));
       Assert.assertNull(matcher.group(4));
       
	   ver = "2.3.21.Final-redhat-4";
	   matcher = VersionScopes.VERSION_PATTERN.matcher(ver);
	   Assert.assertTrue(matcher.matches());
	   Assert.assertEquals(Integer.valueOf(2), Integer.valueOf(matcher.group(1)));
	   Assert.assertEquals(Integer.valueOf(3), Integer.valueOf(matcher.group(2)));
	   Assert.assertEquals(Integer.valueOf(21), Integer.valueOf(matcher.group(3)));
	   Assert.assertEquals(".Final-redhat-4", matcher.group(4));
	   
	   ver = "2.3.21-Final-redhat-4";
	   matcher = VersionScopes.VERSION_PATTERN.matcher(ver);
       Assert.assertTrue(matcher.matches());
       Assert.assertEquals(Integer.valueOf(2), Integer.valueOf(matcher.group(1)));
       Assert.assertEquals(Integer.valueOf(3), Integer.valueOf(matcher.group(2)));
       Assert.assertEquals(Integer.valueOf(21), Integer.valueOf(matcher.group(3)));
       Assert.assertEquals("-Final-redhat-4", matcher.group(4));
       
       ver = "1.0.1_Beta";
       matcher = VersionScopes.VERSION_PATTERN.matcher(ver);
       Assert.assertTrue(matcher.matches());
       Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(matcher.group(1)));
       Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(matcher.group(2)));
       Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(matcher.group(3)));
       Assert.assertEquals("_Beta", matcher.group(4));
       
       ver = "1.0.Beta";
       matcher = VersionScopes.VERSION_PATTERN.matcher(ver);
       Assert.assertTrue(matcher.matches());
       Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(matcher.group(1)));
       Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(matcher.group(2)));
       Assert.assertNull(matcher.group(3));
       Assert.assertEquals("Beta", matcher.group(4));
       
       // some bad versions
       ver = "1.0.1#Beta";
       matcher = VersionScopes.VERSION_PATTERN.matcher(ver);
       Assert.assertFalse(matcher.matches());
       
       ver = "1.0.1Beta,";
       matcher = VersionScopes.VERSION_PATTERN.matcher(ver);
       Assert.assertFalse(matcher.matches());
       
       ver = ".1.0.1Beta";
       matcher = VersionScopes.VERSION_PATTERN.matcher(ver);
       Assert.assertFalse(matcher.matches());
       
       ver = "_1.0.1Beta";
       matcher = VersionScopes.VERSION_PATTERN.matcher(ver);
       Assert.assertFalse(matcher.matches());
       
       ver = "1.0.1Beta_";
       matcher = VersionScopes.VERSION_PATTERN.matcher(ver);
       Assert.assertFalse(matcher.matches());
	}
	
	@Test
	public void testVersionScope() throws Exception {
	   String scope = "<=2.6.5,2.6 :: >=3.0.1";
	   VersionScopes verScope = new VersionScopes(scope);
	   
	   Assert.assertTrue(verScope.isCaptured("2.6.1"));
	   Assert.assertTrue(verScope.isCaptured("2.6.2"));
	   Assert.assertTrue(verScope.isCaptured("2.6.3"));
	   Assert.assertTrue(verScope.isCaptured("2.6.4"));
	   Assert.assertTrue(verScope.isCaptured("2.6.5"));
	   
	   Assert.assertFalse(verScope.isCaptured("2.6.6"));
	   
	   Assert.assertTrue(verScope.isCaptured("3.0.1"));
       Assert.assertTrue(verScope.isCaptured("3.0.2"));
       Assert.assertTrue(verScope.isCaptured("4.0.0"));
       
	   scope = "==2.7.0";
       verScope = new VersionScopes(scope);
	   Assert.assertTrue(verScope.isCaptured("2.7.0"));
	   
	   // only one scope defined
	   scope = ">=3.2.2";
	   verScope = new VersionScopes(scope);
       Assert.assertTrue(verScope.isCaptured("3.2.2"));
       Assert.assertTrue(verScope.isCaptured("3.2.3"));
       Assert.assertTrue(verScope.isCaptured("4.0.0")); // because no series defined, major version will be counted
	   
       Assert.assertFalse(verScope.isCaptured("3.2.1"));
       Assert.assertFalse(verScope.isCaptured("2.6.6"));
       
       // scope with string pre-release
       scope = ">=1.0.27.Final-redhat-2,1.0 :: <=1.1.2.Final-redhat-4,1.1";
       verScope = new VersionScopes(scope);
       
       Assert.assertTrue(verScope.isCaptured("1.0.27.Final-redhat-3"));
       Assert.assertTrue(verScope.isCaptured("1.0.28.Final-redhat-1"));
       Assert.assertTrue(verScope.isCaptured("1.0.27.Final-redhat-10"));
       
       Assert.assertTrue(verScope.isCaptured("1.1.2.Final-redhat-3"));
       Assert.assertTrue(verScope.isCaptured("1.1.2.Final-redhat-2"));
       Assert.assertTrue(verScope.isCaptured("1.1.2.Final-redhat-1"));
       
       Assert.assertFalse(verScope.isCaptured("1.1.2.Final-redhat-5"));
       Assert.assertFalse(verScope.isCaptured("1.1.2.Final-redhat-10"));
       
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalVersionScope1() {
	   String scope = "<=2.6.1,2.7"; // series must is the main stream of the same major or minor version stream.
       new VersionScopes(scope);
       Assert.fail("SHOULD NOT EXECUTE HERE!");
	}
	
	@Test(expected = IllegalArgumentException.class)
    public void testIllegalVersionScope2() {
       String scope = "==2.6.1,2.7"; // when '==' is used, there should NO seriers defined.
       new VersionScopes(scope);
       Assert.fail("SHOULD NOT EXECUTE HERE!");
    }
	
	@Test(expected = IllegalArgumentException.class)
    public void testIllegalVersionScope3() {
       String scope = "<=2.6.,2.7"; // version or series must not ends|starts with dot
       new VersionScopes(scope);
       Assert.fail("SHOULD NOT EXECUTE HERE!");
    }
}
