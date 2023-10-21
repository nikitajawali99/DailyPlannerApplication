package com.dailyplanner.token;

import java.util.Optional;

public interface IVerificationTokenService {

	String validateToken(String token);

	Optional<VerificationToken> findByToken(String token);

}
