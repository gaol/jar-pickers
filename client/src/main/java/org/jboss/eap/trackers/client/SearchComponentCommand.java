/**
 * 
 */
package org.jboss.eap.trackers.client;

import java.io.IOException;
import java.util.List;

import javax.naming.NamingException;

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.eap.trackers.data.DataServiceException;
import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.service.ProductsTracker;

/**
 * @author lgao
 */
@CommandDefinition(name="searchcomponent", description="Search components of a product.")
public class SearchComponentCommand extends AbstractTrackerCommand {

	@Option(required = true)
	private String prdName;
	
	@Option(required = true)
	private String version;
	
	@Option
	private String filter;
	
	
	@Override
	public CommandResult execute(CommandInvocation ci) throws IOException {
		try {
			ProductsTracker tracker = getTracker();
			String pv = prdName + "-" + version;
			List<Component> components = tracker.loadComponent(pv);
			if (components == null || components.size() == 0) {
				printMessage(ci, "No components found of product version: " + pv);
				return CommandResult.FAILURE;
			}
			StringBuilder sb = new StringBuilder("\t");
			int i = 0;
			int size = 2;
			for (Component comp: components) {
				i ++;
				String name = comp.getName();
				String v = comp.getVersion();
				String str = name + "-" + v;
				if (filter != null && filter.length() > 0) {
					if (!name.contains(filter)) {
						sb.append(formatLens(str, 40));
					}
				} else {
					sb.append(formatLens(str, 40));
				}
				sb.append("\t");
				if  (i % size == 0)
				{
					sb.append("\n\t");
				}
			}
			printMessage(ci, sb.toString());
		} catch (NamingException e) {
			e.printStackTrace(ci.getShell().err());
			return CommandResult.FAILURE;
		} catch (DataServiceException e) {
			e.printStackTrace(ci.getShell().err());
			return CommandResult.FAILURE;
		}
		return CommandResult.SUCCESS;
	}

}
