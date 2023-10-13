package com.dailyplanner.token;

import java.util.Date;

import com.dailyplanner.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class VerificationToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String token;

	private Date expirationTime;
	
	@OneToOne
    @JoinColumn(name = "user_id")
	private User user;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/mm/yyyy")
	@Column(nullable = false)
	private Date createdDate;

	public VerificationToken() {
		super();
		// TODO Auto-generated constructor stub
	}

	public VerificationToken(String token, User user,Date currentDate) {
		super();
		this.token = token;
		this.user = user;
		this.createdDate=currentDate;
		this.expirationTime = TokenExpirationTime.getExpirationTime();
		
	}

}
