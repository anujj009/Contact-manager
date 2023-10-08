package com.smart.service;

import java.util.Properties;

import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
	
	public boolean sendEmail(String subject, String text, String to) {
		
		boolean flag = false;
		
		String from = "anujj572@gmail.com";
		
		//variable for email
		String host = "smtp.gmail.com";
		
		//get system properties
		Properties properties = System.getProperties();
		System.out.println(properties);
		
		//setting important information to properties object
		
		//host set
		properties.put("mail.smtp.auth", true);
		properties.put("mail.smtp.starttls.enable", true);
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.debug", "true");
		
		String username = "anujj572@gmail.com";
		String password ="tecpsymbaeoixlfw";
		
		//session
		Session session = Session.getInstance(properties, new Authenticator() {
			
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				
				return new PasswordAuthentication(username, password);
			}
		});
		
		//compose message
		try {
			Message message = new MimeMessage(session);
			
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setFrom(new InternetAddress(from));
			message.setSubject(subject);
			message.setText(text);

			
			Transport.send(message);
			
			System.out.println("*****************");
			flag=true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return flag;
	}

}
