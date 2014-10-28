package org.jboss.eap.trackers.ircbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.camel.component.irc.IrcConfiguration;
import org.apache.camel.main.Main;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Camel Application
 */
public class MainApp {
	
	private static final Logger LOG = LoggerFactory.getLogger(MainApp.class);
	
	public static final String CONFIG_HOST_NAME = "irc.hostname";
	public static final String DEFAULT_IRC_HOST_NAME = "irc.devel.redhat.com";
	
	public static final String CONFIG_HOST_PORT = "irc.port";
	public static final String CONFIG_USER_NAME = "irc.username";
	public static final String DEFAULT_IRC_USER_NAME = "trackerbot";
	
	public static final String CONFIG_NICK_NAME = "irc.nickname";
	public static final String CONFIG_REAL_NAME = "irc.realname";
	public static final String CONFIG_CHANNELS = "irc.channels";
	public static final List<String> DEFAULT_IRC_CHANNELS = new ArrayList<String>();
	static {
		DEFAULT_IRC_CHANNELS.add("#trackerbot-channel");
	}
	
	public static final String TRACKER_REST_API = "tracker.rest.api";
	public static final String DEFAULT_REST_API = "http://10.66.78.40:8080/trackers/api/";
	
	
	@Option(name = "--config", metaVar = "CONFIG_FILE", aliases = {"-f"}, usage = "Specify the config file. The configuration in the file can be overrided using command line options.")
	private File configFile;
	
	@Option(name = "--hostname", metaVar = "IRC_HOST_NAME", aliases = {"-h"}, usage = "Specify the IRC host name.")
	private String ircHostName;
	
	@Option(name = "--port", metaVar = "IRC_HOST_PORT", aliases = {"-p"}, usage = "Specify the IRC host port.")
	private Integer ircPort;
	
	@Option(name = "--nickname", metaVar = "IRC_NICK_NAME", aliases = {"-n"}, usage = "Specify the IRC nick name.")
	private String ircNickName;
	
	@Option(name = "--username", metaVar = "IRC_USER_NAME", aliases = {"-u"}, usage = "Specify the IRC user name.")
	private String ircUserName;
	
	@Option(name = "--realname", metaVar = "IRC_REAL_NAME", aliases = {"-r"}, usage = "Specify the IRC real name.")
	private String ircRealName;
	
	@Option(name = "--channels", metaVar = "IRC_CHANNELS", aliases = {"-c"}, usage = "Specify the IRC channels.")
	private List<String> ircChannels;
	
	@Option(name = "--api", metaVar = "TRACKER_API_BASE", aliases = {"-a"}, usage = "Specify the tracker REST api base.")
	private String trackerRESTAPIBase;
	
    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) {
    	MainApp mainApp = new MainApp();
    	CmdLineParser cmdParser = new CmdLineParser(mainApp);
    	try {
			cmdParser.parseArgument(args);
		} catch (CmdLineException e) {
			LOG.error("Can't parse command line.", e);
			cmdParser.printUsage(System.out);
			return;
		}
    	Properties config = new Properties();
    	if (mainApp.configFile != null
    			&& mainApp.configFile.exists()
    			&& mainApp.configFile.canRead()) {
    		InputStream input = null;
    		try {
    			input = new FileInputStream(mainApp.configFile);
				config.load(input);
			} catch (IOException e) {
				LOG.error("Can't read from file: " + mainApp.configFile.getAbsolutePath(), e);
				return;
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						
					}
				}
			}
    	}
    	
    	String ircHostName = mainApp.ircHostName;
    	if (ircHostName == null) {
    		ircHostName = config.getProperty(CONFIG_HOST_NAME);
    	}
    	
    	String ircUserName = mainApp.ircUserName;
    	if (ircUserName == null) {
    		ircUserName = config.getProperty(CONFIG_USER_NAME);
    	}
    	
    	Integer ircPort = mainApp.ircPort;
    	if (ircPort == null) {
    		String portStr = config.getProperty(CONFIG_HOST_PORT);
    		if (portStr != null) {
    			ircPort = Integer.valueOf(portStr);
    		}
    	}
    	
    	String ircNickName = mainApp.ircNickName;
    	if (ircNickName == null) {
    		ircNickName = config.getProperty(CONFIG_NICK_NAME);
    	}
    	
    	String ircRealName = mainApp.ircRealName;
    	if (ircRealName == null) {
    		ircRealName = config.getProperty(CONFIG_REAL_NAME);
    	}
    	
    	List<String> channels = mainApp.ircChannels;
    	if (channels == null || channels.isEmpty()) {
    		String CHS = config.getProperty(CONFIG_CHANNELS);
    		if (CHS != null && CHS.length() > 0) {
    			channels = Arrays.asList(CHS.split(","));
    		}
    	}
    	
    	String api = mainApp.trackerRESTAPIBase;
    	if (api == null) {
    		api = config.getProperty(TRACKER_REST_API);
    	}
    	
    	if (ircHostName == null) {
    		LOG.info("IRC Host Name is NOT specified, will use default host name: " + DEFAULT_IRC_HOST_NAME);
    		ircHostName = DEFAULT_IRC_HOST_NAME;
    	}
    	
    	if (ircUserName == null) {
    		LOG.info("IRC User Name is NOT specified, will use default user name: " + DEFAULT_IRC_USER_NAME);
    		ircUserName = DEFAULT_IRC_USER_NAME;
    	}
    	
    	if (ircNickName == null) {
    		LOG.info("IRC Nick Name is NOT speicified, will use the User Name: " + ircUserName + " as the nick name.");
    		ircNickName = ircUserName;
    	}
    	
    	if (ircRealName == null) {
    		LOG.info("IRC Real Name is NOT speicified, will use the User Name: " + ircUserName + " as the nick name.");
    		ircRealName = ircUserName;
    	}
    	
    	if (channels == null || channels.isEmpty()) {
    		LOG.info("IRC channels are NOT specified, will use default channels: " + DEFAULT_IRC_CHANNELS.toString());
    		channels = DEFAULT_IRC_CHANNELS;
    	}
    	
    	if (api == null) {
    		LOG.info("Tracker REST API is NOT specified, will use default API: " + DEFAULT_REST_API);
    		api = DEFAULT_REST_API;
    	}
    	
    	IrcConfiguration ircConfig = new IrcConfiguration();
    	ircConfig.setOnReply(true);
    	ircConfig.setChannel(channels);
    	ircConfig.setHostname(ircHostName);
    	ircConfig.setNickname(ircNickName);
    	if (ircPort != null) {
    		ircConfig.setPorts(new int[]{ircPort});
    	}
    	ircConfig.setRealname(ircRealName);
    	ircConfig.setUsername(ircUserName);
    	TrackerIRCBotRouteBuilder routeBuilder = new TrackerIRCBotRouteBuilder(ircConfig, api);
    	
        Main main = new Main();
        main.enableHangupSupport();
        main.addRouteBuilder(routeBuilder);
        try {
			main.run();
		} catch (Exception e) {
			throw new RuntimeException("Error when start the Camel route.", e);
		}
    }

}

