/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.ircbot;

import java.util.regex.Pattern;

/**
 * @author lgao
 *
 */
public abstract class AbstractAnswer implements AnswerMe {

	private String question;
	
	private String restAPIBase;
	
	private boolean fullAnswer;
	
	private Pattern pattern;

	/**
     * @return the pattern
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * @param pattern the pattern to set
     */
    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
	 * @return the fullAnswer
	 */
	public boolean isFullAnswer() {
		return fullAnswer;
	}

	/**
	 * @param fullAnswer the fullAnswer to set
	 */
	public void setFullAnswer(boolean fullAnswer) {
		this.fullAnswer = fullAnswer;
	}

	/**
	 * @return the question
	 */
	public String getQuestion() {
		return question;
	}

	/**
	 * @param question the question to set
	 */
	public void setQuestion(String question) {
		this.question = question;
	}

	/**
	 * @param restAPIBase the restAPIBase to set
	 */
	public void setRestAPIBase(String restAPIBase) {
		this.restAPIBase = restAPIBase;
	}
	
	/**
	 * @return the restAPIBase
	 */
	public String getRestAPIBase() {
		return restAPIBase == null ? null : (restAPIBase.endsWith("/") ? restAPIBase.substring(0, restAPIBase.length() - 1) : restAPIBase);
	}
	
}
