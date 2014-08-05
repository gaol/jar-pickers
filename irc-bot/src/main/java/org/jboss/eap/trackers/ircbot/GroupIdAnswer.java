/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.ircbot;

import java.util.List;
import java.util.regex.Matcher;

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
	public QuestionType getQuestionType() {
		return QuestionType.GROUPD_ID_OF;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Answer answer() throws Exception {
		Matcher matcher = PATTERN_GRP_ID_OF.matcher(getQuestion());
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
					Object entity = resp.getEntity();
					if (entity != null && entity instanceof List) {
						List<Artifact> artis = (List<Artifact>)entity;
						if (artis != null && artis.size() > 0) {
							StringBuilder sb = new StringBuilder();
							sb.append("[");
							if (artis.size() == 1) {
								Artifact arti = artis.get(0);
								sb.append(arti.getGroupId());
							} else { 
								 // more than one groupId found
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
							}
							sb.append("]");
							answer.setAnswer("Artifacts under: \"" + artifactToQuery + "\" are: " + sb.toString());
							return answer;
						}
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
