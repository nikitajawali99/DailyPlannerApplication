package com.dailyplanner.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Service
public class CustomerSuccessHandler implements AuthenticationSuccessHandler{


	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		var authorities = authentication.getAuthorities();
		
		var roles = authorities.stream().map(r -> r.getAuthority()).findFirst();
		
		if(roles.orElse("").equals("ROLE_ADMIN")) {
			response.sendRedirect("/users");		
		}else if(roles.orElse("").equals("ROLE_USER")) {
			response.sendRedirect("/user-view");	
		}else {
			response.sendRedirect("/error");
		}
		
	}

}
