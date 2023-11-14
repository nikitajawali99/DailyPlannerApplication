package com.dailyplanner.controller;

import java.security.Principal;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.dailyplanner.controller.AuthController;
import com.dailyplanner.dto.TodoDto;
import com.dailyplanner.dto.UserDto;
import com.dailyplanner.dto.UserRolesTokenDto;
import com.dailyplanner.entity.User;
import com.dailyplanner.event.RegistrationCompleteEvent;
import com.dailyplanner.repository.UserRepository;
import com.dailyplanner.repository.UserRolesRepository;
import com.dailyplanner.service.TodoService;
import com.dailyplanner.service.UserService;
import com.dailyplanner.token.VerificationTokenRepository;
import com.dailyplanner.utils.UrlUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Controller
@CrossOrigin("*")
public class AuthController {

	Logger log = LoggerFactory.getLogger(AuthController.class);

	private UserService userService;
	private TodoService todoService;
	private UserDetailsService userDetailsService;
	private UserRepository userRepository;
	private UserRolesRepository userRolesRepository;
	private final PasswordEncoder passwordEncoder;
	private final ApplicationEventPublisher publisher;
	private final VerificationTokenRepository verificationRepository;

	public AuthController(UserService userService, TodoService todoService, UserDetailsService userDetailsService,
			UserRepository userRepository, UserRolesRepository userRolesRepository, PasswordEncoder passwordEncoder,
			ApplicationEventPublisher publisher,VerificationTokenRepository verificationRepository) {
		this.userService = userService;
		this.todoService = todoService;
		this.userDetailsService = userDetailsService;
		this.userRepository = userRepository;
		this.userRolesRepository = userRolesRepository;
		this.passwordEncoder = passwordEncoder;
		this.publisher = publisher;
		this.verificationRepository=verificationRepository;
	}

//	@PreAuthorize("hasRole('USER')")
	@GetMapping("/user-view")
	public String userView(Model model, Principal principal) throws ParseException {

		log.info("Entering into AuthController :: userView");
		UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());

		User user = userService.findUserByEmail(userDetails.getUsername());

		if (user == null) {
			return "redirect:/error?invalid";
		}
		
		//Date date = new Date();  
//	    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");  
//	    String strDate = formatter.format(user.getCreatedDate());  
//	    Date date1=new SimpleDateFormat("dd/MM/yyyy").parse(strDate);  
//	    
//	    System.out.println("Date format :"+date1);
//	    
//		user.setCreatedDate(date1);
		model.addAttribute("user", user);
		log.info("Exiting into AuthController :: userView");
		return "user-view";
	}

	// handler method to handle list of users
	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	@GetMapping("/users")
	public String users(Model model) {

		log.info("Entering into AuthController :: users");
		List<UserDto> users = userService.findAllUsers();
		model.addAttribute("users", users);
		log.info("Exiting into AuthController :: users");
		return "users";
	}

	// handler method to handle edit student request
	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	@GetMapping("/users/{id}/view")
	public String view(@PathVariable("id") Long id, Model model) {

		log.info("Entering into AuthController :: view");
		List<TodoDto> users = todoService.getUserTodoById(id);
		model.addAttribute("users", users);
		log.info("Exiting into AuthController :: view");
		return "todo-view";
	}

	// handler method to handle edit student request
	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	@GetMapping("/users/{id}/userview")
	public String userview(@PathVariable("id") Long id, Model model) {

		log.info("Entering into AuthController :: userview");
		List<TodoDto> users = todoService.getUserTodoById(id);
		model.addAttribute("users", users);
		log.info("Exiting into AuthController :: userview");
		return "user-todo";
	}

	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	@GetMapping("/editTodo/{id}")
	public String editTodo(@PathVariable("id") Long id, Model model) {

		log.info("Entering into AuthController :: editTodo");
		TodoDto user = todoService.updateTodoById(id);

		model.addAttribute("user", user);
		log.info("Exiting into AuthController :: editTodo");
		return "edit-todo";
	}

	// handler method to handle edit student request
	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	@GetMapping("/users/{id}/edit")
	public String editStudent(@PathVariable("id") Long id, Model model) {
		log.info("Entering into AuthController :: editStudent");
		UserDto user = userService.getStudentById(id);

		model.addAttribute("user", user);
		log.info("Exiting into AuthController :: editStudent");
		return "edit-user";
	}

	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	@PostMapping("/todo/{id}")
	public String updateTodo(@PathVariable("id") Long id, @Valid @ModelAttribute("user") TodoDto userDto,
			BindingResult result, Model model) {

		log.info("Entering into AuthController :: updateTodo");

		if (result.hasErrors()) {

			model.addAttribute("user", userDto);
			return "edit-todo";
		} else {
			userDto.setId(id);
			todoService.updateTodo(userDto, id);
			log.info("Exiting into AuthController :: updateTodo");
			return "redirect:/user-view";
		}

	}

	// handler method to handle edit student form submit request
	@PreAuthorize("hasRole('USER')")
	@PostMapping("/users/{id}")
	@Transactional
	public String updateStudent(@PathVariable("id") Long id, @Valid @ModelAttribute("user") UserDto userDto,
			BindingResult result, Model model,HttpServletRequest request) {
		User existingId=null;
		try {
			User existingEmail = userService.findUserByEmail(userDto.getEmail());
			log.info("Entering into AuthController :: updateStudent");
			 existingId = userRepository.findByUserId(id);

			if (existingId.getEmail().equals(userDto.getEmail())) {

				userDto.setId(id);

			} else {

				
				if (existingEmail != null && existingEmail.getEmail() != null && !existingEmail.getEmail().isEmpty()) {
					result.rejectValue("email", null, "There is already an account registered with the same email");
				}
			}

			if (result.hasErrors()) {
				log.info("Entering into updateStudent :: errors");
				model.addAttribute("user", userDto);
				return "edit-user";
			} else {
				log.info("Entering into updateStudent :: updateUser");
								
				User user =new User();
				user.setId(existingId.getId());
				user.setName(userDto.getFirstName());
				user.setAddress(userDto.getAddress());
				user.setEmail(userDto.getEmail());
				existingId = userRepository.findByUserId(id);
				System.out.println(user.getEmail());
				System.out.println(existingId.getEmail());
				User updateNotEnable = null; 
				if(!existingId.getEmail().equals(user.getEmail())) {
					
					List<UserRolesTokenDto> verificationTokenIds=userService.searchVerificationIds(user.getId());
					
					for (UserRolesTokenDto userRolesTokenDto : verificationTokenIds) {
						verificationRepository.deleteById(userRolesTokenDto.getUserId());
					}
					//
					 userRepository.updateNotEnable(user.getId());
					
					publisher.publishEvent(new RegistrationCompleteEvent(user, UrlUtil.getApplicationUrl(request)));				
				}
				
				Optional<User> userdls = userRepository.findById(id);
				if (userdls.get().getEnabled() == '1') {

					UserDto updateUser = userService.updateUser(userDto);
				}

				
				log.info("Exiting into updateStudent :: updateUser");
			}

//			Long roleId = userRolesRepository.findRoleId(id);
//
//			if (roleId == 2) {
//				return "redirect:/login?success";
//			}
			log.info("Exiting into AuthController :: updateStudent");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "redirect:/login?success";
	}

	// Handler method to handle delete student request
	@GetMapping("/users/{id}/delete")
	public String deleteUser(@PathVariable("id") Long userId) {
		log.info("Entering into AuthController :: deleteUser");
		userService.deleteUser(userId);
		log.info("Exiting into AuthController :: deleteUser");
		return "redirect:/users";
	}

	@PreAuthorize("hasRole('USER')")
	@GetMapping("/change-password")
	public String changePassword(Model model, Principal principal) {

		log.info("Entering into AuthController :: changePassword");
		UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());

		User user = userService.findUserByEmail(userDetails.getUsername());

		if (user == null) {
			return "redirect:/error?invalid";
		}
		model.addAttribute("user", user);
		log.info("Exiting into AuthController :: changePassword");
		return "change-password";
	}

	@PostMapping("/changepassword/save")
	public String changePasswordSave(@Valid @ModelAttribute("user") User user, BindingResult result, Model model,
			Principal principal) {

		try {
			log.info("Entering into AuthController :: changePasswordSave");
			UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());

			User existingUser = userService.findUserByEmail(userDetails.getUsername());

			if (user.getConfirmPassword() == "") {
				result.rejectValue("confirmPassword", null, "Confirm Password cannot be null");
			}

			if (user.getPassword() == "") {
				result.rejectValue("password", null, "Password cannot be null");
			}

			if (user.getConfirmPassword() != null && user.getPassword() != null) {
				if (!user.getPassword().equals(user.getConfirmPassword())) {
					result.rejectValue("password", null, "Password and Confirm Password should be same");
					result.rejectValue("confirmPassword", null, "Password and Confirm Password should be same");
				}
			}

			if (result.hasErrors()) {
				model.addAttribute("user", user);
				return "/change-password";
			} else {
				log.info("Entering into changePasswordSave :: updating-password");
				existingUser.setId(existingUser.getId());
				existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
				existingUser.setConfirmPassword(passwordEncoder.encode(user.getConfirmPassword()));
				existingUser.setActive('1');
				userRepository.save(existingUser);
				log.info("Exiting into changePasswordSave :: updating-password");
				return "redirect:/change-password?success";
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
		log.info("Exiting into AuthController :: changePasswordSave");
		return null;

	}

}
