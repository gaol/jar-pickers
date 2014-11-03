/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.ircbot;

import java.util.List;
import java.util.regex.Matcher;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.eap.trackers.model.Artifact;
import org.jboss.eap.trackers.utils.ArtifactsUtil;

/**
 * @author lgao
 *
 */
public class ArtifactsVersionAnswer extends AbstractAnswer {

	private static final String API_PATH = "/a/";

	@Override
	public Answer answer() throws Exception {
		Matcher matcher = getPattern().matcher(getQuestion());
		if (matcher.matches()) {
			String filter = matcher.group(1);
			String prdName = matcher.group(2);
			String prdVersion = matcher.group(3);
			
			ClientBuilder builder = ClientBuilder.newBuilder();
			Client client = builder.build();
			WebTarget target = client.target(getRestAPIBase() + API_PATH + prdName.toUpperCase() + ":" + prdVersion + "?filter=" + filter);
			Response resp = target.request().buildGet().invoke();
			Answer answer = new Answer();
			answer.setAnswered(true);
			int status = resp.getStatus();
			if (status == 200) {
				// Good
				MediaType mediaType = resp.getMediaType();
				if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
					GenericType<List<Artifact>> artisType = new GenericType<List<Artifact>>(){};
					List<Artifact> artis = resp.readEntity(artisType);
					if (!isFullAnswer()) {
						artis = ArtifactsUtil.distinctGroupIdArtis(artis);
					}
					if (artis != null && artis.size() > 0) {
						if (artis.size() == 1) {
							Artifact arti = artis.get(0);
							String msgPrefix = "Version of " + arti.getArtifactId() + " in: \"" + prdName + ":" + prdVersion + "\" is: ";
							if (isFullAnswer()) {
								StringBuilder sb = new StringBuilder();
								sb.append(msgPrefix);
								sb.append("{");
								sb.append("\"groupId\": \"" + arti.getGroupId() + "\", ");
								sb.append("\"artifactId\": \"" + arti.getArtifactId() + "\", ");
								sb.append("\"version\": \"" + arti.getVersion() + "\"");
								if (arti.getBuildInfo() != null && arti.getBuildInfo().length() > 0) {
									sb.append(", ");
									sb.append("\"buildInfo\": \"" + arti.getBuildInfo() + "\"");
								}
								sb.append("}");
								answer.setAnswer(sb.toString());
							} else {
								answer.setAnswer(msgPrefix + arti.getVersion());
							}
							return answer;
						} else if (artis.size() > 1) {
							StringBuilder sb = new StringBuilder();
							sb.append("[");
							boolean first = true;
							for (Artifact arti: artis) {
								if (first) {
									first = false;
								} else {
									sb.append(", ");
								}
								sb.append("{");
								sb.append("\"groupId\": \"" + arti.getGroupId() + "\", ");
								sb.append("\"artifactId\": \"" + arti.getArtifactId() + "\", ");
								sb.append("\"version\": \"" + arti.getVersion() + "\"");
								if (arti.getBuildInfo() != null && arti.getBuildInfo().length() > 0) {
									sb.append(", ");
									sb.append("\"buildInfo\": \"" + arti.getBuildInfo() + "\"");
								}
								sb.append("}");
							}
							sb.append("]");
							answer.setAnswer("Version of " + filter + " in: \"" + prdName + ":" + prdVersion + "\" is: " + sb.toString());
							return answer;
						}
					}
				}
			} else if (status == 404) {
				// not found
				String msg = resp.readEntity(String.class);
				answer.setAnswer(msg);
				return answer;
			}
		}
		return null;
	}

}
