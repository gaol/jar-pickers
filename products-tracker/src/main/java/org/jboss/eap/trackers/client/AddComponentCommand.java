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
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.eap.trackers.data.DataServiceException;
import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;

/**
 * @author lgao
 *
 */
@CommandDefinition(name="addcomponent", description="Add a component to a product version")
public class AddComponentCommand extends AbstractTrackerCommand {

	@Option(required = true, description = "Specify the product name")
	private String prdName;
	
	@Option(required = true, description = "Specify the version of the product")
	private String prdVer;
	
	@Option(description = "Specify groupId of the component")
	private String groupId;
	
	@Option(required = true, description = "Specify the name of the component")
	private String compName;
	
	@Option(required = true, description = "Specify the version of the component")
	private String compVer;
	
	@Option(description = "Specify the brew build information of the component")
	private String buildinfo;
	
	@Option(description = "Specify which dist-git package this component belongs to")
	private String pkg;
	
	@Option(description = "Specify the scm url of the component")
	private String scm;
	
	@Option(description = "Specify the description of the component")
	private String description;
	
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
			Component comp = searchComp(allComps, groupId, compName, compVer);
			StringBuilder sb = new StringBuilder();
			if (comp != null) {
				// update
				if (this.buildinfo != null) {
					comp.setBuildInfo(buildinfo);
				}
				if (this.description != null) {
					comp.setDescription(description);
				}
				if (this.pkg != null) {
					comp.setPkg(pkg);
				}
				if (this.scm != null) {
					comp.setScm(scm);
				}
				sb.append("Component Updated!");
			} else {
				// create
				comp = new Component();
				comp.setBuildInfo(buildinfo);
				comp.setDescription(description);
				comp.setGroupId(groupId);
				comp.setName(compName);
				comp.setPkg(pkg);
				comp.setScm(scm);
				comp.setVersion(compVer);
				sb.append("Component Created!");
			}
			List<Object> params = new ArrayList<Object>();
			params.add(pv);
			params.add(comp);
			tracker.saveComponent(params);
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
