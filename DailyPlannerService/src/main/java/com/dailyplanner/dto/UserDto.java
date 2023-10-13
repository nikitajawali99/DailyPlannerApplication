package com.dailyplanner.dto;


import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "UserDto Model Information")
@ToString
public class UserDto {
	private Long id;

	@NotEmpty(message = "User first name should not be null or empty")
	private String firstName;
	
	@NotEmpty(message = "User email should not be null or empty")
	@Email(message = "Email address should be valid")
	private String email;
	
	//@NotEmpty(message = "User password should not be null or empty")
	@Size(min = 5, max = 20, message = "Password must be between 5 to 20 characters")
	private String password;
	
	@Size(min = 5, max = 20, message = "Confirm Password must be between 5 to 20 characters")
	//@NotEmpty(message = "User confirm password should not be null or empty")
	private String confirmPassword;
	
	@Size(min=3,message = "User address cannot be less than 3 characters")
	@NotEmpty(message = "User address should not be null or empty")
	private String address;

	private String username;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/mm/yyyy")
	private Date createdDate;


}
