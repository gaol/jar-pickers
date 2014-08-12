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
	public QuestionType getQuestionType() {
		return QuestionType.HELP;
	}

	@Override
	public Answer answer() throws Exception {
		Answer answer = new Answer();
		String msg = AnswerMe.QuestionType.HELP_
				+ "\t| " + AnswerMe.QuestionType.ARTIS_OF + " <GROUP_ID>"
				+ "\t| " + AnswerMe.QuestionType.GRP_ID_OF + " <ARTIFACT_ID>"
				+ "\t| " + AnswerMe.QuestionType.ARTIS_VERS_OF + " <ARTIFACT_ID> in <PRD_NAME>:<PRD_VERSION>"
				;
		answer.setAnswer(msg);
		answer.setAnswered(true);
		return answer;
	}

}
