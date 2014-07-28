/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.data.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
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

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jboss.eap.trackers.data.DataServiceException;
import org.w3c.dom.Document;

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
		File tmp = new File(groupIdFileLocation);
		if (tmp.exists() && tmp.canRead()) {
			groupIdFile = tmp;
		}
		mvnRepoBase = System.getProperty(MVN_REPO_KEY, DFT_MVN_REPO);
		throw new RuntimeException("Can't find groupIds file, Please define the file location by system property: " + GRP_ID_FILE_KEY);
	}
	
	
	@Path("/{artifactId}")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response queryGroupIdsOfArtifact(@PathParam("artifactId") String artifactId) throws DataServiceException {
		if (artifactId == null || artifactId.length() == 0) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		StringBuilder sb = new StringBuilder("[");
		try {
			List<String> artiGrpLines = searchArtifactLines(artifactId);
			boolean multiple = artiGrpLines.size() > 1;
			for (String artiLine: artiGrpLines) {
				String[] array = artiLine.split("=");
				String arti = array[0];
				if (arti.indexOf(":") != -1) {// in case of artifactId:verPrefix=groupId
					arti = arti.substring(0, arti.indexOf(":"));
				}
				String grp = array[1];
				sb.append(grp);
				sb.append(":");
				sb.append(arti);
				if(multiple) {
					try {
						appendVersion(sb, grp, arti);
					} catch (DocumentException e) {
						throw new DataServiceException("Can't get versions information.", e);
					}
				}
				sb.append("\n");
			}
		} catch (IOException e) {
			throw new DataServiceException("Exception when read from file: " + this.groupIdFile.getAbsolutePath(), e);
		}
		sb.append("]");
		return Response.ok().entity(sb.toString()).build();
	}
	
	private void appendVersion(StringBuilder sb, String grp, String arti) throws IOException, DocumentException {
		String repoBase = mvnRepoBase.endsWith("/") ? mvnRepoBase : mvnRepoBase + "/";
		URL mvnMetaXMLURL = new URL(repoBase + grp.replace(".", "/") + "/" + arti + "/maven-metadata.xml");
		SAXReader reader = new SAXReader();
		org.dom4j.Document doc = reader.read(mvnMetaXMLURL);
		Element metaData = doc.getRootElement();
		Element versioningEle = (Element)metaData.elementIterator("versioning").next();
		String latest = null;
		String release = null;
		for(Iterator<Element> iterator = versioningEle.elementIterator();iterator.hasNext();){  
            Element ele = (Element) iterator.next();
            if (ele.getName().equals("latest")) {
            	latest = ele.getText();
            } else if (ele.getName().equals("release")) {
            	release = ele.getText();
            }
        }
		sb.append("[");
		if (latest != null && release != null) {
			if (latest.equals(release)) {
				sb.append(latest);
			}
			else {
				sb.append(latest);
				sb.append(",");
				sb.append(release);
			}
		} else if (latest != null && release == null) {
			sb.append(latest);
		} else  if (release != null && latest == null) {
			sb.append(release);
		}
		sb.append("]");
	}


	private List<String> searchArtifactLines(String artifactId) throws IOException {
		List<String> artiGrpLines = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(groupIdFile)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.toLowerCase().startsWith(artifactId.toLowerCase())) {  // artifactId[:verPrefix] = groupId
					artiGrpLines.add(line);
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
	
}
