package org.jboss.eap.trackers.service;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.eap.trackers.data.DataService;
import org.jboss.eap.trackers.data.DataServiceException;
import org.jboss.eap.trackers.data.DataServiceFactory;
import org.jboss.eap.trackers.model.Component;
import org.jboss.eap.trackers.model.Product;
import org.jboss.eap.trackers.model.ProductVersion;

/**
 * 
 * This is application scoped bean, so there is only one instance per JVM.
 * 
 */
//@Path("/products")
//@Stateless
//@Remote(ProductsTracker.class)
//@PermitAll
public class ProductsTrackerImpl 
/*implements ProductsTracker*/ {

//	private final DataService dataService = DataServiceFactory.createFileDataService();
//	
//	@GET
//	@Produces(MediaType.APPLICATION_JSON)
//	@Override
//	public List<Product> loadAllProducts() throws DataServiceException {
//		return dataService.loadAllProducts();
//	}
//
//	@POST
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_JSON)
//	@RolesAllowed("admin")
//	@Override
//	public List<Product> saveProduct(Product product)
//			throws DataServiceException {
//		return dataService.saveProduct(product);
//	}
//
//	@GET
//	@Path("/components/{pv}")
//	@Produces(MediaType.APPLICATION_JSON)
//	@Override
//	public List<Component> loadComponent(@PathParam("pv") String pv)
//			throws DataServiceException {
//		int dashIdx = pv.indexOf("-");
//		if (dashIdx == -1)
//		{
//			throw new RuntimeException("Wrong parameter: " + pv);
//		}
//		String prodName = pv.substring(0, dashIdx);
//		String versionName = pv.substring(dashIdx + 1);
//		ProductVersion pvNeeded = null;
//		for (Product p: loadAllProducts())
//		{
//			if (p.getName().equals(prodName))
//			{
//				if (pvNeeded != null)
//				{
//					break;
//				}
//				for (ProductVersion prodVer: p.getVersions())
//				{
//					if (prodVer.getVersion().equals(versionName))
//					{
//						pvNeeded = prodVer;
//						break;
//					}
//				}
//			}
//		}
//		return dataService.loadComponent(pvNeeded);
//	}
//
//	@POST
//	@Path("/component")
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_JSON)
//	@RolesAllowed("admin")
//	@Override
//	public List<Component> saveComponent(List<Object> objs)
//			throws DataServiceException {
//		ProductVersion pv = (ProductVersion)objs.get(0);
//		Component component = (Component)objs.get(1);
//		return dataService.saveComponent(pv, component);
//	}
//	
//	@RolesAllowed("admin")
//	@Override
//	public List<Component> saveComponents(ProductVersion pv,
//			List<Component> components) throws DataServiceException {
//		return this.dataService.saveComponents(pv, components);
//	}
//	
//	
//	// =========================================================================
//	//         Following are REST only methods.
//	// =========================================================================
//	
//	@GET
//	@Path("/product/{name}")
//	@Produces(MediaType.APPLICATION_JSON)
//	public Product getProduct(String name) throws DataServiceException
//	{
//		List<Product> products = loadAllProducts();
//		if (null != products)
//		{
//			for (Product prod: products)
//			{
//				if (prod.getName().equals(name))
//				{
//					return prod;
//				}
//			}
//		}
//		return null;
//	}
//	
//	
}
