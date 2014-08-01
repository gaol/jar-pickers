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
		String msg = "COMMANDS LIST:  "
				+ "\t  " + AnswerMe.QuestionType.HELP_
				+ "\t| " + AnswerMe.QuestionType.ARTIS_OF
				+ "\t| " + AnswerMe.QuestionType.GRP_ID_OF
				+ "\t| " + AnswerMe.QuestionType.ARTIS_VERS_OF
				;
		answer.setAnswer(msg);
		answer.setAnswered(true);
		return answer;
	}

}
