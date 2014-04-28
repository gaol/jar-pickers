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
import org.jboss.eap.trackers.ProductsTracker;
import org.jboss.eap.trackers.data.DataServiceException;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;

/**
 * @author lgao
 *
 */
@CommandDefinition(name="addproduct", description="Add a new product. All the information will be added or update if --update specified.")
public class AddProductCommand extends AbstractTrackerCommand {

	@Option(required = true, name = "name")
	private String name;
	
	@Option(required = true, name = "fullName")
	private String fullName;
	
	@Option(name = "description")
	private String description;
	
	@OptionList(name = "versions")
	private List<String> versions;
	
	@Option(name = "update", description = "force to update if product name exist already!", hasValue = false)
	private boolean update;
	
	@Override
	public CommandResult execute(CommandInvocation ci) throws IOException {
		try {
			ProductsTracker tracker = getTracker();
			List<Product> allProducts = tracker.loadAllProducts();
			Product product = searchProduct(allProducts, name);
			if (product != null && ! update) {
				printMessage(ci, "Product: " + name + " was added already!");
				printMessage(ci, "Use --update to force update!");
				return CommandResult.FAILURE;
			}
			if (product == null) { // added only
				Product productPopulated = populateProduct();
				tracker.saveProduct(productPopulated);
				printMessage(ci, "Product " + this.name + " Added!");
			} else { // merge and update
				Product productPopulated = mergeProduct(populateProduct(), product);
				tracker.saveProduct(productPopulated);
				printMessage(ci, "Product " + this.name + " Updated!");
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

	/**
	 * If both have versions information, command should provide complete versions, otherwise, some versions in data store will be removed.
	 * Add/remove version only, use addproductversion/removeproductversion commands.
	 */
	static Product mergeProduct(Product populateProduct/*populated*/, Product product/*in data store*/) {
		if (!populateProduct.getName().equals(product.getName()))
		{
			throw new IllegalArgumentException("2 product should have same product name");
		}
		if (!populateProduct.getFullName().equals(product.getFullName()))
		{
			throw new IllegalArgumentException("2 product should have same product full name");
		}
		Product prd = new Product();
		prd.setName(populateProduct.getName());
		prd.setFullName(populateProduct.getFullName());
		prd.setDescription(populateProduct.getDescription() != null ? populateProduct.getDescription() : product.getDescription());
		if (populateProduct.getVersions() != null)
		{
			prd.setVersions(populateProduct.getVersions());
		}
		else {
			prd.setVersions(product.getVersions());
		}
		return prd;
	}

	private Product populateProduct() {
		Product prod = new Product();
		prod.setName(name);
		prod.setDescription(description);
		prod.setFullName(fullName);
		if (this.versions != null)
		{
			List<ProductVersion> pvs = new ArrayList<ProductVersion>();
			for (String v: this.versions) {
				ProductVersion pv = new ProductVersion();
				pv.setProduct(prod);
				pv.setVersion(v);
				pvs.add(pv);
			}
			prod.setVersions(pvs);
		}
		return prod;
	}

}
