package com.dailyplanner.token;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

	Optional<VerificationToken> findByToken(String token);

//	@Query("Delete from VerificationToken p where p.user_id=:userId")
//	void deleteByUserId(@Param("userId") Long userId);

}
