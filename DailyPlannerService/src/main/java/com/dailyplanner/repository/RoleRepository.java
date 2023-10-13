package com.dailyplanner.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dailyplanner.entity.Role;


public interface RoleRepository extends JpaRepository<Role, Long> {

	Role findByName(String name);
}

