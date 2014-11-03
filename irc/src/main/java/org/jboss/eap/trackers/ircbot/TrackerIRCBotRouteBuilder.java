package org.jboss.eap.trackers.ircbot;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.irc.IrcChannel;
import org.apache.camel.component.irc.IrcConfiguration;
import org.apache.camel.component.irc.IrcConstants;

/**
 * A Camel Java DSL Router for IRC bot
 */
public class TrackerIRCBotRouteBuilder extends RouteBuilder {
	
	private IrcConfiguration ircConfig;
	private String restAPI;
	
	public TrackerIRCBotRouteBuilder(IrcConfiguration ircConfig, String restAPI) {
		super();
		this.ircConfig = ircConfig;
		this.restAPI = restAPI;
	}

	private IrcConfiguration getIrcConfig() {
		return this.ircConfig;
	}

	private String fromURI() {
		IrcConfiguration config = getIrcConfig();
		if (config == null) {
			throw new IllegalStateException("No IRC configuration information");
		}
		StringBuilder sb = new StringBuilder();
		sb.append("irc://"); // protocol
		sb.append(config.getUsername()); // username
		sb.append("@");
		sb.append(config.getHostname()); // hostname
		if (config.getPorts() != null && config.getPorts().length > 0) {
			sb.append(":"); // ports
			sb.append(config.getPorts()[0]);
		}
		String nickName = config.getNickname() != null ? config.getNickname() : config.getUsername();
		sb.append("?nickname=" + nickName);
		String realName = config.getRealname() != null ? config.getRealname() : config.getUsername();
		sb.append("&realname=" + realName);
		sb.append("&onReply=" + config.isOnReply());
		boolean first = true;
		// channels
		if (config.getChannels() != null && config.getChannels().size() > 0) {
			sb.append("&channels=");
			for (IrcChannel ch: config.getChannels()) {
				if (first) {
					first = false;
				} else {
					sb.append(",");
				}
				sb.append(ch.getName());
			}
		}
		return sb.toString();
	}
	

	/**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
    	TrackerIRCBean ircBean = new TrackerIRCBean(getIrcConfig(), restAPI);
    	from(fromURI())
    	  .choice()
	    	.when(header(IrcConstants.IRC_MESSAGE_TYPE).isEqualTo("PRIVMSG")).bean(ircBean, TrackerIRCBean.METHOD_NAME);
    }
}
