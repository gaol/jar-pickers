/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.data.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.jboss.eap.trackers.data.DataServiceException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

/**
 * 
 * @author lgao
 * 
 * REST Service which is used to query the artifactIds and groupIds matches.
 * 
 * There is a script: `collect_gids.sh` in the tools directory, which can be used
 * to collect groupids for each artifacts from a Maven repository.
 * 
 * This service will read the file on each request, will <b>NOT</b> cache the data.
 * 
 * <p>
 *   The format of the groupids.ini file is:
 * <ul>
 *   <li>Each line represents each artifactId and groupId pair</li>
 *   <li>For Each Line, use format: <b>artifactId = groupId</b></li>
 *   <li>For the duplicated artifactIds with different groupIds, this REST service will list difference
 *     by retrieving the associated `maven-metadata.xml` on versions.</li>
 * </ul>
 * </p>
 */
@Path("/groupids")
@PermitAll
@ApplicationScoped
public class GroupIdRestService {
	
	private final File groupIdFile;
	private final String mvnRepoBase;
	
	private static final String GRP_ID_FILE_KEY = "groupids.file";
	private static final String DFT_GRP_ID_FILE_NAME = "groupids.ini";
	private static final String MVN_REPO_KEY = "groupids.mvn.repo.url";
	private static final String DFT_MVN_REPO = "http://download.devel.redhat.com/brewroot/repos/jb-eap-6-rhel-6-build/latest/maven/";
	
	public GroupIdRestService () {
		// locate groupids file, default at JBoss_AS7_Home/{server}/configuration/groupids.ini
		String groupIdFileLocation = System.getProperty(GRP_ID_FILE_KEY);
		if (groupIdFileLocation == null || groupIdFileLocation.trim().length() == 0) {
			groupIdFileLocation = System.getProperty("jboss.server.config.dir") + File.separator + DFT_GRP_ID_FILE_NAME;
		}
		mvnRepoBase = System.getProperty(MVN_REPO_KEY, DFT_MVN_REPO);
		File tmp = new File(groupIdFileLocation);
		if (tmp.exists() && tmp.canRead()) {
			groupIdFile = tmp;
		} else {
			throw new RuntimeException("Can't find groupIds file, Please define the file location by system property: " + GRP_ID_FILE_KEY);
		}
	}
	
	@Path("/artifacts/{groupId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response queryArtifactsOfGroupId(@PathParam("groupId") String groupId) throws DataServiceException {
		if (groupId == null || groupId.length() == 0) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		try {
			List<String> artiGrpLines = searchArtifactLines(groupId, false);
			List<ArtifactGrp> artiGrpObjs  = composeArtiGrpList(artiGrpLines, false);
			if (artiGrpObjs.size() > 0) {
				ObjectMapper om = new ObjectMapper();
				ArrayNode arrayNode = om.createArrayNode();
				for (ArtifactGrp ag: artiGrpObjs) {
					ObjectNode node = om.createObjectNode();
					node.put("artifactId", ag.getArtifactId());
					arrayNode.add(node);
				}
				return Response.ok().entity(om.writeValueAsString(arrayNode)).build();
			} else {
				// not found
				return Response.status(Status.NOT_FOUND).entity("GroupId: " + groupId + " is not found").build();
			}
		} catch (IOException e) {
			throw new DataServiceException("Exception when read from file: " + this.groupIdFile.getAbsolutePath(), e);
		} catch (JDOMException e) {
			throw new DataServiceException("Exception when parsing versions from maven repository: " + this.mvnRepoBase, e);
		}
	}

	private List<ArtifactGrp> composeArtiGrpList(List<String> artiGrpLines, boolean appendVers) throws IOException, JDOMException {
		List<ArtifactGrp> result = new ArrayList<GroupIdRestService.ArtifactGrp>();
		for (String artiLine: artiGrpLines) {
			String[] array = artiLine.split("=");
			if (array.length < 2) {
				throw new IllegalStateException("Wrong format in groupid file: " + artiLine);
			}
			String arti = array[0];
			if (arti.indexOf(":") != -1) {// in case of artifactId:verPrefix=groupId
				arti = arti.substring(0, arti.indexOf(":"));
			}
			String grp = array[1];
			ArtifactGrp artiGrp = new ArtifactGrp();
			artiGrp.setArtifactId(arti);
			artiGrp.setGroupId(grp);
			if (appendVers) {
				appendVersion(artiGrp, grp, arti);
			}
			if (!result.contains(artiGrp)) {
				result.add(artiGrp);
			}
		}
		return result;
	}

	@Path("/{artifactId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response queryGroupIdsOfArtifact(@PathParam("artifactId") String artifactId) throws DataServiceException {
		if (artifactId == null || artifactId.length() == 0) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		try {
			List<String> artiGrpLines = searchArtifactLines(artifactId, true);
			List<ArtifactGrp> artiGrpObjs = composeArtiGrpList(artiGrpLines, artiGrpLines.size() > 1);
			if (artiGrpObjs.size() > 0) {
				ObjectMapper om = new ObjectMapper();
				ArrayNode arrayNode = om.createArrayNode();
				for (ArtifactGrp ag: artiGrpObjs) {
					ObjectNode node = om.createObjectNode();
					node.put("groupId", ag.getGroupId());
					List<String> versions = ag.getVersions();
					if (versions != null && versions.size() > 0) {
						ArrayNode versionNode = om.createArrayNode();
						for (String ver: versions) {
							versionNode.add(ver);
						}
						node.put("versions", versionNode);
					}
					arrayNode.add(node);
				}
				return Response.ok().entity(om.writeValueAsString(arrayNode)).build();
			} else {
				// not found
				return Response.status(Status.NOT_FOUND).entity("GroupIds of artifact: " + artifactId + " is not found").build();
			}
		} catch (IOException e) {
			throw new DataServiceException("Exception when read from file: " + this.groupIdFile.getAbsolutePath(), e);
		} catch (JDOMException e) {
			throw new DataServiceException("Exception when parsing versions from maven repository: " + this.mvnRepoBase, e);
		}
	}
	
	private void appendVersion(ArtifactGrp ag, String grp, String arti) throws IOException, JDOMException {
		String repoBase = mvnRepoBase.endsWith("/") ? mvnRepoBase : mvnRepoBase + "/";
		URL mvnMetaXMLURL = new URL(repoBase + grp.replace(".", "/") + "/" + arti + "/maven-metadata.xml");
		org.jdom.input.SAXBuilder saxBuilder = new SAXBuilder();
		Document mvnMetaDoc = saxBuilder.build(mvnMetaXMLURL);
		Element rootEle = mvnMetaDoc.getRootElement();
		Element latestEle = (Element)XPath.selectSingleNode(rootEle, "//metadata/versioning/latest");
		Element releaseEle = (Element)XPath.selectSingleNode(rootEle, "//metadata/versioning/release");
		String latest = null;
		String release = null;
		if (latestEle != null) {
			latest = latestEle.getText();
		}
		if (releaseEle != null) {
			release = releaseEle.getText();
		}
		List<String> versions = ag.getVersions();
		if (versions == null) {
			versions = new ArrayList<String>();
			ag.setVersions(versions);
		}
		if (latest != null && release != null) {
			if (latest.equals(release)) {
				versions.add(release);
			}
			else {
				versions.add(latest);
				versions.add(release);
			}
		} else if (latest != null && release == null) {
			versions.add(latest);
		} else  if (release != null && latest == null) {
			versions.add(release);
		}
	}


	private List<String> searchArtifactLines(String artiIdOrGrpId, boolean starts) throws IOException {
		List<String> artiGrpLines = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(groupIdFile)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.replace(" ", ""); // remove all spaces
				if (starts) {
					if (line.toLowerCase().startsWith(artiIdOrGrpId.toLowerCase() + "=")
							|| line.toLowerCase().startsWith(artiIdOrGrpId.toLowerCase() + ":")) {
						artiGrpLines.add(line);
					}
				} else {
					if (line.toLowerCase().endsWith("=" + artiIdOrGrpId.toLowerCase())) {
						artiGrpLines.add(line);
					}
				}
				
			}
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		return artiGrpLines;
	}
	
	public class ArtifactGrp implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private String groupId;
		private String artifactId;
		private List<String> versions;
		/**
		 * @return the groupId
		 */
		public String getGroupId() {
			return groupId;
		}
		/**
		 * @param groupId the groupId to set
		 */
		public void setGroupId(String groupId) {
			this.groupId = groupId;
		}
		/**
		 * @return the artifactId
		 */
		public String getArtifactId() {
			return artifactId;
		}
		/**
		 * @param artifactId the artifactId to set
		 */
		public void setArtifactId(String artifactId) {
			this.artifactId = artifactId;
		}
		/**
		 * @return the versions
		 */
		public List<String> getVersions() {
			return versions;
		}
		/**
		 * @param versions the versions to set
		 */
		public void setVersions(List<String> versions) {
			this.versions = versions;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((artifactId == null) ? 0 : artifactId.hashCode());
			result = prime * result
					+ ((groupId == null) ? 0 : groupId.hashCode());
			return result;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ArtifactGrp other = (ArtifactGrp) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (artifactId == null) {
				if (other.artifactId != null)
					return false;
			} else if (!artifactId.equals(other.artifactId))
				return false;
			if (groupId == null) {
				if (other.groupId != null)
					return false;
			} else if (!groupId.equals(other.groupId))
				return false;
			return true;
		}
		private GroupIdRestService getOuterType() {
			return GroupIdRestService.this;
		}
		
	}
	
}
