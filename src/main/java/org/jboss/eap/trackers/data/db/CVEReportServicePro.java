/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.data.db;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

import org.jboss.eap.trackers.model.CVELastUpdated;
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
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    
    @GET()
    @Path("/last_updated")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCVEReportLastUpdated()
    {
       String hql = "SELECT clu FROM " + CVELastUpdated.class.getSimpleName() + " clu WHERE clu.id = 1";
       CVELastUpdated lastUpdated = em.createQuery(hql, CVELastUpdated.class).getSingleResult();
       String time = "Unknown";
       if (lastUpdated != null) {
           // {"last_updated":"2015-02-06 08:13:37 -0500"}
           Timestamp timeStamp = lastUpdated.getLast_updated();
           if (timeStamp != null) {
               time = dateFormat.format(new Date(timeStamp.getTime()));
           }
       }
       String msg = "{\"last_updated\":\"" + time + "\"}";
       return Response.ok(msg, MediaType.APPLICATION_JSON_TYPE).build();
    }

}
