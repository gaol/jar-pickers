/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.data.db;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.eap.trackers.data.DataServiceException;
import org.jboss.eap.trackers.model.Artifact;

/**
 * 
 * @author lgao
 * 
 * REST Service which is used to query the artifactIds and groupIds matches.
 * 
 */
@Path("/groupids")
@PermitAll
@ApplicationScoped
public class GroupIdRestService {
	
	@Inject
	private EntityManager em;
	
	@Path("/artifacts/{groupId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response queryArtifactsOfGroupId(@PathParam("groupId") String groupId) throws DataServiceException {
		if (groupId == null || groupId.length() == 0) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		
		List<Artifact> artis = searchArtifactsByGroupId(groupId);
		if (artis.isEmpty()) {
			return Response.status(Status.NOT_FOUND).entity("GroupId: " + groupId + " is not found").build();
		}
		return Response.ok().entity(artis).build();
	}

	@Path("/{artifactId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response queryGroupIdsOfArtifact(@PathParam("artifactId") String artifactId) throws DataServiceException {
		if (artifactId == null || artifactId.length() == 0) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		List<Artifact> artis = searchArtifactsByArtifactId(artifactId);
		if (artis.isEmpty()) {
			return Response.status(Status.NOT_FOUND).entity("ArtifactId: " + artifactId + " is not found").build();
		}
		return Response.ok().entity(artis).build();
	}
	
	private List<Artifact> searchArtifactsByGroupId(String groupId) throws DataServiceException {
		String HQL = "SELECT a FROM Artifact a WHERE a.groupId = :groupId";
		List<Artifact> artis = this.em.createQuery(HQL, Artifact.class)
				.setParameter("groupId", groupId).getResultList();
		return artis;
	}
	
	private List<Artifact> searchArtifactsByArtifactId(String artifactId) throws DataServiceException {
		String HQL = "SELECT a FROM Artifact a WHERE a.artifactId = :artifactId";
		List<Artifact> artis = this.em.createQuery(HQL, Artifact.class)
				.setParameter("artifactId", artifactId).getResultList();
		return artis;
	}
	
}
