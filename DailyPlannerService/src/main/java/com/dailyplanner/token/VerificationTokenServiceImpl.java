package com.dailyplanner.token;

import java.util.Calendar;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.dailyplanner.entity.User;
import com.dailyplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationTokenServiceImpl implements IVerificationTokenService {

	private final VerificationTokenRepository tokenRepository;
	private final UserRepository userRepository;

	@Override
	public String validateToken(String token) {

		Optional<VerificationToken> theToken = tokenRepository.findByToken(token);

		if (theToken.isEmpty()) {

			return "INVALID";
		}

		User user = theToken.get().getUser();
		Calendar calendar = Calendar.getInstance();
		if ((theToken.get().getExpirationTime().getTime() - calendar.getTime().getTime()) <= 0) {

			return "EXPIRED";
		}
		user.setEnabled('1');
		userRepository.save(user);

		return "VALID";
	}

	@Override
	public Optional<VerificationToken> findByToken(String token) {

		return tokenRepository.findByToken(token);
	}

}
