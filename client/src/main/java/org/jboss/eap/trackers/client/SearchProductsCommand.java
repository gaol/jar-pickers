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
import org.jboss.eap.trackers.service.ProductsTracker;

/**
 * @author lgao
 *
 */
@CommandDefinition(name="searchproduct", description="Search commands")
public class SearchProductsCommand extends AbstractTrackerCommand {

	@Option(name="prodName", shortName='p', description = "Which product")
	private String prdName;
	
	@Override
	public CommandResult execute(CommandInvocation ci) throws IOException {
		try {
			ProductsTracker tracker = getTracker();
			List<Product> allPrds = tracker.loadAllProducts();
			StringBuilder sb = new StringBuilder();
			Product prd = null;
			if (allPrds == null || allPrds.size() == 0)
			{
				sb.append("No products in data store!");
			}
			else
			{
				if (prdName != null)
				{
					prd = searchProduct(allPrds, prdName);
					if (prd == null)
					{
						sb.append("Product: " + prdName + " is not found!");
					}
					else
					{
						// list single product
						sb.append(productString(prd));
					}
				}
				else
				{
					// list all products
					for (Product p: allPrds)
					{
						sb.append(productString(p));
						sb.append("\n");
					}
				}
			}
			printMessage(ci, sb.toString());
		} catch (DataServiceException e) {
			e.printStackTrace();
			return CommandResult.FAILURE;
		} catch (NamingException e) {
			e.printStackTrace();
			return CommandResult.FAILURE;
		}
		return CommandResult.SUCCESS;
	}

}
