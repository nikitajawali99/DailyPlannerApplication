package com.dailyplanner.controller;

import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dailyplanner.dto.ContactDto;
import com.dailyplanner.dto.UserDto;
import com.dailyplanner.entity.*;
import com.dailyplanner.event.RegistrationCompleteEvent;
import com.dailyplanner.password.IPasswordResetTokenService;
import com.dailyplanner.repository.ContactRepository;
import com.dailyplanner.service.UserService;
import com.dailyplanner.utils.UrlUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@CrossOrigin("*")
public class LoginRegisterationController {

	Logger log = LoggerFactory.getLogger(LoginRegisterationController.class);

	private UserService userService;
	private final ContactRepository contactRepository;
	private final ApplicationEventPublisher publisher;
	private final IPasswordResetTokenService passwordResetTokenService;

	public LoginRegisterationController(UserService userService, ContactRepository contactRepository,
			ApplicationEventPublisher publisher, IPasswordResetTokenService passwordResetTokenService) {
		this.userService = userService;
		this.contactRepository = contactRepository;
		this.publisher = publisher;
		this.passwordResetTokenService = passwordResetTokenService;

	}

	@GetMapping("/index")
	public String home() {
		log.info("Entering into LoginRegisterationController :: index");
		return "index";
	}

	@GetMapping("/about")
	public String about() {
		log.info("Entering into LoginRegisterationController :: about");
		return "about";
	}

	@GetMapping("/error")
	public String error() {
		log.info("Entering into LoginRegisterationController :: error");
		return "error";
	}

	@GetMapping("/login")
	public String login(Model model) {
		log.info("Entering into LoginRegisterationController :: login");
		UserDto user = new UserDto();
		model.addAttribute("user", user);
		log.info("Exiting into LoginRegisterationController :: login");
		return "login";
	}

	@GetMapping("/contact")
	public String contact(Model model) {
		log.info("Entering into LoginRegisterationController :: contact");
		ContactDto contact = new ContactDto();
		model.addAttribute("contact", contact);
		log.info("Exiting into LoginRegisterationController :: contact");
		return "contact";
	}

	@PostMapping("/contact/save")
	public String contactSave(@Valid @ModelAttribute("contact") ContactDto contact, BindingResult result, Model model) {

		try {

			log.info("Entering into LoginRegisterationController :: contactSave");

			if (result.hasErrors()) {
				model.addAttribute("contact", contact);
				return "/contact";
			} else {

				Contact c = new Contact();
				c.setName(contact.getName());
				c.setEmail(contact.getEmail());
				c.setCreatedDate(new Date());
				c.setNumber(contact.getNumber());
				c.setMessage(contact.getMessage());
				contactRepository.save(c);
				log.info("Exiting into LoginRegisterationController :: contactSave");
				return "redirect:/contact?success";
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
		return null;
	}

	// handler method to handle user registration form request
	@GetMapping("/register")
	public String showRegistrationForm(Model model) {

		log.info("Entering into LoginRegisterationController :: showRegistrationForm");

		UserDto user = new UserDto();
		model.addAttribute("user", user);

		log.info("Exiting into LoginRegisterationController :: showRegistrationForm");
		return "register";
	}

	// handler method to handle user registration form submit request
	@PostMapping("/register/save")
	public String registration(@Valid @ModelAttribute("user") UserDto userDto, BindingResult result, Model model,
			HttpServletRequest request) {

		try {

			log.info("Entering into LoginRegisterationController :: registration");
			User existingEmail = userService.findUserByEmail(userDto.getEmail());

			if (existingEmail != null && existingEmail.getEmail() != null && !existingEmail.getEmail().isEmpty()) {
				result.rejectValue("email", null, "There is already an account registered with the same email");
			}

			if (userDto.getConfirmPassword() != null && userDto.getPassword() != null) {
				if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
					result.rejectValue("password", null, "Password and Confirm Password should be same");
					result.rejectValue("confirmPassword", null, "Password and Confirm Password should be same");
				}
			}

			log.info("Entering into LoginRegisterationController :: hasErrors");
			if (result.hasErrors()) {
				model.addAttribute("user", userDto);
				return "/register";
			}

			else {
				User user = userService.saveUser(userDto);

				// publish verification-email event
				publisher.publishEvent(new RegistrationCompleteEvent(user, UrlUtil.getApplicationUrl(request)));

				log.info("Exiting into LoginRegisterationController :: registration");
				return "redirect:/register?success";
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
		return null;
	}

	@GetMapping("/password-reset-form")
	public String passwordResetForm(@RequestParam("token") String token, Model model) {
		log.info("Entering into LoginRegisterationController :: passwordResetForm");
		model.addAttribute("token", token);
		return "password-reset-form";
	}

	@PostMapping("/reset-password")
	public String resetPassword(HttpServletRequest request) {

		log.info("Entering into LoginRegisterationController :: resetPassword");

		String theToken = request.getParameter("token");
		String password = request.getParameter("password");
		String confirmpassword = request.getParameter("confirmPassword");

		if (!password.equals(confirmpassword)) {
			return "redirect:/error?mismatch_password";

		}

		String tokenVerificationResult = passwordResetTokenService.validatePasswordResetToken(theToken);
		if (!tokenVerificationResult.equalsIgnoreCase("valid")) {
			return "redirect:/error?invalid_token";
		}
		Optional<User> theUser = passwordResetTokenService.findUserByPasswordResetToken(theToken);
		if (theUser.isPresent()) {
			passwordResetTokenService.resetPassword(theUser.get(), password, confirmpassword);
			return "redirect:/login?reset_success";
		}
		log.info("Exiting into LoginRegisterationController :: resetPassword");
		return "redirect:/error?not_found";
	}

}
