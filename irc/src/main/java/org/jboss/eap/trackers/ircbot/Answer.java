/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.ircbot;

/**
 * @author lgao
 *
 */
public class Answer {

	/**
	 * In case the answered is false, this will be reason why it is NOT answered.
	 */
	private String answer;
	
	/**
	 * post answer to a paste bin in case the answer is too long for IRC message.
	 */
	private String pastebinLink;
	
	/**
	 * Sometime the question gets no answer even I should be able to answer you.
	 * 
	 */
	private boolean answered;
	

	/**
	 * @return the answered
	 */
	public boolean isAnswered() {
		return answered;
	}

	/**
	 * @param answered the answered to set
	 */
	public void setAnswered(boolean answered) {
		this.answered = answered;
	}

	/**
	 * @return the answer
	 */
	public String getAnswer() {
		return answer;
	}

	/**
	 * @param answer the answer to set
	 */
	public void setAnswer(String answer) {
		this.answer = answer;
	}

	/**
	 * @return the pastebinLink
	 */
	public String getPastebinLink() {
		return pastebinLink;
	}

	/**
	 * @param pastebinLink the pastebinLink to set
	 */
	public void setPastebinLink(String pastebinLink) {
		this.pastebinLink = pastebinLink;
	}
	
	
}
