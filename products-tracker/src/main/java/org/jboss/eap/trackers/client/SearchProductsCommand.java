/**
 * 
 */
package org.jboss.eap.trackers.client;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.eap.trackers.data.DataServiceException;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;

/**
 * @author lgao
 *
 */
@CommandDefinition(name="search-product", description="Search commands")
public class SearchProductsCommand implements Command<CommandInvocation> {

	@Option(name="prodName", shortName='p', description = "Which product")
	private String prdName;
	
	@Override
	public CommandResult execute(CommandInvocation ci) throws IOException {
		try {
			ProductsTracker tracker = getTracker();
			List<Product> allPrds = tracker.loadAllProducts();
			StringBuilder sb = new StringBuilder();
			sb.append("Products: ");
			for (Product prd: allPrds)
			{
				if (prdName != null)
				{
					if (prdName.equals(prd.getShortName()))
					{
						sb.append(prd.getShortName() + " ");
						// append versions for single product
						sb.append("[ ");
						for (ProductVersion pv: prd.getVersions())
						{
							sb.append(pv.getVersion() + " ");
						}
						sb.append("]");
						break;
					}
				}
				else
				{
					sb.append(prd.getShortName() + " ");
				}
			}
			System.out.println(sb);
		} catch (DataServiceException e) {
			e.printStackTrace();
			return CommandResult.FAILURE;
		} catch (NamingException e) {
			e.printStackTrace();
			return CommandResult.FAILURE;
		}
		return CommandResult.SUCCESS;
	}

	static ProductsTracker getTracker() throws NamingException {
		final Hashtable<String, String> jndiProperties = new Hashtable<String, String>();
        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        final Context context = new InitialContext(jndiProperties);
        final String appName = "";
        final String moduleName = "products-tracker";
        final String distinctName = "";
        final String beanName = "ProductTrackerImpl";
        final String viewClassName = ProductsTracker.class.getName();
        final String lookUpName = "ejb:" + appName + "/" + moduleName + "/" + distinctName + "/" + beanName + "!" + viewClassName;
		return (ProductsTracker)context.lookup(lookUpName);
	}

}
