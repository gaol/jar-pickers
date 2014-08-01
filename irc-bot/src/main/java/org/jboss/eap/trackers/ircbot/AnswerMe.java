/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.ircbot;

import java.util.regex.Pattern;

/**
 * @author lgao
 *
 */
public interface AnswerMe {
	
	Pattern PATTERN_GRP_ID_OF = Pattern.compile("\\s*" + QuestionType.GRP_ID_OF + "\\s+(\\S+)\\s*[^\n]*");
	Pattern PATTERN_ARTIS_OF = Pattern.compile("\\s*" + QuestionType.ARTIS_OF + "\\s+(\\S+)\\s*[^\n]*");
	Pattern PATTERN_ARTI_VERSION_OF = Pattern.compile("\\s*" + QuestionType.ARTIS_VERS_OF + "\\s+(\\S+)\\s+in\\s+(\\S+):(\\S+)\\s*[^\n]*");
	
	public enum QuestionType {
		HELP,
		GROUPD_ID_OF,
		ARTIFACTS_OF,
		VERSION_OF_ARTI_IN_PV;
		
		public static final String HELP_ = "help";
		public static final String GRP_ID_OF = "groupid_of";
		public static final String ARTIS_OF = "artifacts_of";
		public static final String ARTIS_VERS_OF = "version_of";
		
		public static QuestionType forName(String line) {
			if (line == null || line.length() == 0) {
				return null;
			}
			line = line.trim().toLowerCase();
			if (PATTERN_GRP_ID_OF.matcher(line).matches()) {
				return GROUPD_ID_OF;
			} else if (line.equals(HELP_)) {
				return HELP;
			} else if (PATTERN_ARTIS_OF.matcher(line).matches()) {
				return ARTIFACTS_OF;
			} else if (PATTERN_ARTI_VERSION_OF.matcher(line).matches()) {
				return VERSION_OF_ARTI_IN_PV;
			}
			return null;
		}
	}
	
	String getQuestion();
	
	void setQuestion(String question);
	
	String getRestAPIBase();
	
	void setRestAPIBase(String restAPI);
	
	QuestionType getQuestionType();
	
	Answer answer() throws Exception;

}
