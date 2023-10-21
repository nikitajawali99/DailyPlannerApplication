package com.dailyplanner.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Service
public class CustomerSuccessHandler implements AuthenticationSuccessHandler {

	Logger log = LoggerFactory.getLogger(CustomerSuccessHandler.class);

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		log.info("Entering into CustomerSuccessHandler :: onAuthenticationSuccess");
		var authorities = authentication.getAuthorities();

		var roles = authorities.stream().map(r -> r.getAuthority()).findFirst();

		if (roles.orElse("").equals("ROLE_ADMIN")) {
			log.info("Entering into CustomerSuccessHandler :: admin");
			response.sendRedirect("/users");
		} else if (roles.orElse("").equals("ROLE_USER")) {
			log.info("Entering into CustomerSuccessHandler :: user");
			response.sendRedirect("/user-view");
		} else {
			log.info("Entering into CustomerSuccessHandler :: error");
			response.sendRedirect("/error");
		}

	}

}
