package com.dailyplanner.service;

import java.util.List;
import java.util.Map;

import org.springframework.ui.Model;

import com.dailyplanner.dto.UserDto;
import com.dailyplanner.dto.UserRolesTokenDto;
import com.dailyplanner.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;


public interface UserService {
	
	Map<String, Object> createUser(UserDto user, Model model);

	UserDto getUserById(Long userId);

	List<UserDto> getAllUsers();

	UserDto updateUser(UserDto user);

	void deleteUser(Long userId);

	User saveUser(UserDto userDto);

	User findUserByEmail(String email);

	List<UserDto> findAllUsers();

	User findUserByuserName(String userName);

	List<UserRolesTokenDto> searchVerificationIds(Long userId);
	
	UserDto getStudentById(Long id);

	void updateUserDetails(@Valid UserDto userDto, HttpServletRequest request);

}
