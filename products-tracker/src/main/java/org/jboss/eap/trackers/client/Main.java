/**
 * 
 */
package org.jboss.eap.trackers.client;

import java.io.IOException;

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.AeshConsoleBuilder;
import org.jboss.aesh.console.Prompt;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.console.command.registry.AeshCommandRegistryBuilder;
import org.jboss.aesh.console.command.registry.CommandRegistry;
import org.jboss.aesh.console.settings.Settings;
import org.jboss.aesh.console.settings.SettingsBuilder;
import org.jboss.aesh.terminal.Color;
import org.jboss.aesh.terminal.TerminalColor;
import org.jboss.aesh.terminal.TerminalString;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * @author lgao
 *
 */
public class Main {

	private CmdLineParser parser;
	
	@Option(name="help", metaVar="h", usage = "Print Usage.")
	private boolean help;
	
	

	public static void main(String[] args) {
		Main main = new Main();
		CmdLineParser parser = new CmdLineParser(main);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			parser.printUsage(System.out);
			return;
		}
		main.parser = parser;
		main.run();
	}

	
	private void run() {
		if (help) {
			usage();
			return;
		}
		startConsole();
	}
	
	private void usage() {
		if (parser != null)
		{
			parser.printUsage(System.out);
		}
	}

	private void startConsole() {
		
		Settings settings = new SettingsBuilder().logging(true).enableMan(true).create();
		Prompt prompt = new Prompt(new TerminalString("[client@trackers]$ ",
                new TerminalColor(Color.GREEN, Color.DEFAULT, Color.Intensity.BRIGHT)));
		CommandRegistry registry = new AeshCommandRegistryBuilder()
        .command(ExitCommand.class)
        .command(SearchProductsCommand.class)
        .create();
		
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
			System.out.println("Bye!");
			arg0.stop();
			return CommandResult.SUCCESS;
		}
		
	}
	
}
