/**
 * 
 */
package org.jboss.eap.trackers.client;

import java.util.Properties;

import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;

/**
 * 
 * Information about remote ejb service
 * <p>
 * NOTE: Only one remote ejb reciver, means one host:port (one node)
 * </p>
 * 
 * @author lgao
 * 
 */
public class RemoteTrackerServiceInfo {

	static volatile RemoteTrackerServiceInfo INSTANCE = new RemoteTrackerServiceInfo();
	
	private Properties ejbProps;
	
	private static final String DOT = ".";
	private static final String KEY_SSL_ENABLED = "remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED";
	private static final String KEY_CONNS = "remote.connections";
	private static final String KEY_CON = "remote.connection";
	private static final String VAL_DEF_CONN = "default";
	private static final String KEY_DEF_HOST = KEY_CON + DOT + VAL_DEF_CONN + DOT + "host";
	private static final String KEY_DEF_PORT = KEY_CON + DOT + VAL_DEF_CONN + DOT + "port";
	private static final String KEY_DEF_USERNAME = KEY_CON + DOT + VAL_DEF_CONN + DOT + "username";
	private static final String KEY_DEF_PASSWORD = KEY_CON + DOT + VAL_DEF_CONN + DOT + "password";
	
	
	private RemoteTrackerServiceInfo() {
		super();
		this.ejbProps = new Properties();
		this.ejbProps.setProperty(KEY_SSL_ENABLED, "false");
		this.ejbProps.setProperty(KEY_CONNS, VAL_DEF_CONN);
	}

	public void setSSLEnabled(boolean enabled) {
		this.ejbProps.setProperty(KEY_SSL_ENABLED, String.valueOf(enabled));
	}

	public void setHost(String host) {
		if (host == null) {
			this.ejbProps.remove(KEY_DEF_HOST);
		}
		else {
			this.ejbProps.setProperty(KEY_DEF_HOST, host);
		}
	}

	public void setPort(String port) {
		if (port == null) {
			this.ejbProps.remove(KEY_DEF_PORT);
		}
		else {
			this.ejbProps.setProperty(KEY_DEF_PORT, port);
		}
	}

	public void setUserName(String username) {
		if (username == null) {
			this.ejbProps.remove(KEY_DEF_USERNAME);
		}
		else {
			this.ejbProps.setProperty(KEY_DEF_USERNAME, username);
		}
	}

	public void setPassword(String password) {
		if (password == null) {
			this.ejbProps.remove(KEY_DEF_PASSWORD);
		}
		else {
			this.ejbProps.setProperty(KEY_DEF_PASSWORD, password);			
		}
	}
	
	public String getHost() {
		return this.ejbProps.getProperty(KEY_DEF_HOST);
	}
	
	public String getPort() {
		return this.ejbProps.getProperty(KEY_DEF_PORT);
	}
	
	public String getUserName() {
		return this.ejbProps.getProperty(KEY_DEF_USERNAME);
	}
	
	/**
	 * Gets current connection uri
	 * 
	 * @return null if not connected.
	 */
	public String getConnectionURI() {
		String host = getHost();
		String portStr = getPort();
		if (host != null && portStr != null) {
			return host + ":" + portStr;
		}
		return null;
	}
	
	public void reload() {
		EJBClientContext.setSelector(new ConfigBasedEJBClientContextSelector(new PropertiesBasedEJBClientConfiguration(this.ejbProps)));
	}
	
}
