package com.dailyplanner.password;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dailyplanner.dto.PasswordTokenDto;
import com.dailyplanner.dto.TodoDto;
import com.dailyplanner.entity.User;
import com.dailyplanner.repository.UserRepository;
import com.dailyplanner.token.TokenExpirationTime;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService implements IPasswordResetTokenService {

	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public void createPasswordResetTokenForUser(User user, String passwordResetToken) {

		//PasswordResetToken resetToken = new PasswordResetToken(passwordResetToken, user);
		PasswordResetToken resetToken = new PasswordResetToken();
		resetToken.setExpirationTime(TokenExpirationTime.getExpirationTime());
		resetToken.setCreatedDate(new Date());
		resetToken.setUser(user);
		
		Long userId = user.getId();
		
		PasswordTokenDto passwordTokenDto  = getPasswordResetId(userId);
		
		if(passwordTokenDto!=null && passwordTokenDto.getId()!=null ) {
			resetToken.setId(passwordTokenDto.getId());
			resetToken.setToken(passwordResetToken);
//			passwordResetTokenRepository.updateTokenDetails(resetToken.getId(),userId,
//					resetToken.getToken(),resetToken.getExpirationTime()
//					);
			
			
		}
		else {
		resetToken = 	new PasswordResetToken();
		resetToken.setExpirationTime(TokenExpirationTime.getExpirationTime());
		resetToken.setCreatedDate(new Date());
		resetToken.setUser(user);
		resetToken.setToken(passwordResetToken);
		
		}
		resetToken =  passwordResetTokenRepository.save(resetToken);

	}

	private PasswordTokenDto getPasswordResetId(Long userId) {
		

			
			List<PasswordTokenDto> passwordTokenDtoList = null;
			PasswordTokenDto passwordTokenDtoDto = null;
			

			StringBuilder sqlQuery = new StringBuilder(
					"SELECT t.id,t.user_id FROM "
					+ "user_management.password_reset_token t left join users u " + " on t.user_id=u.id where t.user_id=:userId");

			Query query = entityManager.createNativeQuery(sqlQuery.toString());
			
			query.setParameter("userId", userId);
			
				try {
					
				List<Object[]> obj = query.getResultList();
				passwordTokenDtoList = new ArrayList<>();
				
				if(!obj.isEmpty()) {
			
					for (Object[] record : obj) {
						
						passwordTokenDtoDto = new PasswordTokenDto();
						passwordTokenDtoDto.setId(Long.parseLong(String.valueOf(record[0])));
						passwordTokenDtoDto.setUserId(Long.parseLong(String.valueOf(record[1])));
						
						
						passwordTokenDtoList.add(passwordTokenDtoDto);
					}
					
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
				return passwordTokenDtoDto;
			
				
			
		
		
		
	}

	@Override
	public String validatePasswordResetToken(String theToken) {

		Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepository.findByToken(theToken);
		if (passwordResetToken.isEmpty()) {
			return "invalid";
		}
		Calendar calendar = Calendar.getInstance();
		if ((passwordResetToken.get().getExpirationTime().getTime() - calendar.getTime().getTime()) <= 0) {
			return "expired";
		}
		return "valid";
	}

	@Override
	public Optional<User> findUserByPasswordResetToken(String theToken) {
		return Optional.ofNullable(passwordResetTokenRepository.findByToken(theToken).get().getUser());
	}

	@Override
	public String resetPassword(User theUser, String password, String confirmpassword) {

		theUser.setPassword(passwordEncoder.encode(password));
		theUser.setConfirmPassword(passwordEncoder.encode(confirmpassword));

		if (password.equals(confirmpassword)) {

			userRepository.save(theUser);
		} else {
			return "redirect:/error?mismatch_password";
		}
		return "redirect:/login?reset_success";
	}

}
