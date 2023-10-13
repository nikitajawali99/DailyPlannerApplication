package com.dailyplanner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dailyplanner.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	@Query("select p from User p where p.email=:email and p.isEnabled ='1' ")
	User findByEmail(@Param("email") String email);
	
	@Query("select p from User p where p.email=:email ")
	User findByExistingEmail(@Param("email") String email);

	User findByName(String name);

	Boolean existsByEmail(String email);

	@Query("select p from User p where p.email=:username")
	User findByUsername(@Param("username") String username);

	@Query("select p from User p where p.id=:id")
	User findByUserId(@Param("id") Long id);

}

