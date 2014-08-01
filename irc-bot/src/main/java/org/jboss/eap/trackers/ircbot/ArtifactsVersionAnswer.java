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
public class ArtifactsVersionAnswer extends AbstractAnswer {

	private static final String API_PATH = "/a/";
	
	@Override
	public QuestionType getQuestionType() {
		return QuestionType.VERSION_OF_ARTI_IN_PV;
	}

	@Override
	public Answer answer() throws Exception {
		Matcher matcher = PATTERN_ARTI_VERSION_OF.matcher(getQuestion());
		if (matcher.matches()) {
			String filter = matcher.group(1);
			String prdName = matcher.group(2);
			String prdVersion = matcher.group(3);
			
			ClientBuilder builder = ClientBuilder.newBuilder();
			Client client = builder.build();
			WebTarget target = client.target(getRestAPIBase() + API_PATH + prdName + ":" + prdVersion + "?filter=" + filter);
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
							JsonNode node = nodes.next(); // each one is an Artifact Node
							String artiId = node.get("artifactId").getTextValue();
							String groupId = node.get("groupId").getTextValue();
							String version = node.get("version").getTextValue();
							
							String buildInfo = node.get("buildInfo").getTextValue();
							sb.append("{");
							sb.append("\"groupId\": \"" + groupId + "\", ");
							sb.append("\"artifactId\": \"" + artiId + "\", ");
							sb.append("\"version\": \"" + version + "\"");
							if (buildInfo != null && buildInfo.length() > 0 && !buildInfo.toLowerCase().equals("null")) {
								sb.append(", ");
								sb.append("\"buildInfo\": \"" + buildInfo + "\"");
							}
							sb.append("}");
						}
						sb.append("]");
						answer.setAnswer("Artifacts of Product: \"" + prdName + ":" + prdVersion + "\" are: " + sb.toString());
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
