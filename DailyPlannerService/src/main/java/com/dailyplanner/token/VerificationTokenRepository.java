package com.dailyplanner.token;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;



public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long>{

	
	Optional<VerificationToken> findByToken(String token);

}
