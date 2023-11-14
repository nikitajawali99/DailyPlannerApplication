package com.dailyplanner.event;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.dailyplanner.entity.User;
import com.dailyplanner.token.VerificationToken;
import com.dailyplanner.token.VerificationTokenRepository;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

	static Logger log = LoggerFactory.getLogger(RegistrationCompleteEventListener.class);

	private final JavaMailSender mailSender;
	private final VerificationTokenRepository tokenRepository;
	private User user;

	@Transactional
	@Override
	public void onApplicationEvent(RegistrationCompleteEvent event) {

		log.info("Entering into RegistrationCompleteEventListener :: onApplicationEvent");

		// 1. get the user
		user = event.getUser();
		// 2. generate a token for the user
		String vToken = UUID.randomUUID().toString();
		// 3. save the token for the user
		saveVerificationTokenForUser(user, vToken);
		// 4. Build the verification url
		String url = event.getConfirmationUrl() + "/registration/verifyEmail?token=" + vToken;
		// 5. send the email to the user

		log.info("Sending mail to the user :" + url);
		try {
			log.info("Entering into RegistrationCompleteEventListener :: Before sending mail");
			sendVerificationEmail(url);
			log.info("Exiting into RegistrationCompleteEventListener :: Before sending mail");
		} catch (UnsupportedEncodingException | MessagingException e) {
			throw new RuntimeException(e.getMessage());
		}

	}

	@Transactional
	private void saveVerificationTokenForUser(User user, String vToken) {

		log.info("Entering into RegistrationCompleteEventListener :: saveVerificationTokenForUser");
		Date currentDate = new Date();
		var verificationToken = new VerificationToken(vToken, user, currentDate);
		tokenRepository.save(verificationToken);
		log.info("Exiting into RegistrationCompleteEventListener :: saveVerificationTokenForUser");
	}

	@Transactional
	public void sendVerificationEmail(String url) throws MessagingException, UnsupportedEncodingException {

		log.info("Entering into RegistrationCompleteEventListener :: sendVerificationEmail");

		String subject = "Email Verification | Daily Planner System ";
		String senderName = "Daily Planner Service";

		String mailContent = " Hi " + user.getName() + ", " + " Thank you for registering with us.  " + "" + "\n" + "\n"
				+ "Please, follow the link below to complete your registration : " + "" + url + "\" " + "\n" + "\n"
				+ "Verify your email to activate your account. ";

		emailMessage(subject, senderName, mailContent, mailSender, user);

	}

	public void sendPasswordResetVerificationEmail(String url, User user)
			throws MessagingException, UnsupportedEncodingException {

		log.info("Entering into RegistrationCompleteEventListener :: sendPasswordResetVerificationEmail");
		String subject = "Password Reset Request Verification | Daily Planner System";
		String senderName = "Daily Planner Service";

		String mailContent = " Hi " + user.getName() + ", " + " You recently requested to reset your password,  " + ""
				+ "\n" + "\n" + "Please, follow the link below to complete the action : " + "" + url + "\" " + "\n"
				+ "\n" + "Reset password. ";

		emailMessage(subject, senderName, mailContent, mailSender, user);

		log.info("Exiting into RegistrationCompleteEventListener :: sendPasswordResetVerificationEmail");
	}

	private static void emailMessage(String subject, String senderName, String mailContent, JavaMailSender mailSender,
			User theUser) throws MessagingException, UnsupportedEncodingException {
		log.info("Entering into RegistrationCompleteEventListener :: emailMessage");
		try {

			final String username = "dailyplannerapplication23@gmail.com";
			final String password = "wgij tzmm ivps kfwx";

			Properties prop = new Properties();
			prop.put("mail.smtp.host", "smtp.gmail.com");
			prop.put("mail.smtp.port", "587");
			prop.put("mail.smtp.auth", "true");
			prop.put("mail.smtp.starttls.enable", "true"); // TLS

			Session session = Session.getInstance(prop, new jakarta.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("from@gmail.com"));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(theUser.getEmail()));
			message.setSubject(subject);
			message.setText(mailContent);

			log.info("Entering into emailMessage :: before sending-mail");
			Transport.send(message);
			log.info("Exiting into emailMessage :: Mail send succesfully ::");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
