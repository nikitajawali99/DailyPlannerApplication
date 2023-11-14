package com.dailyplanner.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import lombok.AllArgsConstructor;

@Configuration
@EnableMethodSecurity
@AllArgsConstructor
public class SpringSecurityConfig {
	
	//Logger log = LoggerFactory.getLogger(SpringSecurityConfig.class);

	private UserDetailsService userDetailsService;

	private CustomerSuccessHandler customerSucessHandler;

	@Bean
	public static PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// configure SecurityFilterChain
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
		//log.info("Entering into SpringSecurityConfig :: filterChain");

		http.csrf().disable().authorizeHttpRequests()
		        .requestMatchers("/register/**").permitAll()
				.requestMatchers("/index").permitAll()
				.requestMatchers("/about").permitAll()
				.requestMatchers("/error").permitAll()
				.requestMatchers("/contact/**").permitAll()
				.requestMatchers("/user-view").permitAll()
				.requestMatchers("/password-reset-form/**").permitAll()
				.requestMatchers("/login").hasAnyRole("USER", "ADMIN")
				.requestMatchers("/users").hasAnyRole("USER", "ADMIN")
				.requestMatchers("/todos/**").hasRole("USER")
				.requestMatchers("/changePassword").hasRole("USER")
				.requestMatchers("/registration/**").permitAll()
				.requestMatchers("/todos/getAllTodos").hasAnyRole("USER", "ADMIN")
				.requestMatchers("/users/**").hasAnyRole("USER", "ADMIN")
//				.requestMatchers("/user-view").hasRole("USER")
				.anyRequest().authenticated().and()
				.formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login")
				.successHandler(customerSucessHandler).permitAll())
				.logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout")).permitAll());

	//	log.info("Exiting into SpringSecurityConfig :: filterChain");
		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder builder) throws Exception {
		builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
	}

}
