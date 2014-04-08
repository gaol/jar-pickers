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
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;

/**
 * @author lgao
 *
 */
@CommandDefinition(name="removeproductversion", description="Remove a version from a product.")
public class RemoveProductVersionCommand extends AbstractTrackerCommand {

	@Option(required = true, name = "prdName")
	private String prdName;
	
	@Option(required = true, name = "version")
	private String version;

	@Override
	public CommandResult execute(CommandInvocation ci) throws IOException {
		
		try {
			ProductsTracker tracker = getTracker();
			Product product = searchProduct(tracker.loadAllProducts(), prdName);
			if (product == null)
			{
				printMessage(ci, "Product: " + prdName + " does not exist!");
				return CommandResult.FAILURE;
			}
			List<ProductVersion> pvs = product.getVersions();
			if (pvs != null && pvs.size() > 0) {
				ProductVersion pv = getProductVersion(pvs, version);
				if (pv == null) {
					printMessage(ci, "There is no version: " + version + " of product: " + prdName + "!");
					return CommandResult.FAILURE;
				}
				pvs.remove(pv);
				tracker.saveProduct(product);
				printMessage(ci, "Version Removed!");
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
	
	
}
