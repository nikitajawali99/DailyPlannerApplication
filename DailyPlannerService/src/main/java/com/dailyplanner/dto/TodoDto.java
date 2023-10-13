package com.dailyplanner.dto;


import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@ToString
public class TodoDto {

    private Long id;
    @Size(min=3,message = "Todo title cannot be less than 3 words")
    @NotEmpty(message = "Todo title not be null or empty")
    private String title;
    @NotEmpty(message = "Todo description not be null or empty")
    @Size(min=3,message = "Todo description cannot be less than 3 words")
    private String description;
    private boolean completed;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private Date createdDate;
    //@NotEmpty(message = "Todo target date not be null or empty")
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date targetDate;
    private Long userId;
    private Long remainingDaysToComplete;
    //@NotEmpty(message = "Email address not be null or empty")
	//@Email(message = "Email address should be valid")
	//private String email;
}
