/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.data.db;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.eap.trackers.model.ProductCVE;

/**
 * This is the REST Service place open to HTTP access.
 * 
 * It almost delegates all invocation to the DataService
 * 
 */
@Path("/cvereport2")
@PermitAll
@ApplicationScoped
public class CVEReportServicePro {

    @Inject
    private EntityManager em;

    @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Response getCVEReport()
   {
      String hql = "SELECT pc FROM " + ProductCVE.class.getSimpleName() + " pc ORDER by pc.cve desc";
      List<ProductCVE> cves = em.createQuery(hql, ProductCVE.class).getResultList();

      List<CVEInfo> cveInfos = new ArrayList<CVEInfo>();
      if (cves != null)
      {
         for (ProductCVE cve : cves)
         {
             CVEInfo cveInfo = new CVEInfo();
             cveInfo.setBugzilla(cve.getBugzilla());
             cveInfo.setBugzilla_status(cve.getBugzillaStatus());
             cveInfo.setBuild_nvr(cve.getBuild());
             cveInfo.setCve_name(cve.getCve());
             cveInfo.setErrata(cve.getErrata());
             cveInfo.setFixed_in_version(cve.getFixedIn());
             cveInfo.setProduct_name(cve.getName());
             cveInfo.setProduct_version(cve.getVersion());
             cveInfo.setNote(cve.getNote());
             cveInfos.add(cveInfo);
         }
      }
      return Response.ok(cveInfos, MediaType.APPLICATION_JSON_TYPE).build();
   }

}
