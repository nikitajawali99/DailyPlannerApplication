package com.dailyplanner.token;

import java.util.Calendar;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dailyplanner.entity.User;
import com.dailyplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationTokenServiceImpl implements IVerificationTokenService {

	Logger log = LoggerFactory.getLogger(VerificationTokenServiceImpl.class);

	private final VerificationTokenRepository tokenRepository;
	private final UserRepository userRepository;

	@Override
	public String validateToken(String token) {
		log.info("Entering into EmailVerificationTokenController :: validateToken");
		Optional<VerificationToken> theToken = tokenRepository.findByToken(token);

		if (theToken.isEmpty()) {
			log.info("Entering into EmailVerificationTokenController :: invalid");
			return "INVALID";
		}

		User user = theToken.get().getUser();
		Calendar calendar = Calendar.getInstance();
		if ((theToken.get().getExpirationTime().getTime() - calendar.getTime().getTime()) <= 0) {
			log.info("Entering into EmailVerificationTokenController :: expired");
			return "EXPIRED";
		}
		user.setEnabled('1');
		userRepository.save(user);
		log.info("Exiting into EmailVerificationTokenController :: validateToken");
		return "VALID";
	}

	@Override
	public Optional<VerificationToken> findByToken(String token) {
		log.info("Entering into EmailVerificationTokenController :: findByToken");
		return tokenRepository.findByToken(token);
	}

}
