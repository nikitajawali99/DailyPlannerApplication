package com.dailyplanner.token;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dailyplanner.dto.UserDto;
import com.dailyplanner.entity.User;
import com.dailyplanner.event.RegistrationCompleteEvent;
import com.dailyplanner.event.RegistrationCompleteEventListener;
import com.dailyplanner.password.IPasswordResetTokenService;
import com.dailyplanner.repository.ContactRepository;
import com.dailyplanner.repository.UserRepository;
import com.dailyplanner.service.UserService;
import com.dailyplanner.utils.UrlUtil;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/registration")
public class EmailVerificationTokenController {

	private UserService userService;
	private final ContactRepository contactRepository;
	private final ApplicationEventPublisher publisher;
	private final IVerificationTokenService tokenService;
	private final IPasswordResetTokenService passwordResetTokenService;
	private final RegistrationCompleteEventListener eventListener;
	private final UserRepository userRepository;

	public EmailVerificationTokenController(UserService userService, ContactRepository contactRepository,
			ApplicationEventPublisher publisher, IVerificationTokenService tokenService,
			IPasswordResetTokenService passwordResetTokenService, 
			RegistrationCompleteEventListener eventListener,UserRepository userRepository) {
		this.userService = userService;
		this.contactRepository = contactRepository;
		this.publisher = publisher;
		this.tokenService = tokenService;
		this.passwordResetTokenService = passwordResetTokenService;
		this.eventListener = eventListener;
		this.userRepository=userRepository;

	}

	/// registration/verifyEmail?token

	@GetMapping("/verifyEmail")
	public String verifyEmail(@RequestParam("token") String token) {

		System.out.println("IN verifyEmail");

		Optional<VerificationToken> theToken = tokenService.findByToken(token);

		System.out.println(theToken);

		char tokenResult = theToken.get().getUser().getEnabled();

		if (theToken.isPresent() && tokenResult == '1') {
			return "redirect:/login?verified";
		}

		String verificationResult = tokenService.validateToken(token);
		switch (verificationResult.toLowerCase()) {
		case "expired":
			return "redirect:/error?expired";
		case "valid":
			return "redirect:/login?valid";
		default:
			return "redirect:/error?invalid";
		}

	}

	@GetMapping("/forgot-password-request")
	public String forgotPasswordForm(Model model) {
		
		UserDto user = new UserDto();
		model.addAttribute("user", user);
		
		return "forgot-password-form";
	}

	@PostMapping("/reset-password")
	public String resetPassword(HttpServletRequest request) {
		String theToken = request.getParameter("token");
		String password = request.getParameter("password");
		String confirmPassword = request.getParameter("confirmPassword");
		String tokenVerificationResult = passwordResetTokenService.validatePasswordResetToken(theToken);
		if (!tokenVerificationResult.equalsIgnoreCase("valid")) {
			return "redirect:/error?invalid_token";
		}
//	    	if (userDto.getConfirmPassword() != null && userDto.getPassword() != null) {
//				if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
//					result.rejectValue("password", null, "Password and Confirm Password should be same");
//				}
//			}
//
//			
//			if (result.hasErrors()) {
//				model.addAttribute("user", userDto);
//				return "/register";
//			}

		Optional<User> theUser = passwordResetTokenService.findUserByPasswordResetToken(theToken);
		if (theUser.isPresent()) {
			passwordResetTokenService.resetPassword(theUser.get(), password,confirmPassword);
			return "redirect:/login?reset_success";
		}
		return "redirect:/error?not_found";
	}

	@PostMapping("/forgot-password")
	public String resetPasswordRequest(HttpServletRequest request, Model model,@ModelAttribute("user") UserDto userDto) {

		String email = request.getParameter("email");
		User user = userRepository.findByEmail(email);
		
		System.out.println("UserDto :"+userDto);

		if (user == null) {
			return "redirect:/registration/forgot-password-request?not_found";
		}

		String passwordResetToken = UUID.randomUUID().toString();
		passwordResetTokenService.createPasswordResetTokenForUser(user, passwordResetToken);
		// send password reset verification email to the user
		String url = UrlUtil.getApplicationUrl(request) + "/password-reset-form?token="
				+ passwordResetToken;

		try {
			
			System.out.println("Hii before");
			
			eventListener.sendPasswordResetVerificationEmail(url,user);
			
			System.out.println("Hii after");
			//publisher.publishEvent(new RegistrationCompleteEvent(user, UrlUtil.getApplicationUrl(request)));

		} catch (Exception  e) {
			model.addAttribute("error", e.getMessage());
			e.printStackTrace();
		}

		return "redirect:/registration/forgot-password-request?success";
	}

}
