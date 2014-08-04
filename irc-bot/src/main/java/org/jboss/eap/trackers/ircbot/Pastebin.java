/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.ircbot;

import java.io.IOException;

/**
 * @author lgao
 *
 * The interface which can be used to post a message to a <code>pastebin</code> and return the link.
 *
 */
public interface Pastebin {

	
	/**
	 * Post the message to pastebin, and return the link to the posted message.
	 * 
	 * @param message the message to post
	 * @return the link of the posted message
	 * @throws IOException any I/O exception
	 */
	String pastebin(String message) throws IOException;
}
