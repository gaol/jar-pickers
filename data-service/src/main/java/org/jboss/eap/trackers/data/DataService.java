/**
 * 
 */
package org.jboss.eap.trackers.data;

import java.util.List;

import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;

/**
 * @author lgao
 *
 * This is a facade interface for all data operations 
 */
public interface DataService {

	/**
	 * Loads all Product information including all ProductVersion information.
	 * 
	 */
	List<Product> loadAllProducts() throws DataServiceException;
	
	/**
	 * Create or Update a Product and associated the ProductVersion List.
	 * 
	 * Client code must provide full information of the product, including all product version list,
	 * otherwise, the data will be lost.
	 */
	List<Product> saveProduct(Product product) throws DataServiceException;
	
	/**
	 * Removes the product from the data store.
	 * 
	 */
	List<Product> removeProduct(Product product) throws DataServiceException;
	
	/**
	 * Removes a ProductVersion out of the Product.
	 * 
	 */
	List<Product> removeProductVersion(Product product, ProductVersion pv) throws DataServiceException;
	
	/**
	 * Create or Update ProductVersions and associated it to the Product.
	 * 
	 */
	List<Product> saveProductVersions(Product product, List<ProductVersion> pvs) throws DataServiceException;
	
	/**
	 * Loads all components information of the ProductVersion object.
	 */
	List<Component> loadComponent(ProductVersion pv) throws DataServiceException;
	
	/**
	 * Create or Update Components and associated it to the ProductVersion.
	 */
	List<Component> saveComponents(ProductVersion pv, List<Component> components) throws DataServiceException;
	
	/**
	 * Create or Update a single Component and associated it to the ProductVersion.
	 */
	List<Component> saveComponent(ProductVersion pv, Component component) throws DataServiceException;
}