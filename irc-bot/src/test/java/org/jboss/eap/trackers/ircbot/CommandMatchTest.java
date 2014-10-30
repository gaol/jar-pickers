/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.ircbot;

import java.util.regex.Matcher;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author lgao
 *
 */
public class CommandMatchTest {

    @Test
    public void testQuestionTypeMatch() {
        String question = "groupid_of ironjacamar-common-api";
        AnswerMe answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);

        Assert.assertNotNull(answerMe);
        Assert.assertEquals(GroupIdAnswer.class, answerMe.getClass());

        question = " groupid of  ironjacamar-common-api dddsdf ";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);

        Assert.assertNotNull(answerMe);
        Assert.assertEquals(GroupIdAnswer.class, answerMe.getClass());

        question = " groupid_of   ironjacamar-common-api dddsdf ";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);

        Assert.assertNotNull(answerMe);
        Assert.assertEquals(GroupIdAnswer.class, answerMe.getClass());

        question = "artifacts_of org.jboss.ironjacamar";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);

        Assert.assertNotNull(answerMe);
        Assert.assertEquals(ArtifactsAnswer.class, answerMe.getClass());

        question = "artifacts of org.jboss.ironjacamar";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);

        Assert.assertNotNull(answerMe);
        Assert.assertEquals(ArtifactsAnswer.class, answerMe.getClass());
        
        question = " artifacts_of   org.jboss.ironjacamar  ";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);

        Assert.assertNotNull(answerMe);
        Assert.assertEquals(ArtifactsAnswer.class, answerMe.getClass());

        question = "help";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);

        Assert.assertNotNull(answerMe);
        Assert.assertEquals(HelpAnswer.class, answerMe.getClass());

        question = "version_of ironjacamar-common-api in EAP:6.2.4";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);

        Assert.assertNotNull(answerMe);
        Assert.assertEquals(ArtifactsVersionAnswer.class, answerMe.getClass());
        
        question = "version of ironjacamar-common-api in EAP:6.2.4";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);

        Assert.assertNotNull(answerMe);
        Assert.assertEquals(ArtifactsVersionAnswer.class, answerMe.getClass());

        question = " version_of    ironjacamar-common-api       in    EAP:6.2.4   ddd ";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);

        Assert.assertNotNull(answerMe);
        Assert.assertEquals(ArtifactsVersionAnswer.class, answerMe.getClass());
    }

    @Test
    public void testFilterQuestionOutGroupIdOf() {
        String question = "groupid_of ironjacamar-common-api";
        AnswerMe answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        Matcher matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(1, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertEquals("ironjacamar-common-api", matcher.group(1).trim());

        question = "groupid_of ironjacamar-common-api privately";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(1, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertEquals("ironjacamar-common-api", matcher.group(1).trim());
        
        question = "groupid of ironjacamar-common-api privately";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(1, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertEquals("ironjacamar-common-api", matcher.group(1).trim());

        question = " groupid_of ironjacamar-common-api privately "; // space before and end
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(1, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertEquals("ironjacamar-common-api", matcher.group(1).trim());
    }

    @Test
    public void testFilterQuestionOutArtifactsOf() {
        String question = "artifacts_of org.jboss.ironjacamar";
        AnswerMe answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        Matcher matcher = answerMe.getPattern().matcher(question);
        
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(1, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertEquals("org.jboss.ironjacamar", matcher.group(1).trim());

        question = "artifacts_of org.jboss.ironjacamar privately";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(1, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertEquals("org.jboss.ironjacamar", matcher.group(1).trim());
        
        question = "artifacts of org.jboss.ironjacamar privately";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(1, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertEquals("org.jboss.ironjacamar", matcher.group(1).trim());

        question = " artifacts_of org.jboss.ironjacamar privately ";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(1, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertEquals("org.jboss.ironjacamar", matcher.group(1).trim());
    }

    @Test
    public void testFilterQuestionOutArtifactsVersionsInPV() {
        String question = "version_of ironjacamar-common-api in EAP:6.2.4";
        AnswerMe answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        Matcher matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(3, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertNotNull(matcher.group(2));
        Assert.assertNotNull(matcher.group(3));
        Assert.assertEquals("ironjacamar-common-api", matcher.group(1).trim());
        Assert.assertEquals("EAP", matcher.group(2).trim());
        Assert.assertEquals("6.2.4", matcher.group(3).trim());

        question = "version_of ironjacamar-common-api in EAP:6.2.4 privately";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(3, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertNotNull(matcher.group(2));
        Assert.assertNotNull(matcher.group(3));
        Assert.assertEquals("ironjacamar-common-api", matcher.group(1).trim());
        Assert.assertEquals("EAP", matcher.group(2).trim());
        Assert.assertEquals("6.2.4", matcher.group(3).trim());
        
        question = "version of ironjacamar-common-api in EAP:6.2.4 privately";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(3, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertNotNull(matcher.group(2));
        Assert.assertNotNull(matcher.group(3));
        Assert.assertEquals("ironjacamar-common-api", matcher.group(1).trim());
        Assert.assertEquals("EAP", matcher.group(2).trim());
        Assert.assertEquals("6.2.4", matcher.group(3).trim());

        question = " version_of ironjacamar-common-api in EAP:6.2.4 privately ";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(3, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertNotNull(matcher.group(2));
        Assert.assertNotNull(matcher.group(3));
        Assert.assertEquals("ironjacamar-common-api", matcher.group(1).trim());
        Assert.assertEquals("EAP", matcher.group(2).trim());
        Assert.assertEquals("6.2.4", matcher.group(3).trim());
    }
    
    
    @Test
    public void testCVEs() throws Exception {
        String question = "cves_of EAP:6.2.4";
        AnswerMe answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        Matcher matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(2, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertNotNull(matcher.group(2));
        Assert.assertEquals("EAP", matcher.group(1).trim());
        Assert.assertEquals("6.2.4", matcher.group(2).trim());
        
        question = "cves_of openssl:1.0.1";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(2, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertNotNull(matcher.group(2));
        Assert.assertEquals("openssl", matcher.group(1).trim());
        Assert.assertEquals("1.0.1", matcher.group(2).trim());
        
        question = "cves_of ironjacamar:1.0.27.Final";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(2, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertNotNull(matcher.group(2));
        Assert.assertEquals("ironjacamar", matcher.group(1).trim());
        Assert.assertEquals("1.0.27.Final", matcher.group(2).trim());
        
        question = "cves of ironjacamar-common-impl:1.0.27.Final";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(2, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertNotNull(matcher.group(2));
        Assert.assertEquals("ironjacamar-common-impl", matcher.group(1).trim());
        Assert.assertEquals("1.0.27.Final", matcher.group(2).trim());
        
        question = "artifacts of CVE-2014-3657";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(1, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertEquals("CVE-2014-3657", matcher.group(1).trim());
        
        question = "artifacts_of CVE-2014-3657";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(1, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertEquals("CVE-2014-3657", matcher.group(1).trim());
        
        question = "components of CVE-2014-3657";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(1, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertEquals("CVE-2014-3657", matcher.group(1).trim());
        
        question = "components_of CVE-2014-3657";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(1, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertEquals("CVE-2014-3657", matcher.group(1).trim());
        
        question = "products of CVE-2014-3657";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(1, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertEquals("CVE-2014-3657", matcher.group(1).trim());
        
        question = "products_of CVE-2014-3657";
        answerMe = AnswerMeLoader.INSTANCE.getAnswerMeBySentence(question);
        Assert.assertNotNull(answerMe);
        matcher = answerMe.getPattern().matcher(question);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(1, matcher.groupCount());
        Assert.assertNotNull(matcher.group(1));
        Assert.assertEquals("CVE-2014-3657", matcher.group(1).trim());
    }
}
