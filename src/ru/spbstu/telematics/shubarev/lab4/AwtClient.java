package ru.spbstu.telematics.shubarev.lab4;

import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.awt.Font;

public class AwtClient {

	private JFrame frame;
	private JTextField textForSending;
	private Client client;
	private JTextField txtName;
	private TextArea textArea;
	private JTextField textNameOfRecipient;
	private volatile boolean in;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AwtClient window = new AwtClient();
					window.frame.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public AwtClient() {
		initialize();
	}

	public void startListening() {
		new Thread(new Runnable() {
			public void run() {
				while (in) {
					byte[] incomingData = new byte[1024];
					DatagramPacket p = new DatagramPacket(incomingData,
							incomingData.length);
					System.out.println("listening...");
					try {
						client.recieveMessage(p);
					} catch (IOException e) {
						System.err.println("Can't get the message" + e);
					}
					System.out.println();
					Msg msg = null;
					try {
						msg = client.getMessageFromBytes(p.getData());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					textArea.append(msg.toString() + '\n');
				}
			}
		}).start();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		in = false;
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (in) {
					Msg buyServer = client.formGoodbuyMessage();
					try {
						client.sendMessage(buyServer);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					in = false;
				}
				System.out.println();
				e.getWindow().dispose();
			}
		});
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JPanel panelForMessages = new JPanel();
		panelForMessages.setBounds(0, 0, 330, 275);
		frame.getContentPane().add(panelForMessages);
		panelForMessages.setLayout(null);

		JPanel panelForGetting = new JPanel();
		panelForGetting.setBounds(0, 0, 330, 201);
		panelForMessages.add(panelForGetting);
		panelForGetting.setLayout(new CardLayout(0, 0));

		textArea = new TextArea();
		textArea.setEditable(false);
		panelForGetting.add(textArea, "name_32238910945477");

		JPanel panelForSending = new JPanel();
		panelForSending.setBounds(0, 213, 330, 61);
		panelForMessages.add(panelForSending);
		panelForSending.setLayout(new CardLayout(0, 0));

		textForSending = new JTextField();
		panelForSending.add(textForSending, "name_10163533542484");
		textForSending.setColumns(10);

		JPanel panelForButtons = new JPanel();
		panelForButtons.setBounds(342, 0, 106, 275);
		frame.getContentPane().add(panelForButtons);
		panelForButtons.setLayout(null);

		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Msg msg = client.formMessage(textForSending.getText(),
						"all");
				textForSending.setText("");
				try {
					client.sendMessage(msg);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnSend.setBounds(12, 238, 70, 25);
		btnSend.setHorizontalAlignment(SwingConstants.RIGHT);
		panelForButtons.add(btnSend);

		JButton btnLogin = new JButton("login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (!in) {
						client = new Client(txtName.getText());
						Msg helloServer = client.formWelcomingMessage();
						client.sendMessage(helloServer);
						in = true;
						btnLogin.setText("logout");
						startListening();
						textForSending.setText("");
					} else {
						Msg buyServer = client.formGoodbuyMessage();
						client.sendMessage(buyServer);
						in = false;
						btnLogin.setText("login");
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnLogin.setBounds(12, 36, 82, 25);
		panelForButtons.add(btnLogin);

		txtName = new JTextField();
		txtName.setText("Name");
		txtName.setBounds(0, 12, 94, 19);
		panelForButtons.add(txtName);
		txtName.setColumns(10);

		textNameOfRecipient = new JTextField();
		textNameOfRecipient.setBounds(0, 115, 94, 19);
		panelForButtons.add(textNameOfRecipient);
		textNameOfRecipient.setColumns(10);

		JButton SendToUser = new JButton("Send to user");
		SendToUser.setFont(new Font("Dialog", Font.BOLD, 8));
		SendToUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Msg msg = client.formMessage(textForSending.getText(),
						textNameOfRecipient.getText());
				try {
					client.sendMessage(msg);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		SendToUser.setBounds(0, 146, 94, 25);
		panelForButtons.add(SendToUser);
	}
}