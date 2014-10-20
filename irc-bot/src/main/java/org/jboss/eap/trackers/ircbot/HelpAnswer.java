/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.ircbot;

/**
 * @author lgao
 *
 */
public class HelpAnswer extends AbstractAnswer {


	@Override
	public Answer answer() throws Exception {
		Answer answer = new Answer();
		String msg = "help\t| groupid_of <GROUP_ID>"
				+ "\t|  artifactid_of <ARTIFACT_ID>"
				+ "\t|  version_of <ARTIFACT_ID> in <PRD_NAME>:<PRD_VERSION>"
				;
		answer.setAnswer(msg);
		answer.setAnswered(true);
		return answer;
	}

}
