package com.dailyplanner.service.Impl;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.dailyplanner.constant.Constant;
import com.dailyplanner.dto.TodoDto;
import com.dailyplanner.dto.UserDto;
import com.dailyplanner.dto.UserRolesTokenDto;
import com.dailyplanner.entity.Role;
import com.dailyplanner.entity.User;
import com.dailyplanner.exception.ResourceNotFoundException;
import com.dailyplanner.password.PasswordResetTokenRepository;
import com.dailyplanner.repository.RoleRepository;
import com.dailyplanner.repository.TodoRepository;
import com.dailyplanner.repository.UserRepository;
import com.dailyplanner.repository.UserRolesRepository;
import com.dailyplanner.service.UserService;
import com.dailyplanner.token.VerificationTokenRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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
	private final VerificationTokenRepository verificationRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final TodoRepository todoRepository;

	@PersistenceContext
	private EntityManager entityManager;

	public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder, ModelMapper modelMapper,
			VerificationTokenRepository verificationRepository,
			PasswordResetTokenRepository passwordResetTokenRepository, TodoRepository todoRepository) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.modelMapper = modelMapper;
		this.verificationRepository = verificationRepository;
		this.passwordResetTokenRepository = passwordResetTokenRepository;
		this.todoRepository = todoRepository;
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

				if (optionalEmail != null) {

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
		log.info("Entering into UserServiceImpl :: saveUser");
		Map<String, Object> response = new HashMap<>();
		User user = modelMapper.map(userDto, User.class);

		if (!userDto.getPassword().equals(user.getConfirmPassword())) {
			response.put(Constant.FAILED, 0);
			response.put(Constant.MESSAGE, "Password and Confirm Password are not same");
		} else {
			Date date = new Date();
			LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			user.setActive('1');
			user.setCreatedDate(localDate);
			User savedUser = userRepository.save(user);

			UserDto savedUserDto = modelMapper.map(savedUser, UserDto.class);
			response.put(Constant.DATA, savedUserDto);
			response.put(Constant.SUCCESS, 1);

			model.addAttribute("userForm", response);

			getSuccess(model);

		}
		log.info("Exiting into UserServiceImpl :: saveUser");
		return response;
	}

	private String getSuccess(Model model) {
		return "register-success";
	}

	@Override
	@Transactional
	public UserDto getUserById(Long userId) {
		log.info("Entering into UserServiceImpl :: getUserById");
		User user = null;
		try {

			user = userRepository.findById(userId)
					.orElseThrow(() -> new ResourceNotFoundException("User with ", "id :", userId));
			log.info("Exiting into UserServiceImpl :: getUserById");
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
		log.info("Entering into UserServiceImpl :: updateUser");
		User existingUser = userRepository.findById(user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", user.getId()));

		existingUser.setName(user.getFirstName());

		existingUser.setEmail(user.getEmail());
		existingUser.setAddress(user.getAddress());
		existingUser.setActive('1');
		User updatedUser = userRepository.save(existingUser);
		log.info("Exiting into UserServiceImpl :: updateUser");
		return modelMapper.map(updatedUser, UserDto.class);
	}

	@Override
	@Transactional
	@Modifying
	public void deleteUser(Long userId) {
		try {
			log.info("Entering into UserServiceImpl :: deleteUser");
			userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

			List<UserRolesTokenDto> todoDtoList = searchByUserId(userId);

			for (UserRolesTokenDto todo : todoDtoList) {

				if (todo.getUserVerificationTokenId() != null)
					verificationRepository.deleteById(todo.getUserVerificationTokenId());
				if (todo.getUserPasswordResetTokenId() != null)
					passwordResetTokenRepository.deleteById(todo.getUserPasswordResetTokenId());
				if (todo.getUserTodoId() != null)
					todoRepository.deleteById(todo.getUserTodoId());

			}
			
			userRepository.updateNotActive(userId);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<UserRolesTokenDto> searchByUserId(Long userId) {

		List<UserRolesTokenDto> todoDtoList = null;
		UserRolesTokenDto todoDto = null;

		StringBuilder sqlQuery = new StringBuilder("SELECT t.id,v.id,p.id,u.user_id,todo.id  \r\n"
				+ " FROM user_management.users t \r\n" + " left join verification_token v on t.id=v.user_id\r\n"
				+ " left join password_reset_token p on t.id=p.user_id \r\n"
				+ " left join users_roles u on t.id=u.user_id" + " left join todos todo on t.id=todo.users_id"
				+ " where t.id=:userId");

		Query query = entityManager.createNativeQuery(sqlQuery.toString());

		query.setParameter("userId", userId);

		try {

			List<Object[]> obj = query.getResultList();
			todoDtoList = new ArrayList<>();

			if (!obj.isEmpty()) {

				for (Object[] record : obj) {

					todoDto = new UserRolesTokenDto();
					todoDto.setUserId(Long.parseLong(String.valueOf(record[0])));
					if (record[1] != null)
						todoDto.setUserVerificationTokenId(Long.parseLong(String.valueOf(record[1])));
					if (record[2] != null)
						todoDto.setUserPasswordResetTokenId(Long.parseLong(String.valueOf(record[2])));
					if (record[3] != null)
						todoDto.setUserRoleId(Long.parseLong(String.valueOf(record[3])));
					if (record[4] != null)
						todoDto.setUserTodoId(Long.parseLong(String.valueOf(record[4])));
					todoDtoList.add(todoDto);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		// log.info("Exiting into TodoServiceImpl :: getUserTodoById");
		return todoDtoList;
	}

	@Transactional
	@Modifying
	private void userDelete(Long userId) {
		try {

			userRepository.deleteById(userId);
		} catch (Exception e) {
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

			Date date = new Date();
			LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			user.setCreatedDate(localDate);
			Role role = roleRepository.findByName("ROLE_USER");
			if (role == null) {
				role = checkRoleExist();
			}
			user.setEnabled('0');
			user.setActive('1');
			user.setRoles(Arrays.asList(role));
			log.info("Exiting into UserServiceImpl :: saveUser");
			userRepository.save(user);

			return user;
		} catch (Exception e) {
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
		List<User> users = userRepository.findUpdatedUsers();
		return users.stream().map((user) -> mapToUserDto(user)).collect(Collectors.toList());
	}

	private UserDto mapToUserDto(User user) {
		log.info("Entering into UserServiceImpl :: mapToUserDto");
		UserDto userDto = null;
		try {
			userDto = new UserDto();
			userDto.setId(user.getId());
			userDto.setFirstName(user.getName());
			userDto.setEmail(user.getEmail());
			Date date = new Date();
			// LocalDate localDate =
			// date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			userDto.setCreatedDate(date);
			userDto.setAddress(user.getAddress());

		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("Exiting into UserServiceImpl :: mapToUserDto");
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
		log.info("Entering into UserServiceImpl :: getStudentById");
		User userDto = userRepository.findById(id).get();

		UserDto savedUserDto = new UserDto();

		savedUserDto.setId(userDto.getId());
		savedUserDto.setFirstName(userDto.getName());
		savedUserDto.setEmail(userDto.getEmail());
		savedUserDto.setAddress(userDto.getAddress());
		savedUserDto.setPassword(userDto.getPassword());
		savedUserDto.setConfirmPassword(userDto.getConfirmPassword());
		log.info("Exiting into UserServiceImpl :: getStudentById");
		return savedUserDto;
	}
}
