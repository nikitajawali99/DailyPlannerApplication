package com.dailyplanner.password;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dailyplanner.dto.PasswordTokenDto;
import com.dailyplanner.entity.User;
import com.dailyplanner.repository.UserRepository;
import com.dailyplanner.token.TokenExpirationTime;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService implements IPasswordResetTokenService {

	Logger log = LoggerFactory.getLogger(PasswordResetTokenService.class);

	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public void createPasswordResetTokenForUser(User user, String passwordResetToken) {

		log.info("Entering into PasswordResetTokenService :: createPasswordResetTokenForUser");
		PasswordResetToken resetToken = new PasswordResetToken();
		resetToken.setExpirationTime(TokenExpirationTime.getExpirationTime());
		resetToken.setCreatedDate(new Date());
		resetToken.setUser(user);

		Long userId = user.getId();

		PasswordTokenDto passwordTokenDto = getPasswordResetId(userId);

		if (passwordTokenDto != null && passwordTokenDto.getId() != null) {
			resetToken.setId(passwordTokenDto.getId());
			resetToken.setToken(passwordResetToken);
		} else {
			resetToken = new PasswordResetToken();
			resetToken.setExpirationTime(TokenExpirationTime.getExpirationTime());
			resetToken.setCreatedDate(new Date());
			resetToken.setUser(user);
			resetToken.setToken(passwordResetToken);

		}
		resetToken = passwordResetTokenRepository.save(resetToken);
		log.info("Exiting into PasswordResetTokenService :: createPasswordResetTokenForUser");

	}

	private PasswordTokenDto getPasswordResetId(Long userId) {

		log.info("Entering into PasswordResetTokenService :: getPasswordResetId");

		List<PasswordTokenDto> passwordTokenDtoList = null;
		PasswordTokenDto passwordTokenDtoDto = null;

		StringBuilder sqlQuery = new StringBuilder(
				"SELECT t.id,t.user_id FROM " + "user_management.password_reset_token t left join users u "
						+ " on t.user_id=u.id where t.user_id=:userId");

		Query query = entityManager.createNativeQuery(sqlQuery.toString());

		query.setParameter("userId", userId);

		try {

			List<Object[]> obj = query.getResultList();
			passwordTokenDtoList = new ArrayList<>();

			if (!obj.isEmpty()) {

				for (Object[] record : obj) {

					passwordTokenDtoDto = new PasswordTokenDto();
					passwordTokenDtoDto.setId(Long.parseLong(String.valueOf(record[0])));
					passwordTokenDtoDto.setUserId(Long.parseLong(String.valueOf(record[1])));

					passwordTokenDtoList.add(passwordTokenDtoDto);
				}

			}
			log.info("Exiting into PasswordResetTokenService :: getPasswordResetId");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return passwordTokenDtoDto;

	}

	@Override
	public String validatePasswordResetToken(String theToken) {

		log.info("Entering into PasswordResetTokenService :: validatePasswordResetToken");
		Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepository.findByToken(theToken);
		if (passwordResetToken.isEmpty()) {
			log.info("Entering into PasswordResetTokenService :: invalid");
			return "invalid";
		}
		Calendar calendar = Calendar.getInstance();
		if ((passwordResetToken.get().getExpirationTime().getTime() - calendar.getTime().getTime()) <= 0) {
			log.info("Entering into PasswordResetTokenService :: expired");
			return "expired";
		}
		log.info("Exiting into PasswordResetTokenService :: validatePasswordResetToken");
		return "valid";
	}

	@Override
	public Optional<User> findUserByPasswordResetToken(String theToken) {
		return Optional.ofNullable(passwordResetTokenRepository.findByToken(theToken).get().getUser());
	}

	@Override
	public String resetPassword(User theUser, String password, String confirmpassword) {

		log.info("Entering into PasswordResetTokenService :: resetPassword");
		theUser.setPassword(passwordEncoder.encode(password));
		theUser.setConfirmPassword(passwordEncoder.encode(confirmpassword));

		if (password.equals(confirmpassword)) {

			userRepository.save(theUser);
		} else {
			return "redirect:/error?mismatch_password";
		}
		log.info("Exiting into PasswordResetTokenService :: resetPassword");
		return "redirect:/login?reset_success";
	}

}
