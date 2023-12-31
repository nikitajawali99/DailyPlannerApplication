package com.dailyplanner.password;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

import com.dailyplanner.entity.User;
import com.dailyplanner.token.TokenExpirationTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class PasswordResetToken {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String token;
	private Date expirationTime;
	@OneToOne
	@JoinColumn(name = "user_id", unique = false)
	private User user;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/mm/yyyy")
	@Column(nullable = false)
	private Date createdDate;

	public PasswordResetToken(String token, User user) {
		this.token = token;
		this.user = user;
		this.expirationTime = TokenExpirationTime.getExpirationTime();
	}

	public PasswordResetToken(Long id, String token, Date expirationTime, User user) {
		super();
		this.id = id;
		this.token = token;
		this.expirationTime = TokenExpirationTime.getExpirationTime();
		this.user = user;
	}

}
