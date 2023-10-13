package com.dailyplanner.service.Impl;


import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.dailyplanner.constant.Constant;
import com.dailyplanner.dto.UserDto;
import com.dailyplanner.entity.Role;
import com.dailyplanner.entity.User;
import com.dailyplanner.exception.ResourceNotFoundException;
import com.dailyplanner.repository.RoleRepository;
import com.dailyplanner.repository.UserRepository;
import com.dailyplanner.service.UserService;

import jakarta.transaction.Transactional;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

	Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;


	public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder,ModelMapper modelMapper) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.modelMapper=modelMapper;
		
	}


	private final ModelMapper modelMapper;

	boolean isValid = true;

	@Override
	@Transactional
	public Map<String, Object> createUser(UserDto userDto, Model model) {
		log.info("Entering into UserServiceImpl :: createUser");
		Map<String, Object> response = new HashMap<>();
		try {
			if (userDto.getId() == null) {
				User optionalEmail = userRepository.findByEmail(userDto.getEmail());

				if (optionalEmail!=null) {
					
					isValid = false;
					response.put(Constant.FAILED, 0);
					response.put(Constant.MESSAGE, "Email-Id already exists :Already Registered ?");
					

				}
			}

			if (isValid) {
				response = saveUser(userDto, model);

				return response;
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.put(Constant.FAILED, 0);
			response.put(Constant.MESSAGE, "SOMETHING_WENT_WRONG");
		}
		log.info("Exiting into UserServiceImpl :: createUser");
		return response;
	}

	@Transactional
	private Map<String, Object> saveUser(UserDto userDto, Model model) {
		Map<String, Object> response = new HashMap<>();
		User user = modelMapper.map(userDto, User.class);

		if (!userDto.getPassword().equals(user.getConfirmPassword())) {
			response.put(Constant.FAILED, 0);
			response.put(Constant.MESSAGE, "Password and Confirm Password are not same");
		} else {

			user.setCreatedDate(new Date());
			User savedUser = userRepository.save(user);

			UserDto savedUserDto = modelMapper.map(savedUser, UserDto.class);
			response.put(Constant.DATA, savedUserDto);
			response.put(Constant.SUCCESS, 1);

			model.addAttribute("userForm", response);

			getSuccess(model);

		}
		return response;
	}

	private String getSuccess(Model model) {
		return "register-success";
		// TODO Auto-generated method stub

	}

	@Override
	@Transactional
	public UserDto getUserById(Long userId) {
		User user = null;
		try {

			user = userRepository.findById(userId)
					.orElseThrow(() -> new ResourceNotFoundException("User with ", "id :", userId));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return modelMapper.map(user, UserDto.class);

	}

	@Override
	@Transactional
	public List<UserDto> getAllUsers() {
		List<User> users = null;
		try {
			users = userRepository.findAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return users.stream().map((user) -> modelMapper.map(user, UserDto.class)).collect(Collectors.toList());
	}

	@Override
	public UserDto updateUser(UserDto user) {

		User existingUser = userRepository.findById(user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", user.getId()));

		existingUser.setName(user.getFirstName());
		
		existingUser.setEmail(user.getEmail());
		existingUser.setAddress(user.getAddress());
	
		
		User updatedUser = userRepository.save(existingUser);
  

		return modelMapper.map(updatedUser, UserDto.class);
	}

	@Override
	public void deleteUser(Long userId) {
		try {
			userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

			
			//userRolesRepository.deleteById(userId);
			userDelete(userId);
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Transactional
	@Modifying
	private void userDelete(Long userId) {
		try {
		
		userRepository.deleteById(userId);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	@Transactional
	public User saveUser(UserDto userDto) {
		try {
			log.info("Entering into UserServiceImpl :: saveUser");
			User user = new User();
			user.setName(userDto.getFirstName());
		
			user.setEmail(userDto.getEmail());
			// encrypt the password using spring security
			user.setPassword(passwordEncoder.encode(userDto.getPassword()));
			user.setConfirmPassword(passwordEncoder.encode(userDto.getConfirmPassword()));
			user.setAddress(userDto.getAddress());
		
			user.setCreatedDate(new Date());
			Role role = roleRepository.findByName("ROLE_USER");
			if (role == null) {
				role = checkRoleExist();
			}
			user.setEnabled('0');
			user.setRoles(Arrays.asList(role));
			log.info("Exiting into UserServiceImpl :: saveUser");
			userRepository.save(user);
			
			return user;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public User findUserByEmail(String email) {
		return userRepository.findByExistingEmail(email);
	}

	@Override
	public List<UserDto> findAllUsers() {
		List<User> users = userRepository.findAll();
		return users.stream().map((user) -> mapToUserDto(user)).collect(Collectors.toList());
	}

	private UserDto mapToUserDto(User user) {
		UserDto userDto = null;
		try {
			userDto = new UserDto();
			userDto.setId(user.getId());
			userDto.setFirstName(user.getName());
			userDto.setEmail(user.getEmail());
			userDto.setCreatedDate(user.getCreatedDate());
			userDto.setAddress(user.getAddress());
//			List<Todo> todoList = todoRepository.findUserId(userDto.getId());
//			
//			for (Todo todo : todoList) {
//				userDto.setTitle(todo.getTitle());
//			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userDto;
	}

	@Transactional
	private Role checkRoleExist() {
		Role role = new Role();
		role.setName("ROLE_USER");
		return roleRepository.save(role);
	}

	@Override
	public User findUserByuserName(String userName) {
		return null;
	}

	@Override
	public UserDto getStudentById(Long id) {
		User userDto = userRepository.findById(id).get();
		
		UserDto savedUserDto = new UserDto();
	
		savedUserDto.setId(userDto.getId());
		savedUserDto.setFirstName(userDto.getName());
		savedUserDto.setEmail(userDto.getEmail());
		savedUserDto.setAddress(userDto.getAddress());
		savedUserDto.setPassword(userDto.getPassword());
		savedUserDto.setConfirmPassword(userDto.getConfirmPassword());
		//savedUserDto.setUserName(userDto.getUserName());
		
		return savedUserDto;
	}
}
