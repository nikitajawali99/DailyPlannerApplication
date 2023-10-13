package com.dailyplanner.config;

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
	
	
	private UserDetailsService userDetailsService;

	private CustomerSuccessHandler customerSucessHandler;

	@Bean
	public static PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	    
	    // configure SecurityFilterChain
	    @Bean
	    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    
	        http.
	        csrf().disable()
	                .authorizeHttpRequests()
	                .requestMatchers("/register/**").permitAll()
	                .requestMatchers("/index").permitAll()
	                .requestMatchers("/error").permitAll()
	                .requestMatchers("/contact").permitAll()
	                .requestMatchers("/login").hasAnyRole("USER","ADMIN")
	                .requestMatchers("/users").hasAnyRole("USER","ADMIN")  
	                .requestMatchers("/todos/**").hasRole("USER")
	                .requestMatchers("/registration/**").permitAll()
	                .requestMatchers("/todos/getAllTodos").hasAnyRole("USER","ADMIN")  
	                .requestMatchers("/users/**").hasAnyRole("USER","ADMIN")  
	                .anyRequest().authenticated()
	                .and()
	                
	                .formLogin(
	                        form -> form
	                                .loginPage("/login")
	                                .loginProcessingUrl("/login")
	                                .successHandler(customerSucessHandler)                             
	                                .permitAll())
	                
	                .logout(
	                        logout -> logout
	                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
	                                .permitAll()
	                );
	       
	        return http.build();
	    }
	
//	    @Bean
//	    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//	    	
//	        http.csrf().disable()
//            .authorizeHttpRequests((authorize) -> {
////              authorize.requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN");
////              authorize.requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN");
////              authorize.requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN");
////              authorize.requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "USER");
////              authorize.requestMatchers(HttpMethod.PATCH, "/api/**").hasAnyRole("ADMIN", "USER");
////              authorize.requestMatchers(HttpMethod.GET, "/api/**").permitAll();
//            	 authorize.anyRequest().authenticated();
//            }).httpBasic(Customizer.withDefaults());
//    return http.build();
//	 }
	    
//	    @Bean
//	    public UserDetailsService userDetailsService(){
//
//	        UserDetails ramesh = User.builder()
//	                .username("ramesh")
//	                .password(passwordEncoder().encode("password"))
//	                .roles("USER")
//	                .build();
//
//	        UserDetails admin = User.builder()
//	                .username("admin")
//	                .password(passwordEncoder().encode("admin"))
//	                .roles("ADMIN")
//	                .build();
//
//	        return new InMemoryUserDetailsManager(ramesh, admin);
//	    }
	   
	    @Bean
	    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
	        return configuration.getAuthenticationManager();
	    }
	   
	    
	    @Autowired
	    public void configureGlobal(AuthenticationManagerBuilder builder) throws Exception {
	        builder.userDetailsService(userDetailsService)
	                .passwordEncoder(passwordEncoder());
	    }

		
		
	


}
