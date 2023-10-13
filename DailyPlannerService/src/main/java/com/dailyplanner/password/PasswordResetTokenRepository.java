package com.dailyplanner.password;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

	 Optional<PasswordResetToken> findByToken(String theToken);
	
	
}
