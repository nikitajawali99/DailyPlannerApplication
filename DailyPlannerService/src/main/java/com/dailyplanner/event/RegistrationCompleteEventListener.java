package com.dailyplanner.event;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

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

	private final JavaMailSender mailSender;
	private final VerificationTokenRepository tokenRepository;
	private User user;

//	public RegistrationCompleteEventListener(IVerificationTokenService tokenService, JavaMailSender mailSender
//			) {
//		this.tokenService = tokenService;
//		this.mailSender = mailSender;
//	}

	@Transactional
	@Override
	public void onApplicationEvent(RegistrationCompleteEvent event) {
		
		  //1. get the user
        user = event.getUser();
        //2. generate a token for the user
        String vToken = UUID.randomUUID().toString();
        //3. save the token for the user
        saveVerificationTokenForUser(user, vToken);
        //4. Build the verification url
        String url = event.getConfirmationUrl()+"/registration/verifyEmail?token="+vToken;
        //5. send the email to the user
		
		
		try {
			sendVerificationEmail(url);
		} catch (UnsupportedEncodingException | MessagingException e) {
			throw new RuntimeException(e.getMessage());
		}

	}
	
	

	@Transactional
	private void saveVerificationTokenForUser(User user, String vToken) {

		Date currentDate = new Date();
		var verificationToken = new VerificationToken(vToken, user, currentDate);

		tokenRepository.save(verificationToken);
	}

	@Transactional
	public void sendVerificationEmail(String url) throws MessagingException, UnsupportedEncodingException {

		String subject = "Email Verification | Daily Planner System ";
		String senderName = "Daily Planner Service";

//		String mailContent = " Hi, " + user.getName() + ", " + " Thank you for registering with us,  " + ""
//				+ "Please, follow the link below to complete your registration." + "<a href=\"" + url
//				+ "\">Verify your email to activate your account. </a>";
		
		String mailContent = " Hi " + user.getName() + ", " + " Thank you for registering with us.  " + ""
				+ "\n" + "\n" + "Please, follow the link below to complete your registration : " + "" + url+ "\" "
				+ "\n" + "\n" + "Verify your email to activate your account. ";

		emailMessage(subject, senderName, mailContent, mailSender, user);

	}
	
	public void sendPasswordResetVerificationEmail(String url, User user) throws MessagingException, UnsupportedEncodingException {
      
		
		
		String subject = "Password Reset Request Verification | Daily Planner System";
        String senderName = "Daily Planner Service";
//        String mailContent = "<p> Hi, "+ user.getName()+ ", </p>"+
//                "<p><b>You recently requested to reset your password,</b>"+"" +
//                "Please, follow the link below to complete the action.</p>"+
//                "<a href=\"" +url+ "\">Reset password</a>"+
//                "<p> Daily Planner Portal Service";
//        
//        String mailContent = "<p> Hi, "+  ", </p>"+
//                "<p><b>You recently requested to reset your password,</b>"+"" +
//                "Please, follow the link below to complete the action.</p>"+
//                "<a href=\"" +url+ "\">Reset password</a>"+
//                "<p> Daily Planner Portal Service";
        
        String mailContent = " Hi " + user.getName() + ", " + " You recently requested to reset your password,  " + ""
				+ "\n" + "\n" + "Please, follow the link below to complete the action : " + "" + url+ "\" "
				+ "\n" + "\n" + "Reset password. ";
        
        emailMessage(subject, senderName, mailContent, mailSender, user);
    }

	private static void emailMessage(String subject, String senderName, String mailContent,
			JavaMailSender mailSender,
			User theUser) throws MessagingException, UnsupportedEncodingException {
		
		try {
			
			final String username = "nikitajawali99@gmail.com";
	        final String password = "rrai usmh clvs fwqu";

	        Properties prop = new Properties();
			prop.put("mail.smtp.host", "smtp.gmail.com");
	        prop.put("mail.smtp.port", "587");
	        prop.put("mail.smtp.auth", "true");
	        prop.put("mail.smtp.starttls.enable", "true"); //TLS
	        
	        Session session = Session.getInstance(prop,
	                new jakarta.mail.Authenticator() {
	                    protected PasswordAuthentication getPasswordAuthentication() {
	                        return new PasswordAuthentication(username, password);
	                    }
	                });
			
	        Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("from@gmail.com"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(theUser.getEmail())
            );
            message.setSubject(subject);
            message.setText(mailContent);

            Transport.send(message);
			
			
//		MimeMessage message = mailSender.createMimeMessage();
//		MimeMessageHelper messageHelper = new MimeMessageHelper(message);
//		
//		
//		messageHelper.setFrom("nikitajawali99@gmail.com", senderName);
//		messageHelper.setTo(theUser.getEmail());
//		messageHelper.setSubject(subject);
//		messageHelper.setText(mailContent, true);
//		
//		mailSender.send(message);
	
		}catch (Exception e) {
			e.printStackTrace();
		}

	}

}
