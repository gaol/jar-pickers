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
@CommandDefinition(name="logout", description="Log out, but keep connection status")
public class LogoutCommand implements Command<CommandInvocation> {
	
	@Override
	public CommandResult execute(CommandInvocation ci)
			throws IOException {
		try {
			SecurityContext.logout();
			RemoteTrackerServiceInfo.INSTANCE.setUserName(null);
			RemoteTrackerServiceInfo.INSTANCE.setPassword(null);
			RemoteTrackerServiceInfo.INSTANCE.reload();
			ci.setPrompt(Main.getConsolePrompt());
			return CommandResult.SUCCESS;
		} catch (LoginException e) {
			ci.getShell().err().println("Logout Error: " + e.getMessage());
			return CommandResult.FAILURE;
		}
	}
	
}
