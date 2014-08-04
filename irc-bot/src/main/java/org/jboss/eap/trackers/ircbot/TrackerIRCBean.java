/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.ircbot;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.irc.IrcConfiguration;
import org.apache.camel.component.irc.IrcConstants;
import org.apache.camel.component.irc.IrcEndpoint;
import org.apache.camel.component.irc.IrcMessage;
import org.jboss.eap.trackers.ircbot.AnswerMe.QuestionType;
import org.schwering.irc.lib.IRCUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lgao
 * 
 * The bean which is responsible to answer queries from IRC channels.
 *
 */
public class TrackerIRCBean {
	
	public static final String METHOD_NAME = "chatIRC";
	private static final String MAX_ANSWER_LEN_KEY = "irc.answer.max.length";
	private static final String MAX_ANSWER_PRIVATE_KEY = "irc.answer.private.length";
	private static final String PRIVATE_ANSWER = "privately";
	
	private static final String PASTEBIN_URI = "http://pastebin.test.redhat.com/pastebin.php";

	private static final Logger logger = LoggerFactory.getLogger(TrackerIRCBean.class);
	
	private static HashMap<QuestionType, Class<? extends AnswerMe>> answerMap = new HashMap<QuestionType, Class<? extends AnswerMe>>();
	static {
		answerMap.put(QuestionType.HELP, HelpAnswer.class);
		answerMap.put(QuestionType.ARTIFACTS_OF, ArtifactsAnswer.class);
		answerMap.put(QuestionType.GROUPD_ID_OF, GroupIdAnswer.class);
		answerMap.put(QuestionType.VERSION_OF_ARTI_IN_PV, ArtifactsVersionAnswer.class);
	}
	
	private final IrcConfiguration ircConfig;
	private final String api;
	
	public TrackerIRCBean(IrcConfiguration config, String api) {
		if (config == null) {
			throw new IllegalArgumentException("IrcConfiguration can't be null for TrackerIRCBean.");
		}
		if (api == null) {
			throw new IllegalArgumentException("Tracker REST API can't be null for TrackerIRCBean.");
		}
		this.ircConfig = config;
		this.api = api;
	}
	
	private String getTrackerBotNickName() {
		return ircConfig.getNickname() != null ? ircConfig.getNickname() : ircConfig.getUsername();
	}
	/**
	 * The Tracker IRC Bot Answer Entrance.
	 */
	public void chatIRC(Exchange exchange) {
		IrcEndpoint ircEnd = (IrcEndpoint)exchange.getFromEndpoint();
		IrcMessage ircMsgIn = (IrcMessage)exchange.getIn();
		try {
			AnswerMe answerMe = getAnswerMe(ircMsgIn);
			if (answerMe != null) {
				answerMe.setQuestion(trimQuestion(ircMsgIn.getMessage()));
				answerMe.setRestAPIBase(api);
				Answer answer = answerMe.answer();
				if (answer != null && answer.isAnswered()) {
					maybePostToPastebin(answer);
					String target = getTarget(ircMsgIn, answer);
					if (logger.isDebugEnabled()) {
						logger.debug("Will answer the question to: " + target);
					}
					IRCUser user = new IRCUser(getTrackerBotNickName(), ircConfig.getUsername(), InetAddress.getLocalHost().getHostName());
					String messageBack = getAnswerMessage(answer);
					Exchange outExchange = ircEnd.createOnPrivmsgExchange(target, user, messageBack);
					ProducerTemplate producerTemp = exchange.getContext().createProducerTemplate();
					producerTemp.send(ircEnd, outExchange);
				} else {
					// I should be able to answer you, but sorry, I have no idea what it is, log it for further analysis
					StringBuilder sb = new StringBuilder();
					sb.append("Not answered question: \"");
					sb.append(answerMe.getQuestion());
					sb.append("\", from: ");
					sb.append(ircMsgIn.getUser().getNick());
					if (answer != null) {
						sb.append("\nThe reason is: ");
						sb.append(answer.getAnswer());
					}
					logger.info(sb.toString());
				}
			}
		} catch (Exception e) {
			logger.error("Error when answer question: \"" + ircMsgIn.getMessage() + "\" from: " + ircMsgIn.getUser().getNick(), e);
		}
	}

	// if the answer is too long than 400 letters, post it to pastebin.test.redhat.com for 1 day
	private void maybePostToPastebin(Answer answer) throws IOException, URISyntaxException {
		String msg = answer.getAnswer();
		if (msg.length() > Integer.getInteger(MAX_ANSWER_LEN_KEY, 400)) {
			DefaultPastebin pastebin = new DefaultPastebin();
			pastebin.setUserName(getTrackerBotNickName());
			pastebin.setPastebinURI(new URI(PASTEBIN_URI));
			String link = pastebin.pastebin(msg);
			answer.setPastebinLink(link);
		}
	}

	/**
	 * If the answer is too long, it maybe posted to paste bin and return the link.
	 */
	private String getAnswerMessage(Answer answer) {
		String msg = answer.getAnswer();
		String link = answer.getPastebinLink();
		return link == null ? msg : "Please see answer at: " + link;
	}

	/**
	 * Gets the target, maybe the channel name, or the user nick name for private talk.
	 */
	private String getTarget(IrcMessage ircMsgIn, Answer answer) {
		String target = ircMsgIn.getTarget();
		if (target.equals(getTrackerBotNickName())) { // in case of private message
			return ircMsgIn.getHeader(IrcConstants.IRC_USER_NICK, String.class);
		}
		if (wantsoToTalkPrivately(ircMsgIn.getMessage())) {
			return ircMsgIn.getHeader(IrcConstants.IRC_USER_NICK, String.class);
		}
		if (answer.getPastebinLink() == null) {
			if (answer.getAnswer().length() > Integer.getInteger(MAX_ANSWER_PRIVATE_KEY, 150)) {
				return ircMsgIn.getHeader(IrcConstants.IRC_USER_NICK, String.class);
			}
		}
		if (QuestionType.HELP.equals(getQuestionType(ircMsgIn))) {
			return ircMsgIn.getHeader(IrcConstants.IRC_USER_NICK, String.class); 
		}
		return target;
	}

	/**
	 * If the the string: 'privately' in the question, it will be answer back in a private dialog
	 */
	private boolean wantsoToTalkPrivately(String message) {
		return message.contains(PRIVATE_ANSWER);
	}
	
	private AnswerMe getAnswerMe(IrcMessage ircMsg) throws InstantiationException, IllegalAccessException {
		QuestionType questionType = getQuestionType(ircMsg);
		if (questionType == null) {
			return null;
		}
		Class<? extends AnswerMe> cls = answerMap.get(questionType);
		if (cls == null) {
			throw new IllegalStateException("Unkown answer for QuestionType: " + questionType);
		}
		if (questionType.equals(QuestionType.HELP)) {
			if (ircMsg.getMessage().startsWith(getTrackerBotNickName())) {
				return cls.newInstance(); // trackerBot HELP
			}
			if (ircMsg.getTarget().equals(getTrackerBotNickName())) {
				return cls.newInstance(); // HELP in private dialog
			}
			return null; // ignore!
		}
		return cls.newInstance();
	}

	private QuestionType getQuestionType(IrcMessage ircMsg) {
		String question = trimQuestion(ircMsg.getMessage());
		return QuestionType.forName(question);
	}
	
	private String trimQuestion(String question) {
		if (question.startsWith(getTrackerBotNickName())) {
			question = question.substring(getTrackerBotNickName().length() + 1);
 		}
		if (question.contains(PRIVATE_ANSWER)) {
			question = question.replace(PRIVATE_ANSWER, "");
		}
		return question;
	}
}
