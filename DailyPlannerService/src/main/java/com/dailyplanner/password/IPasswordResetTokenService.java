package com.dailyplanner.password;

import java.util.Optional;

import com.dailyplanner.entity.User;

public interface IPasswordResetTokenService {

	void createPasswordResetTokenForUser(User user, String passwordResetToken);

	String validatePasswordResetToken(String theToken);

	Optional<User> findUserByPasswordResetToken(String theToken);

	String resetPassword(User theUser, String password, String confirmpassword);

}
