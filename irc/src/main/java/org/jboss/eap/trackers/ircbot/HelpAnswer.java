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
		String msg = "Please see: https://github.com/gaol/trackers/wiki/IRC_Bot_Help";
		answer.setAnswer(msg);
		answer.setAnswered(true);
		return answer;
	}

}
