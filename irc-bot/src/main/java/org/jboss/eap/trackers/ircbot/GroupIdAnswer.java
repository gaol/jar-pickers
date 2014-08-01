/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.ircbot;

import java.util.regex.Matcher;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;


/**
 * @author lgao
 *
 */
public class GroupIdAnswer extends AbstractAnswer {

	private static final String API_PATH = "/groupids/";
	
	@Override
	public QuestionType getQuestionType() {
		return QuestionType.GROUPD_ID_OF;
	}

	@Override
	public Answer answer() throws Exception {
		Matcher matcher = PATTERN_GRP_ID_OF.matcher(getQuestion());
		if (matcher.matches()) {
			String artifactToQuery = matcher.group(1);
			
			ClientBuilder builder = ClientBuilder.newBuilder();
			Client client = builder.build();
			WebTarget target = client.target(getRestAPIBase() + API_PATH + artifactToQuery);
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
						answer.setAnswer("GroupId(s) of ArtifactId: \"" + artifactToQuery + "\" are: " + jsonTree);
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
