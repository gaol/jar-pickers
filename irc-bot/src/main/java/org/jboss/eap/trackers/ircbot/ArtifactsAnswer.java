/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.ircbot;

import java.util.Iterator;
import java.util.regex.Matcher;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;


/**
 * @author lgao
 *
 */
public class ArtifactsAnswer extends AbstractAnswer  {

	private static final String API_PATH = "/groupids/artifacts/";
	
	@Override
	public QuestionType getQuestionType() {
		return QuestionType.ARTIFACTS_OF;
	}

	@Override
	public Answer answer() throws Exception {
		Matcher matcher = PATTERN_ARTIS_OF.matcher(getQuestion());
		if (matcher.matches()) {
			String groupIdToQuery = matcher.group(1);
			ClientBuilder builder = ClientBuilder.newBuilder();
			Client client = builder.build();
			WebTarget target = client.target(getRestAPIBase() + API_PATH + groupIdToQuery);
			Response resp = target.request().buildGet().invoke();
			Answer answer = new Answer();
			answer.setAnswered(true);
			int status = resp.getStatus();
			if (status == 200) {
				// Good
				MediaType mediaType = resp.getMediaType();
				if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
					ObjectMapper om = new ObjectMapper();
					String jsonTree = resp.readEntity(String.class);
					ArrayNode rootNode = (ArrayNode)om.readTree(jsonTree);
					if (rootNode.size() > 0) {
						Iterator<JsonNode> nodes = rootNode.iterator();
						StringBuilder sb = new StringBuilder();
						sb.append("[");
						boolean first = true;
						while (nodes.hasNext()) {
							if (first) {
								first = false;
							} else {
								sb.append(", ");
							}
							JsonNode node = nodes.next();
							String artiId = node.get("artifactId").getTextValue();
							sb.append(artiId);
						}
						sb.append("]");
						answer.setAnswer("Artifacts under: \"" + groupIdToQuery + "\" are: " + sb.toString());
						return answer;
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
