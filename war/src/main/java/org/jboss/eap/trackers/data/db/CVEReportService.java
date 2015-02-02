/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.data.db;

import java.util.Date;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.eap.trackers.data.db.CVEReport.AffectedProduct;
import org.jboss.eap.trackers.model.ProductCVE;

/**
 * This is the REST Service place open to HTTP access.
 * 
 * It almost delegates all invocation to the DataService
 *  
 */
@Path("/cvereport")
@PermitAll
@ApplicationScoped
public class CVEReportService
{

   @EJB
   private DataServiceLocal dataService;

   @Inject
   private EntityManager em;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public CVEReport getCVEReport()
   {
      String hql = "SELECT pc FROM " + ProductCVE.class.getSimpleName() + " pc ORDER by pc.cve desc";
      List<ProductCVE> cves = em.createQuery(hql, ProductCVE.class).getResultList();
      CVEReport report = new CVEReport();
      if (cves != null && cves.size() > 0) {
         report.setLatestModified(new Date()); // change it?
         List<CVEReport.AffectedProduct> prds = report.getCveAffectedProducts();
         for (ProductCVE cve: cves) {
            AffectedProduct affecPrd = new CVEReport.AffectedProduct();
            affecPrd.setCve(cve.getCve().getName());
            affecPrd.setBugzilla(cve.getBugzilla());
            affecPrd.setErrata(cve.getErrata());
            affecPrd.setBuild(cve.getBuild());
            affecPrd.setName(cve.getPv().getProduct().getName());
            affecPrd.setNote(cve.getNote());
            affecPrd.setVersion(cve.getPv().getVersion());
            prds.add(affecPrd);
         }
      }
      return report;
   }
   
}
