package com.dailyplanner.dto;

import lombok.ToString;

@ToString
public class UserRolesTokenDto {

	private Long userId;
	private Long userRoleId;
	private Long userVerificationTokenId;
	private Long userPasswordResetTokenId;
	private Long userTodoId;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getUserRoleId() {
		return userRoleId;
	}

	public void setUserRoleId(Long userRoleId) {
		this.userRoleId = userRoleId;
	}

	public Long getUserVerificationTokenId() {
		return userVerificationTokenId;
	}

	public void setUserVerificationTokenId(Long userVerificationTokenId) {
		this.userVerificationTokenId = userVerificationTokenId;
	}

	public Long getUserPasswordResetTokenId() {
		return userPasswordResetTokenId;
	}

	public void setUserPasswordResetTokenId(Long userPasswordResetTokenId) {
		this.userPasswordResetTokenId = userPasswordResetTokenId;
	}

	public UserRolesTokenDto(Long userId, Long userRoleId, Long userVerificationTokenId,
			Long userPasswordResetTokenId) {
		super();
		this.userId = userId;
		this.userRoleId = userRoleId;
		this.userVerificationTokenId = userVerificationTokenId;
		this.userPasswordResetTokenId = userPasswordResetTokenId;
	}

	public UserRolesTokenDto() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getUserTodoId() {
		return userTodoId;
	}

	public void setUserTodoId(Long userTodoId) {
		this.userTodoId = userTodoId;
	}

}
