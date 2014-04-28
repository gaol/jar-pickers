/**
 * 
 */
package org.jboss.eap.trackers.client;

import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.eap.trackers.ProductsTracker;
import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;

/**
 * @author lgao
 *
 */
public abstract class AbstractTrackerCommand implements Command<CommandInvocation> {

	protected void printMessage(CommandInvocation ci, String sb) {
		ci.getShell().out().println(sb);
	}

	protected StringBuilder productString(Product prd) {
		StringBuilder sb = new StringBuilder();
		sb.append(prd.toString() + " ");
		// append versions for single product
		sb.append("{ \n");
		int i = 0;
		if (prd.getVersions()  != null)
		{
			for (ProductVersion pv: prd.getVersions())
			{
				i ++;
				sb.append("\t" + formatLens(pv.getVersion(), 15));
				if (i % 4 == 0) {
					sb.append("\n");
				}
			}
		}
		sb.append("\n}");
		return sb;
	}

	protected String formatLens(String str, int len) {
		if (str.length() >= len) {
			return str;
		}
		return str + spaces(len - str.length());
	}

	protected String spaces(int i) {
		StringBuilder sb = new StringBuilder();
		for (int d = 0; d< i; d ++)
		{
			sb.append(" ");
		}
		return sb.toString();
	}

	protected Product searchProduct(List<Product> allPrds, String prdName) {
		for (Product prd: allPrds) {
			if (prd.getName().equals(prdName))
			{
				return prd;
			}
		}
		return null;
	}
	
	protected ProductVersion getProductVersion(List<ProductVersion> pvs,
			String v) {
		if (pvs == null)
			return null;
		for (ProductVersion pv: pvs) {
			if (pv.getVersion().equals(v)) {
				return pv;
			}
		}
		return null;
	}
	

	protected Component searchComp(List<Component> allComps, String groupId,
			String compName, String compVer) {
		if (allComps == null) {
			return null;
		}
		for (Component comp: allComps) {
			if (comp.getName().equals(compName) && comp.getVersion().equals(compVer)) {
				if (groupId != null && comp.getGroupId() != null && groupId.equals(comp.getGroupId())) {
					return comp;
				} else if (groupId == null && comp.getGroupId() == null) {
					return comp;
				}
			}
		}
		return null;
	}

	protected ProductsTracker getTracker() throws NamingException {
		final Hashtable<String, String> jndiProperties = new Hashtable<String, String>();
        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        final Context context = new InitialContext(jndiProperties);
        final String appName = "";
        final String moduleName = "products-tracker";
        final String distinctName = "";
        final String beanName = "ProductsTrackerImpl";
        final String viewClassName = ProductsTracker.class.getName();
        final String lookUpName = "ejb:" + appName + "/" + moduleName + "/" + distinctName + "/" + beanName + "!" + viewClassName;
        try {
        	return (ProductsTracker)context.lookup(lookUpName);
        } catch (NamingException e) {
        	if (e.getMessage().indexOf("EJBCLIENT000025") != -1) {
        		throw new NamingException("Please connect to remote server first!");
        	}
        	throw e;
        }
	}
}
