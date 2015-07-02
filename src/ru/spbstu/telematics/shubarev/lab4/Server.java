package ru.spbstu.telematics.shubarev.lab4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Server {
	private DatagramSocket socket;

	private ObjectInputStream ois = null;
	private ObjectOutputStream oos = null;
	private ByteArrayInputStream bis = null;
	private ByteArrayOutputStream bos = null;

	private List<Users> users;

	public Server(String inetAdress, int portNumber) throws SocketException,
			UnknownHostException {

		socket = new DatagramSocket(portNumber,
				InetAddress.getByName(inetAdress));
		users = new ArrayList<Users>();
	}

	// form datagram packet for sending
	public DatagramPacket formPacket(byte[] buf, Users recipient) {
		System.out.println(recipient.getInetAddress());
		System.out.println(recipient.getPortNumber());
		return new DatagramPacket(buf, buf.length, recipient.getInetAddress(),
				recipient.getPortNumber());
	}

	// receive message
	public void recieveMessage(DatagramPacket p) throws IOException {
		socket.receive(p);
	}

	// send message
	public void sendMessage(DatagramPacket p) throws IOException {
		socket.send(p);
	}

	public void sendByName(Msg msg, Users recipient) throws IOException {
		byte[] data = getBytesFromMessage(msg);
		DatagramPacket packet = formPacket(data, recipient);
		socket.send(packet);
	}

	public void sendForAll(Msg msg) throws IOException {
		byte[] data = getBytesFromMessage(msg);
		for (Users a : users) {
			DatagramPacket packet = formPacket(data, a);
			socket.send(packet);
		}
	}

	// decode message from bytes
	public Msg getMessageFromBytes(byte[] data) throws IOException {
		bis = new ByteArrayInputStream(data);
		ois = new ObjectInputStream(bis);
		Msg msg = null;
		try {
			msg = (Msg) ois.readObject();
		} catch (ClassNotFoundException e) {
			System.err.println("Something wrong with incoming message" + e);
		}
		ois.close();
		bis.close();
		return msg;
	}

	// code message to bytes
	public byte[] getBytesFromMessage(Msg msg) throws IOException {
		bos = new ByteArrayOutputStream();
		oos = new ObjectOutputStream(bos);
		oos.writeObject(msg);
		byte[] data = null;
		data = bos.toByteArray();
		oos.close();
		bos.close();
		return data;
	}

	public void addUser(Users user) {
		boolean flag = true;
		for (Users a : users) {
			if (user.getPortNumber() == a.getPortNumber()) {
				flag = false;
			}
		}
		if (flag) {
			users.add(user);
			System.out.println("There are new user: " + user.getName());
		}
	}

	public List<Users> checkUser(String user) {
		List<Users> tmp = new ArrayList<Users>();
		for (Users a : users) {
			if (a.getName().equals(user))
				tmp.add(a);
		}
		return tmp;
	}

	public static void main(String[] args) throws SocketException,
			UnknownHostException {
		Server server = new Server("localhost", 5555);
		while (true) {
			byte[] buf = new byte[1024];
			DatagramPacket incomingData = new DatagramPacket(buf, buf.length);
			try {
				server.recieveMessage(incomingData);
			} catch (IOException e) {
				System.err.println("Something wrong with incoming data" + e);
			}
			Msg incomingMessagge = null;
			try {
				incomingMessagge = server.getMessageFromBytes(incomingData
						.getData());
			} catch (IOException e) {
				System.err.println("Problem with decoding message" + e);
			}
			System.out.println(incomingMessagge);
			if (incomingMessagge.getRecipient().equals("server")) {
				Users newUser = new Users(incomingData.getAddress(),
						Integer.parseInt(incomingMessagge.getMessage()),
						incomingMessagge.getSenderName());
				server.addUser(newUser);
				System.out.println(newUser);
			} else if (incomingMessagge.getRecipient().equals("all")) {
				try {
					server.sendForAll(incomingMessagge);
				} catch (IOException e) {
					System.err.println("Problem with sending message" + e);
				}
			} else if (incomingMessagge.getRecipient().equals("buy")) {
				server.checkHimOut(incomingMessagge);
			} else {
				List<Users> recipient = server.checkUser(incomingMessagge
						.getRecipient());
				if (recipient == null) {
					try {
						server.sendForAll(incomingMessagge);
					} catch (IOException e) {
						System.err.println("Problem with sending message" + e);
					}
				} else {
					try {
						for (Users usr : recipient)
							server.sendByName(incomingMessagge, usr);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void checkHimOut(Msg incomingMessagge) {
		int pv = Integer.parseInt(incomingMessagge.getMessage());
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getPortNumber() == pv) {
				users.remove(i);
				return;
			}
		}
	}
}
