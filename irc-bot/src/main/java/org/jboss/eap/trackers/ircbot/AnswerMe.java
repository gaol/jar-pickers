/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.ircbot;

import java.util.regex.Pattern;


/**
 * @author lgao
 *
 */
public interface AnswerMe {
	
	boolean isFullAnswer();
	
	void setFullAnswer(boolean fullAnswer);
	
	String getQuestion();
	
	void setQuestion(String question);
	
	String getRestAPIBase();
	
	void setRestAPIBase(String restAPI);
	
	Answer answer() throws Exception;
	
	void setPattern(Pattern pattern);
	
	Pattern getPattern();

}
