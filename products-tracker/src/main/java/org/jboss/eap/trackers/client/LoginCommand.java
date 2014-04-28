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
import org.jboss.security.SecurityContextAssociation;

/**
 * @author lgao
 *
 */
@CommandDefinition(name="login", description="Log in to do some update operations")
public class LoginCommand implements Command<CommandInvocation> {
	
	@Override
	public CommandResult execute(CommandInvocation ci)
			throws IOException {
		try {
			if (SecurityContext.isLogged()) {
				ci.getShell().out().println("Has logged already! run logout to login again.");
				return CommandResult.FAILURE;
			}
			
			SecurityContext.login();
			
			String userName = SecurityContextAssociation.getPrincipal().getName();
			char[] password = (char[])SecurityContextAssociation.getCredential();
			RemoteTrackerServiceInfo.INSTANCE.setUserName(userName);
			RemoteTrackerServiceInfo.INSTANCE.setPassword(String.valueOf(password));
			RemoteTrackerServiceInfo.INSTANCE.reload();
			ci.setPrompt(Main.getConsolePrompt());
		} catch (LoginException e) {
			e.printStackTrace();
			return CommandResult.FAILURE;
		}
		return CommandResult.SUCCESS;
	}
	
}
