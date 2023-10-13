package com.dailyplanner.token;

import java.util.Optional;

import com.dailyplanner.entity.User;

public interface IVerificationTokenService {

	String validateToken(String token);

	Optional<VerificationToken> findByToken(String token);

}
