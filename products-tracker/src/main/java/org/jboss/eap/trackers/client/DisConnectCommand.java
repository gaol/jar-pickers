/**
 * 
 */
package org.jboss.eap.trackers.client;

import java.io.IOException;

import javax.security.auth.login.LoginException;

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

/**
 * @author lgao
 *
 */
@CommandDefinition(name="disconnect", description="DisConnect from remote tracker service.")
public class DisConnectCommand implements Command<CommandInvocation> {
	
	@Override
	public CommandResult execute(CommandInvocation ci)
			throws IOException {
		try {
			SecurityContext.logout(); // logout first!
		} catch (LoginException e) {
			throw new IOException(e);
		} 
		RemoteTrackerServiceInfo.INSTANCE.setHost(null);
		RemoteTrackerServiceInfo.INSTANCE.setPort(null);
		RemoteTrackerServiceInfo.INSTANCE.reload();
		ci.setPrompt(Main.getConsolePrompt());
		return CommandResult.SUCCESS;
	}
	
}
