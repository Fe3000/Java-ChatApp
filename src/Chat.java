import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

public class Chat extends Application implements javax.jms.MessageListener 
{
	
	static String username;
	public static final String TOPIC = "topic/Chat";
	
	Stage window;
	Scene chatScene;
	Button sendButton;

	/* Text colors */
	public static final String ANSI_RESET = "\033[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\033[0;31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\033[0;33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	
	private Service<Void> backgroundTread;
	
	static Topic mTopic;
	static TopicConnection mTopicConnection;

	public static void main(String[] args) throws
	JMSException, IOException, NamingException
	{
		launch(args);
		username = args[0];
		initChat();
	}
	
	private static void initChat()
			throws JMSException, NamingException, IOException
	{
		Chat chat = new Chat();
		Context initialContext = Chat.getInitialContext();
		mTopic = (Topic)initialContext.lookup(Chat.TOPIC);
		TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory)
				initialContext.lookup("ConnectionFactory");
		mTopicConnection =
				topicConnectionFactory.createTopicConnection();
		System.out.println("Ready for chat");
		chat.subscribe(mTopicConnection, mTopic, chat);
		chat.publish(mTopicConnection, mTopic, username);
	}

	public void subscribe(TopicConnection topicConnection, Topic topic, Chat chat)
			throws JMSException
	{
		TopicSession subscribeSession = topicConnection
				.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		TopicSubscriber topicSubscriper = subscribeSession
				.createSubscriber(topic);
		topicSubscriper.setMessageListener(chat);
	}
	
	public void publish(TopicConnection topicConnection, Topic topic, String username)
			throws JMSException, IOException
	{
		TopicSession publishSession = topicConnection
				.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		TopicPublisher topicPublisher = publishSession
				.createPublisher(topic);
		topicConnection.start();
		BufferedReader reader = new java.io.BufferedReader
				(new InputStreamReader(System.in));

		System.out.println(ANSI_YELLOW + "    " + username + " joined the chat!" + ANSI_RESET);
		sendMessage(ANSI_YELLOW + "    " + username + " joined the chat!" + ANSI_RESET
				,publishSession,topicPublisher);
		
		while (true) {
			String messageToSend = reader.readLine();
			if (messageToSend.equals("exit")) {
				System.out.println(ANSI_RED + "    " + username + " left the chat!" + ANSI_RESET);
				sendMessage(ANSI_RED + "    " + username + " left the chat!" + ANSI_RESET
						,publishSession,topicPublisher);
				topicConnection.close();
				System.exit(0);
			} else {
				sendMessage("     " + username + ":  " + messageToSend
						,publishSession,topicPublisher);
			}
		}
	}
	
	public void sendMessage(String sMessage, TopicSession publishSession, TopicPublisher topicPublisher)
			throws JMSException
	{
		TextMessage message = publishSession.createTextMessage();
		message.setText(sMessage);
		topicPublisher.publish(message);
	}
	
	public static Context getInitialContext()
			throws JMSException, NamingException
	{
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
		props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		props.put(Context.PROVIDER_URL, "jnp://localhost:1099");
		InitialContext ctx = new InitialContext(props);
		return ctx;
	}

	@Override
	public void onMessage(Message message)
	{
		// When we get a message
		try
		{
			System.out.println(((TextMessage)message).getText());
		} catch (JMSException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void start(Stage stage) throws Exception
	{
		// Set up chat window
		window = stage;
		window.setTitle("GroundE");
		VBox root = new VBox(5);
		
		System.out.println("Setting up window");
		
		//Label label = new Label("Send message");
		sendButton = new Button("Send");
		TextArea messageArea = new TextArea();
		messageArea.setPromptText("Your message..");
		
		TextArea chatArea = new TextArea();
		chatArea.setEditable(false);
		VBox.setVgrow(chatArea, Priority.ALWAYS);
		
		// Start chat connection
		backgroundTread = new Service<Void>()
		{

			@Override
			protected Task<Void> createTask() {
				return new Task<Void>() {
					
					@Override
					protected Void call() throws Exception {
						try {
							System.out.println("HEJ");
							initChat();
						} catch (JMSException | NamingException | IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null;
					}
				};
			}
			
		};
		
		sendButton.setOnAction(e -> {
				String message = messageArea.getText();
				try {
					publish(mTopicConnection, mTopic, message);
				} catch (JMSException | IOException e1) {
					e1.printStackTrace();
				}
			});
		
		
		
		AnchorPane bottomRow = new AnchorPane();
        AnchorPane.setLeftAnchor(messageArea, 0.0);
        AnchorPane.setRightAnchor(sendButton, 0.0);
        bottomRow.getChildren().addAll(messageArea, sendButton);
		
		root.getChildren().addAll(chatArea, bottomRow);
		Scene scene = new Scene(root);
		
		window.setScene(scene);
		window.show();
		
		System.out.println("Window setup done.");
	}

}
