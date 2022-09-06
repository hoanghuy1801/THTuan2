package app;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.BasicConfigurator;

public class ChatApp extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	JTextArea textArea;
	JButton Button;
	private Session session;
	private MessageProducer producer;
	private Message msg;
	private Connection con;
	private Destination destination;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatApp frame = new ChatApp();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	

	/**
	 * Create the frame.
	 */
	public ChatApp() throws JMSException, NamingException {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 501);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		textField = new JTextField();
		textField.setBounds(0, 431, 331, 31);
		contentPane.add(textField);
		textField.setColumns(10);
		
		 Button = new JButton("Send");
		Button.setBounds(329, 431, 105, 31);
		contentPane.add(Button);
		
		 textArea = new JTextArea();
		textArea.setBounds(0, 0, 434, 431);
		contentPane.add(textArea);
		//thiết lập môi trường cho JMS
		BasicConfigurator.configure();
//thiết lập môi trường cho JJNDI
		Properties settings = new Properties();
		settings.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		settings.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
//tạo context
		Context ctx = new InitialContext(settings);
//lookup JMS connection factory
		Object obj = ctx.lookup("ConnectionFactory");
		ConnectionFactory factory = (ConnectionFactory) obj;
//lookup destination
		destination = (Destination) ctx.lookup("dynamicQueues/thanthidet");
//tạo connection
		con = factory.createConnection("admin", "admin");
//nối đến MOM
		con.start();
		

		// tạo session
		session = con.createSession(/* transaction */false, /* ACK */Session.AUTO_ACKNOWLEDGE);
		// tạo consumer
		MessageConsumer receiver = session.createConsumer(destination);

		
		receiver.setMessageListener(new MessageListener() {
			// có message đến queue, phương thức này được thực thi
			public void onMessage(Message msg) {// msg là message nhận được
				try {
					if (msg instanceof TextMessage) {
						TextMessage tm = (TextMessage) msg;
						String txt = tm.getText();
						textArea.append("Nhận được " + txt + "\n");
						msg.acknowledge();// gửi tín hiệu ack
					} else if (msg instanceof ObjectMessage) {
						ObjectMessage om = (ObjectMessage) msg;
						System.out.println(om);
					}
					// others message type....
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		

}
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Object obj = e.getSource();

		if (obj == Button && !textField.getText().equalsIgnoreCase("")) {
			try {

				// create session

				// create producer
				MessageProducer producer = session.createProducer(destination);
				// create text message

				String input = textField.getText().toString().trim();

				msg = session.createTextMessage(input);
				producer.send(msg);

				textArea.append("Gui di: " + input + "\n");

				textField.setText("");
			} catch (Exception e2) {
				e2.printStackTrace();
			}

		}

	}
	

}
