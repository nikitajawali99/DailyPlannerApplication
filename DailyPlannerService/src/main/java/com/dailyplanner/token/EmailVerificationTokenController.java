package com.dailyplanner.token;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dailyplanner.dto.UserDto;
import com.dailyplanner.entity.User;
import com.dailyplanner.event.RegistrationCompleteEventListener;
import com.dailyplanner.password.IPasswordResetTokenService;
import com.dailyplanner.repository.UserRepository;
import com.dailyplanner.utils.UrlUtil;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/registration")
public class EmailVerificationTokenController {

	Logger log = LoggerFactory.getLogger(EmailVerificationTokenController.class);

	private final IVerificationTokenService tokenService;
	private final IPasswordResetTokenService passwordResetTokenService;
	private final RegistrationCompleteEventListener eventListener;
	private final UserRepository userRepository;

	public EmailVerificationTokenController(IVerificationTokenService tokenService,
			IPasswordResetTokenService passwordResetTokenService, RegistrationCompleteEventListener eventListener,
			UserRepository userRepository) {

		this.tokenService = tokenService;
		this.passwordResetTokenService = passwordResetTokenService;
		this.eventListener = eventListener;
		this.userRepository = userRepository;

	}

	/// registration/verifyEmail?token
	@GetMapping("/verifyEmail")
	public String verifyEmail(@RequestParam("token") String token) {

		log.info("Entering into EmailVerificationTokenController :: verifyEmail");
		Optional<VerificationToken> theToken = tokenService.findByToken(token);

		char tokenResult = theToken.get().getUser().getEnabled();

		if (theToken.isPresent() && tokenResult == '1') {
			log.info("Entering into EmailVerificationTokenController :: already verified");
			return "redirect:/login?verified";
		}

		String verificationResult = tokenService.validateToken(token);
		switch (verificationResult.toLowerCase()) {
		case "expired":
			log.info("Entering into EmailVerificationTokenController :: expired");
			return "redirect:/error?expired";

		case "valid":
			log.info("Entering into EmailVerificationTokenController :: valid");
			return "redirect:/login?valid";
		default:
			log.info("Entering into EmailVerificationTokenController :: default");
			return "redirect:/error?invalid";
		}

	}

	@GetMapping("/forgot-password-request")
	public String forgotPasswordForm(Model model) {
		log.info("Entering into EmailVerificationTokenController :: forgotPasswordForm");
		UserDto user = new UserDto();
		model.addAttribute("user", user);
		log.info("Exiting into EmailVerificationTokenController :: forgotPasswordForm");
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
			String resetPassword = passwordResetTokenService.resetPassword(theUser.get(), password, confirmPassword);

			if (resetPassword.equals("redirect:/error?mismatch_password")) {
				return "redirect:/error?mismatch_password";
			}

		}
		return "redirect:/login?reset_success";
	}

	@PostMapping("/forgot-password")
	public String resetPasswordRequest(HttpServletRequest request, Model model,
			@ModelAttribute("user") UserDto userDto) {

		log.info("Entering into EmailVerificationTokenController :: resetPasswordRequest");
		String email = request.getParameter("email");
		User user = userRepository.findByEmail(email);

		if (user == null) {
			return "redirect:/registration/forgot-password-request?not_found";
		}

		String passwordResetToken = UUID.randomUUID().toString();
		passwordResetTokenService.createPasswordResetTokenForUser(user, passwordResetToken);
		// send password reset verification email to the user
		String url = UrlUtil.getApplicationUrl(request) + "/password-reset-form?token=" + passwordResetToken;

		log.info("Sending Url:" + url);
		try {
			log.info("Entering into EmailVerificationTokenController :: sendPasswordResetVerificationEmail");
			eventListener.sendPasswordResetVerificationEmail(url, user);
			log.info("Exiting into EmailVerificationTokenController :: sendPasswordResetVerificationEmail");
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			e.printStackTrace();
		}
		log.info("Exiting into EmailVerificationTokenController :: resetPasswordRequest");
		return "redirect:/registration/forgot-password-request?success";
	}

}
