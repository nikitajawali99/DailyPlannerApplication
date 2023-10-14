package com.dailyplanner.password;

import java.util.Date;
import java.util.Optional;

import org.hibernate.validator.constraints.Mod10Check;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.dailyplanner.entity.User;

import jakarta.transaction.Transactional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

	Optional<PasswordResetToken> findByToken(String theToken);

//	@Transactional
//	@Modifying
//	@Query("Update PasswordResetToken p set p.id=:id,p.user_id=:userId,p.token=:token,p.expirationTime=:expirationTime  where p.id=:id")
//	void updateTokenDetails(Long id, Long userId, String token, Date expirationTime);

}
