/**
 * 
 */
package org.jboss.eap.trackers.client;

import java.io.IOException;
import java.util.List;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.AeshConsoleBuilder;
import org.jboss.aesh.console.Prompt;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandNotFoundException;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.console.command.registry.AeshCommandRegistryBuilder;
import org.jboss.aesh.console.command.registry.CommandRegistry;
import org.jboss.aesh.console.settings.Settings;
import org.jboss.aesh.console.settings.SettingsBuilder;
import org.jboss.aesh.terminal.Color;
import org.jboss.aesh.terminal.TerminalColor;
import org.jboss.aesh.terminal.TerminalString;

/**
 * @author lgao
 *
 */
public class Main {

	public static void main(String[] args) {
		Main main = new Main();
		main.run();
	}

	
	private void run() {
		startConsole();
	}
	
	private void startConsole() {
		
		Settings settings = new SettingsBuilder().logging(true).enableMan(true).create();
		Prompt prompt = new Prompt(new TerminalString("[client@trackers]$ ",
                new TerminalColor(Color.GREEN, Color.DEFAULT, Color.Intensity.BRIGHT)));
		CommandRegistryHolder holder = new CommandRegistryHolder();
		CommandRegistry registry = new AeshCommandRegistryBuilder()
        .command(ExitCommand.class)
        .command(SearchProductsCommand.class)
        .command(AddProductCommand.class)
        .command(AddProductVersionCommand.class)
        .command(RemoveProductVersionCommand.class)
        .command(new HelpCommand(holder))
        .command(SearchComponentCommand.class)
        .command(AddComponentCommand.class)
        .command(RemoveComponentCommand.class)
        .create();
		holder.registry = registry;
		
        AeshConsole aeshConsole = new AeshConsoleBuilder().settings(settings)
                .prompt(prompt)
                .commandRegistry(registry)
                .create();
        aeshConsole.start();
	}
	
	@CommandDefinition(name="exit", description="Exit the program")
	private static class ExitCommand implements Command<CommandInvocation> {

		@Override
		public CommandResult execute(CommandInvocation arg0) throws IOException {
			arg0.getShell().out().println("Bye!");
			arg0.stop();
			return CommandResult.SUCCESS;
		}
		
	}
	private static class CommandRegistryHolder {
		private CommandRegistry registry;
	}
	
	@CommandDefinition(name="help", description="Print this help. help COMMAND for single command information.")
	private static class HelpCommand implements Command<CommandInvocation> {

		private HelpCommand(){}
		private CommandRegistryHolder registryHolder;
		private HelpCommand(CommandRegistryHolder registryHolder) {
			super();
			this.registryHolder = registryHolder;
		}
		
		CommandRegistry getCommandRegistry() {
			return this.registryHolder.registry;
		}
		
		
		@Arguments(completer = ListCommandsCompleter.class)
		private List<String> cmds;
		
		@Override
		public CommandResult execute(CommandInvocation ci) throws IOException {
			StringBuilder sb = new StringBuilder();
			String cmd = null;
			if (this.cmds != null && this.cmds.size() > 0) {
				cmd = this.cmds.get(0);
			}
			if (cmd != null && cmd.length() > 0) {
				String help = ci.getHelpInfo(cmd);
				sb.append(help);
			} else {
				sb.append("Commmands List:\n");
				for (String cmdName: ci.getCommandRegistry().getAllCommandNames()) {
					try {
						Class<?> cmdCls = ci.getCommandRegistry().getCommand(cmdName, null).getCommand().getClass();
						CommandDefinition cd = cmdCls.getAnnotation(CommandDefinition.class);
						String help = cd.description();
						sb.append("\t" + formatLens(cmdName + ":", 15) + "\t\t" + help + "\n");
					} catch (CommandNotFoundException e) {
						ci.getShell().err().println("Command: " + cmdName + " is not found!");
					}
				}
			}
			ci.getShell().out().println(sb.toString());
			return CommandResult.SUCCESS;
		}
		
		private static class ListCommandsCompleter implements OptionCompleter<CompleterInvocation> {

			@Override
			public void complete(CompleterInvocation completerInvocation) {
				String currentValue = completerInvocation.getGivenCompleteValue();
				HelpCommand helpCommand = (HelpCommand)completerInvocation.getCommand();
				CommandRegistry registry = helpCommand.getCommandRegistry();
				for (String cmdName: registry.getAllCommandNames()) {
					if (cmdName.startsWith(currentValue)) {
						completerInvocation.addCompleterValue(cmdName);
					}
				}
			}
			
		}
		
	}
	
	public static String formatLens(String str, int len) {
		if (str.length() >= len) {
			return str;
		}
		return str + spaces(len - str.length());
	}

	public static String spaces(int i) {
		StringBuilder sb = new StringBuilder();
		for (int d = 0; d< i; d ++)
		{
			sb.append(" ");
		}
		return sb.toString();
	}

}
