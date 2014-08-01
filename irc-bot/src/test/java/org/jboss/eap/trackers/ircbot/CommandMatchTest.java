/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.ircbot;

import java.util.regex.Matcher;

import org.jboss.eap.trackers.ircbot.AnswerMe.QuestionType;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author lgao
 *
 */
public class CommandMatchTest {

	@Test
	public void testQuestionTypeMatch () {
		String question = "groupid_of ironjacamar-common-api";
		QuestionType type = QuestionType.forName(question);
		Assert.assertNotNull(type);
		Assert.assertEquals(QuestionType.GROUPD_ID_OF, type);
		
		question = " groupid_of   ironjacamar-common-api dddsdf ";
		type = QuestionType.forName(question);
		Assert.assertNotNull(type);
		Assert.assertEquals(QuestionType.GROUPD_ID_OF, type);
		
		question = "artifacts_of org.jboss.ironjacamar";
		type = QuestionType.forName(question);
		Assert.assertNotNull(type);
		Assert.assertEquals(QuestionType.ARTIFACTS_OF, type);
		
		question = " artifacts_of   org.jboss.ironjacamar  ";
		type = QuestionType.forName(question);
		Assert.assertNotNull(type);
		Assert.assertEquals(QuestionType.ARTIFACTS_OF, type);
		
		question = " help ";
		type = QuestionType.forName(question);
		Assert.assertNotNull(type);
		Assert.assertEquals(QuestionType.HELP, type);
		
		question = "version_of ironjacamar-common-api in EAP:6.2.4";
		type = QuestionType.forName(question);
		Assert.assertNotNull(type);
		Assert.assertEquals(QuestionType.VERSION_OF_ARTI_IN_PV, type);
		
		question = " version_of    ironjacamar-common-api       in    EAP:6.2.4   ddd ";
		type = QuestionType.forName(question);
		Assert.assertNotNull(type);
		Assert.assertEquals(QuestionType.VERSION_OF_ARTI_IN_PV, type);
	}
	
	@Test
	public void testFilterQuestionOutGroupIdOf() {
		String question = "groupid_of ironjacamar-common-api";
		Matcher matcher = AnswerMe.PATTERN_GRP_ID_OF.matcher(question);
		Assert.assertTrue(matcher.matches());
		Assert.assertEquals(1, matcher.groupCount());
		Assert.assertNotNull(matcher.group(1));
		Assert.assertEquals("ironjacamar-common-api", matcher.group(1).trim());
		
		question = "groupid_of ironjacamar-common-api privately";
		matcher = AnswerMe.PATTERN_GRP_ID_OF.matcher(question);
		Assert.assertTrue(matcher.matches());
		Assert.assertEquals(1, matcher.groupCount());
		Assert.assertNotNull(matcher.group(1));
		Assert.assertEquals("ironjacamar-common-api", matcher.group(1).trim());
		
		question = " groupid_of ironjacamar-common-api privately "; // space before and end
		matcher = AnswerMe.PATTERN_GRP_ID_OF.matcher(question);
		Assert.assertTrue(matcher.matches());
		Assert.assertEquals(1, matcher.groupCount());
		Assert.assertNotNull(matcher.group(1));
		Assert.assertEquals("ironjacamar-common-api", matcher.group(1).trim());
	}
	
	@Test
	public void testFilterQuestionOutArtifactsOf() {
		String question = "artifacts_of org.jboss.ironjacamar";
		Matcher matcher = AnswerMe.PATTERN_ARTIS_OF.matcher(question);
		Assert.assertTrue(matcher.matches());
		Assert.assertEquals(1, matcher.groupCount());
		Assert.assertNotNull(matcher.group(1));
		Assert.assertEquals("org.jboss.ironjacamar", matcher.group(1).trim());
		
		question = "artifacts_of org.jboss.ironjacamar privately";
		matcher = AnswerMe.PATTERN_ARTIS_OF.matcher(question);
		Assert.assertTrue(matcher.matches());
		Assert.assertEquals(1, matcher.groupCount());
		Assert.assertNotNull(matcher.group(1));
		Assert.assertEquals("org.jboss.ironjacamar", matcher.group(1).trim());
		
		question = " artifacts_of org.jboss.ironjacamar privately ";
		matcher = AnswerMe.PATTERN_ARTIS_OF.matcher(question);
		Assert.assertTrue(matcher.matches());
		Assert.assertEquals(1, matcher.groupCount());
		Assert.assertNotNull(matcher.group(1));
		Assert.assertEquals("org.jboss.ironjacamar", matcher.group(1).trim());
	}
	
	@Test
	public void testFilterQuestionOutArtifactsVersionsInPV() {
		String question = "version_of ironjacamar-common-api in EAP:6.2.4";
		Matcher matcher = AnswerMe.PATTERN_ARTI_VERSION_OF.matcher(question);
		Assert.assertTrue(matcher.matches());
		Assert.assertEquals(3, matcher.groupCount());
		Assert.assertNotNull(matcher.group(1));
		Assert.assertNotNull(matcher.group(2));
		Assert.assertNotNull(matcher.group(3));
		Assert.assertEquals("ironjacamar-common-api", matcher.group(1).trim());
		Assert.assertEquals("EAP", matcher.group(2).trim());
		Assert.assertEquals("6.2.4", matcher.group(3).trim());
		
		question = "version_of ironjacamar-common-api in EAP:6.2.4 privately";
		matcher = AnswerMe.PATTERN_ARTI_VERSION_OF.matcher(question);
		Assert.assertTrue(matcher.matches());
		Assert.assertEquals(3, matcher.groupCount());
		Assert.assertNotNull(matcher.group(1));
		Assert.assertNotNull(matcher.group(2));
		Assert.assertNotNull(matcher.group(3));
		Assert.assertEquals("ironjacamar-common-api", matcher.group(1).trim());
		Assert.assertEquals("EAP", matcher.group(2).trim());
		Assert.assertEquals("6.2.4", matcher.group(3).trim());
		
		question = " version_of ironjacamar-common-api in EAP:6.2.4 privately ";
		matcher = AnswerMe.PATTERN_ARTI_VERSION_OF.matcher(question);
		Assert.assertTrue(matcher.matches());
		Assert.assertEquals(3, matcher.groupCount());
		Assert.assertNotNull(matcher.group(1));
		Assert.assertNotNull(matcher.group(2));
		Assert.assertNotNull(matcher.group(3));
		Assert.assertEquals("ironjacamar-common-api", matcher.group(1).trim());
		Assert.assertEquals("EAP", matcher.group(2).trim());
		Assert.assertEquals("6.2.4", matcher.group(3).trim());
	}
}
