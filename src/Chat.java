import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Chat implements javax.jms.MessageListener {
	
	static String username;
	public static final String TOPIC = "topic/chat";

	public static void main(String[] args) throws JMSException, IOException, NamingException {
		username = args[0];
		Chat chat = new Chat();
		Context initialContext = Chat.getInitialContext();
		Topic topic = (Topic)initialContext.lookup(Chat.TOPIC);
		TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory)initialContext.lookup("ConnectionFactory");
		TopicConnection topicConnection = topicConnectionFactory.createTopicConnection();
		chat.subscribe(topicConnection, topic, chat);
		chat.publish(topicConnection, topic, username);
	}
	
	public void subscribe(TopicConnection topicConnection, Topic topic, Chat chat) throws JMSException {
		TopicSession subscribeSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		TopicSubscriber topicSubscriper = subscribeSession.createSubscriber(topic);
		topicSubscriper.setMessageListener(chat);
	}
	
	public void publish(TopicConnection topicConnection, Topic topic, String username) throws JMSException, IOException {
		TopicSession publishSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		TopicPublisher topicPublisher = publishSession.createPublisher(topic);
		topicConnection.start();
		BufferedReader reader = new java.io.BufferedReader(new InputStreamReader(System.in));
		
		while (true) {
			String messageToSend = reader.readLine();
			if (messageToSend.equals("exit")) {
				topicConnection.close();
				System.exit(0);
			} else {
				TextMessage message = publishSession.createTextMessage();
				message.setText("    " + username + ": " + messageToSend);
				topicPublisher.publish(message);
			}
		}
	}
	
	public static Context getInitialContext() throws JMSException, NamingException {
		Properties props = new Properties();
		props.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		props.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
		props.setProperty("java.naming.provoder.url", "jnp://localhost:1099");
		InitialContext context = new InitialContext(props);
		return context;
	}

	@Override
	public void onMessage(Message message) {
		// When we get a message
		System.out.println((TextMessage)message);
	}

}
