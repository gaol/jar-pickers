/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.ircbot;

import java.util.List;
import java.util.regex.Matcher;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.eap.trackers.model.Artifact;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author lgao
 *
 */
public class GroupIdAnswer extends AbstractAnswer {

	private static final String API_PATH = "/groupids/";
	
	private static final Logger logger = LoggerFactory.getLogger(GroupIdAnswer.class);
	
	@Override
	public Answer answer() throws Exception {
		Matcher matcher = getPattern().matcher(getQuestion());
		if (matcher.matches()) {
			String artifactToQuery = matcher.group(1);
			String ctxPath = getRestAPIBase() + API_PATH + artifactToQuery;
			ResteasyClient client = new ResteasyClientBuilder().build();
            ResteasyWebTarget target = client.target(ctxPath);
            Response resp = target.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
			Answer answer = new Answer();
			answer.setAnswered(true);
			int status = resp.getStatus();
			if (status == 200) {
				// Good
				MediaType mediaType = resp.getMediaType();
				if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
					GenericType<List<Artifact>> artisType = new GenericType<List<Artifact>>(){};
					List<Artifact> artis = resp.readEntity(artisType);
					if (artis != null && artis.size() > 0) {
						StringBuilder sb = new StringBuilder();
						if (artis.size() == 1) {
							Artifact arti = artis.get(0);
							if (isFullAnswer()) {
								sb.append("{");
								sb.append("\"groupId\" : \"" + arti.getGroupId() + "\", ");
								sb.append("\"version\" : \"" + arti.getVersion() + "\"");
								sb.append("}");
							} else {
								sb.append(artis.get(0).getGroupId());
							}
						} else {
							 // more than one groupId found
							sb.append("[");
							boolean first = true;
							for (Artifact arti: artis) {
								if (first) {
									first = false;
								} else {
									sb.append(", ");
								}
								sb.append("{");
								sb.append("\"groupId\" : \"" + arti.getGroupId() + "\", ");
								sb.append("\"version\" : \"" + arti.getVersion() + "\"");
								sb.append("}");
							}
							sb.append("]");
						}
						answer.setAnswer("GroupId(s) of: \"" + artifactToQuery + "\": " + sb.toString());
						return answer;
					}
				} else {
					logger.error("Unkown content type: " + mediaType.getType());
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
