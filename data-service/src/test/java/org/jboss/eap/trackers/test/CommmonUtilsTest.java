/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.test;

import org.jboss.eap.trackers.data.DataService;
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
		
		// False matches:
		
		line = "::::";
		Assert.assertFalse(line.matches(DataService.COMP_STR_REGEX));
		
		line = ":antlr::antd::";
		Assert.assertFalse(line.matches(DataService.COMP_STR_REGEX));
		
		line = "antlr::antd::";
		Assert.assertFalse(line.matches(DataService.COMP_STR_REGEX));
		
	}
}
