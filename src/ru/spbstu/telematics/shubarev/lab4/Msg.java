package ru.spbstu.telematics.shubarev.lab4;

import java.io.Serializable;
import java.util.Date;


public class Msg implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5174856877785175031L;
	private String senderName;
	private Date date;
	private String message;
	private String recipient;
	
	public Msg(String sender, Date date, String message, String recipient) {
		super();
		this.senderName = sender;
		this.date = date;
		this.message = message;
		this.recipient = recipient;
	}

	/**
	 * @return the sender
	 */
	public String getSenderName() {
		return senderName;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the recipient
	 */
	public String getRecipient() {
		return recipient;
	}
	
	@Override
	public String toString(){
		String blank = " ";
		String colon = ": ";
		return date.toString() + blank + senderName + colon + message;  
	}
	
	
	
}
