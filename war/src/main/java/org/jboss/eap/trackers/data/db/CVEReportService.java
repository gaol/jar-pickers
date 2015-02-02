/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.data.db;

import java.util.ArrayList;
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

import org.jboss.eap.trackers.data.db.CVEReport.CVEReportElement;
import org.jboss.eap.trackers.data.db.CVEReport.CVEReportSection;
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

      hql = "SELECT distinct pc.cve.name FROM " + ProductCVE.class.getSimpleName() + " pc ORDER by pc.cve desc";
      List<String> distinctCVEs = em.createQuery(hql, String.class).getResultList();
      CVEReport report = new CVEReport();
      if (distinctCVEs != null && distinctCVEs.size() > 0)
      {
         report.setLatestModified(new Date()); // change it?
         List<CVEReport.CVEReportSection> sections = report.getSections();
         for (String cve : distinctCVEs)
         {
            CVEReportSection section = new CVEReport.CVEReportSection();
            section.setCve(cve);
            List<ProductCVE> prdCVEs = getProductCVEs(cves, cve);
            for (ProductCVE prdCVE: prdCVEs) {
               CVEReportElement element = new CVEReport.CVEReportElement();
               element.setBugzilla(prdCVE.getBugzilla());
               element.setBuild(prdCVE.getBuild());
               element.setComponent(prdCVE.getComponent());
               element.setErrata(prdCVE.getErrata());
               element.setName(prdCVE.getName());
               element.setNote(prdCVE.getNote());
               element.setVersion(prdCVE.getVersion());
               section.getElements().add(element);
            }
            sections.add(section);
         }
      }
      return report;
   }

   private List<ProductCVE> getProductCVEs(List<ProductCVE> cves, String cve)
   {
      List<ProductCVE> subs = new ArrayList<ProductCVE>();
      for (ProductCVE prdCVE: cves) {
         if (prdCVE.getCve().getName().equals(cve)) {
            subs.add(prdCVE);
         }
      }
      return subs;
   }

}
