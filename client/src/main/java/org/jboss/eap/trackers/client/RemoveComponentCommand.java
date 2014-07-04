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
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;
import org.jboss.eap.trackers.service.ProductsTracker;

/**
 * @author lgao
 *
 */
@CommandDefinition(name="removecomponent", description="Remove a component from a product version")
public class RemoveComponentCommand extends AbstractTrackerCommand {

	@Option(required = true, description = "Specify the product name")
	private String prdName;
	
	@Option(required = true, description = "Specify the version of the product")
	private String prdVer;
	
	@Option(required = true, description = "Specify the name of the component")
	private String compName;
	
	@Option(required = true, description = "Specify the version of the component")
	private String compVer;
	
	@Override
	public CommandResult execute(CommandInvocation ci) throws IOException {
		try {
			ProductsTracker tracker = getTracker();
			List<Product> allProducts = tracker.loadAllProducts();
			Product product = searchProduct(allProducts, prdName);
			if (product == null) {
				printMessage(ci, "Product: " + prdName + " is not found!");
				return CommandResult.FAILURE;
			}
			ProductVersion pv = getProductVersion(product.getVersions(), prdVer);
			if (pv == null) {
				printMessage(ci, "There is no version: " + prdVer + " in product: " + prdName + "!");
				return CommandResult.FAILURE;
			}
			List<Component> allComps = tracker.loadComponent(prdName + "-" + prdVer);
			Component comp = searchComp(allComps, compName, compVer);
			StringBuilder sb = new StringBuilder();
			if (comp != null) {
				// remove
				if (allComps.remove(comp)) {
					sb.append("Component Removed!");
					tracker.saveComponents(pv, allComps);
					printMessage(ci, sb.toString());
				}else {
					printMessage(ci, "ERROR: Can't remove the component: " + getComponentString(compName, compVer) + " from product version: " + prdName + ":" + prdVer + "!");
					return CommandResult.FAILURE;
				}
			} else {
				// not found
				printMessage(ci, "No component: " + getComponentString(compName, compVer) + " in product version: " + prdName + ":" + prdVer + "!");
				return CommandResult.FAILURE;
			}
		} catch (NamingException e) {
			e.printStackTrace(ci.getShell().err());
			return CommandResult.FAILURE;
		} catch (DataServiceException e) {
			e.printStackTrace(ci.getShell().err());
			return CommandResult.FAILURE;
		}
		return CommandResult.SUCCESS;
	}
	
	private String getComponentString(String artifactId, String version){
		return artifactId + ":" + version;
	}

}
