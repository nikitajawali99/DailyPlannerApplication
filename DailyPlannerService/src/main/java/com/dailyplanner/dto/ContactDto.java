package com.dailyplanner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "ContactDto Model Information")
@ToString
public class ContactDto {

	@NotEmpty(message = "Email should not be null or empty")
	@Email(message = "Email should be valid")
	private String email;

	@NotEmpty(message = "Phone number should not be null or empty")
	@Size(min = 10, message = "Phone number is invalid", max = 10)
	private String number;

	@Size(min = 3, message = "Message cannot be less than 3 words")
	private String message;

	@Size(min = 3, message = "Name cannot be less than 3 words")
	@NotEmpty(message = "Name should not be null or empty")
	private String name;

}
