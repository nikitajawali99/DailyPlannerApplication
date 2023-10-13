package com.dailyplanner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.PathVariable;

import com.dailyplanner.entity.UsersRoles;


public interface UserRolesRepository extends JpaRepository<UsersRoles, Long> {

	@Query("select p.role_id from UsersRoles p where p.user_id=:id")
	Long findRoleId(@PathVariable("id") Long id);

}

