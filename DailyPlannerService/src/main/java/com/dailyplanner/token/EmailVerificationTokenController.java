package com.dailyplanner.token;

import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dailyplanner.repository.ContactRepository;
import com.dailyplanner.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/registration")
public class EmailVerificationTokenController {
	
	private UserService userService;
	private final ContactRepository contactRepository;
	private final ApplicationEventPublisher publisher;
	private final IVerificationTokenService tokenService;
	
	public EmailVerificationTokenController(UserService userService,ContactRepository contactRepository,
			ApplicationEventPublisher publisher,IVerificationTokenService tokenService) {
		this.userService = userService;
		this.contactRepository=contactRepository;
		this.publisher=publisher;
		this.tokenService=tokenService;

	}
	
	
	///registration/verifyEmail?token
	
	@GetMapping("/verifyEmail")
	public String verifyEmail(@RequestParam("token") String token) {
		
		System.out.println("IN verifyEmail");
		
		Optional<VerificationToken> theToken = tokenService.findByToken(token);
		
		System.out.println(theToken);
		
		char tokenResult = theToken.get().getUser().isEnabled();
		
	
		if(theToken.isPresent() && tokenResult == '1' ) {
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


}
