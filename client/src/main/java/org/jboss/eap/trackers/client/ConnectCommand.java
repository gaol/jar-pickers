/**
 * 
 */
package org.jboss.eap.trackers.client;

import java.io.IOException;
import java.util.List;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.validator.OptionValidator;
import org.jboss.aesh.cl.validator.OptionValidatorException;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.console.command.validator.AeshValidatorInvocation;

/**
 * @author lgao
 *
 */
@CommandDefinition(name="connect", description="Connect to remote tracker service, default connect to localhost:8080")
public class ConnectCommand implements Command<CommandInvocation> {
	
	@Arguments(description = "Specify the remote service to connect to. Default to localhost:8080", defaultValue = "localhost:8080", validator = HostAndPortOptionValidator.class)
	private List<String> hostAndPorts;
	
	@Override
	public CommandResult execute(CommandInvocation ci)
			throws IOException {
		String hostAndPort = hostAndPorts.get(0);
		String host = hostAndPort.substring(0, hostAndPort.indexOf(":"));
		String port = hostAndPort.substring(hostAndPort.indexOf(":") + 1);
		RemoteTrackerServiceInfo.INSTANCE.setHost(host);
		RemoteTrackerServiceInfo.INSTANCE.setPort(port);
		RemoteTrackerServiceInfo.INSTANCE.reload();
		ci.setPrompt(Main.getConsolePrompt());
		return CommandResult.SUCCESS;
	}
	
	private class HostAndPortOptionValidator implements OptionValidator<AeshValidatorInvocation> {

		@Override
		public void validate(AeshValidatorInvocation validatorInvocation)
				throws OptionValidatorException {
			Object obj = validatorInvocation.getValue();
			if (obj == null) {
				throw new OptionValidatorException("host:port must be specified.");
			}
			if (obj.getClass().isAssignableFrom(List.class))
			{
				throw new OptionValidatorException("Wrong Type!");
			}
			String hostAndPort = obj.toString();
			if (hostAndPort.indexOf(":") == -1) {
				throw new OptionValidatorException("Wrong format of host:port");
			}
			try {
				Integer.valueOf(hostAndPort.substring(hostAndPort.indexOf(":") + 1));
			} catch (NumberFormatException nfe) {
				throw new OptionValidatorException("Wrong format of host:port, port must be a number.");
			}
		}
		
	}
	
}
