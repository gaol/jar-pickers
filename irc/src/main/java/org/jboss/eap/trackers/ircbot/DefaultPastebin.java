/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.ircbot;

import java.io.IOException;
import java.net.URI;

import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lgao
 *
 */
public class DefaultPastebin implements Pastebin {

	private static final Logger log = LoggerFactory.getLogger(DefaultPastebin.class);
	
	private String format = "text";
	private String userName;
	private String expiry = "d";
	private URI pastebinURI;
	
	private final static String DEFAULT_USER_NAME = "pastebinBot";
	
	/**
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @param format the format to set
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}


	/**
	 * @return the expiry
	 */
	public String getExpiry() {
		return expiry;
	}

	/**
	 * @param expiry the expiry to set
	 */
	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}


	/**
	 * @return the pastebinURI
	 */
	public URI getPastebinURI() {
		return pastebinURI;
	}

	/**
	 * @param pastebinURI the pastebinURI to set
	 */
	public void setPastebinURI(URI pastebinURI) {
		this.pastebinURI = pastebinURI;
	}


	@Override
	public String pastebin(String message) throws IOException {
		if (message == null) {
			throw new IllegalArgumentException("Wrong argument, message should not be NULL.");
		}
		if (this.pastebinURI == null) {
			throw new IllegalStateException("No Pastebin URI set, please call setPastebinURI(URI uri) method first.");
		}
		String postName = this.userName;
		if (postName == null) {
			postName = DEFAULT_USER_NAME;
		}
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
	        HttpUriRequest postMsg = RequestBuilder.post()
	                .setUri(this.pastebinURI)
	                .addParameter("parent_pid", "")
	                .addParameter("format", this.format)
	                .addParameter("code2", message)
	                .addParameter("poster", postName)
	                .addParameter("expiry", this.expiry)
	                .addParameter("paste", "Send")
	                .build();
			
			CloseableHttpResponse response = httpClient.execute(postMsg);
			try {
				StatusLine line = response.getStatusLine();
				if (line.getStatusCode() == 302) {
					Header locationHeader = response.getFirstHeader("Location");
					if (locationHeader != null) {
						return locationHeader.getValue();
					} else {
						log.error("No Location found!!");
					}
				} else {
					log.error("Not expected response: " + line);
				}
	        } finally {
	        	response.close();
	        }
		} finally {
			httpClient.close();
		}
		return null;
	}
	
}
