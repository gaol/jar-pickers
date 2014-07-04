/**
 * 
 */
package org.jboss.eap.trackers.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.OptionList;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.eap.trackers.data.DataServiceException;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;
import org.jboss.eap.trackers.service.ProductsTracker;

/**
 * @author lgao
 *
 */
@CommandDefinition(name="addproductversion", description="Add new version to a product.")
public class AddProductVersionCommand extends AbstractTrackerCommand {

	@Option(required = true, name = "prdName")
	private String prdName;
	
	/**
	 * The versions will be added, if some of them are already exist in data store,
	 * just ignored.
	 */
	@OptionList(required = true, name = "versions")
	private List<String> versions;
	
	
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
			if (this.versions != null && this.versions.size() > 0) {
				for (String v: versions) {
					ProductVersion pv = getProductVersion(pvs, v);
					if (pv == null) {
						pv = new ProductVersion();
						pv.setVersion(v);
						pv.setProduct(product);
						if (pvs == null)
						{
							pvs = new ArrayList<ProductVersion>();
						}
						if (!pvs.contains(pv)) {
							pvs.add(pv);
						}
					}
				}
			}
			product.setVersions(pvs);
			tracker.saveProduct(product);
			printMessage(ci, "Versions Added!");
		} catch (DataServiceException e) {
			e.printStackTrace(ci.getShell().err());
			return CommandResult.FAILURE;
		} catch (NamingException e) {
			e.printStackTrace(ci.getShell().err());
			return CommandResult.FAILURE;
		}
		return CommandResult.SUCCESS;
	}

}
