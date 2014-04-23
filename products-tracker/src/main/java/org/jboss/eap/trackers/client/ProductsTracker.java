/**
 * 
 */
package org.jboss.eap.trackers.client;

import java.util.List;

import org.jboss.eap.trackers.data.DataServiceException;
import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;

/**
 * 
 * @author lgao
 *
 * Remote interface
 * 
 */
public interface ProductsTracker {

	List<Product> loadAllProducts() throws DataServiceException;
	
	List<Product> saveProduct(Product product) throws DataServiceException;
	
	List<Component> loadComponent(String pv) throws DataServiceException;
	
	/**
	 * The first object is ProductVersion
	 * The second object is Component
	 */
	List<Component> saveComponent(List<Object> objs) throws DataServiceException;
	
	
	List<Component> saveComponents(ProductVersion pv, List<Component> components) throws DataServiceException;
}
