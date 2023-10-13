package com.dailyplanner.controller;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dailyplanner.controller.TodoController;
import com.dailyplanner.dto.TodoDto;
import com.dailyplanner.entity.User;
import com.dailyplanner.service.TodoService;
import com.dailyplanner.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/todos")
@CrossOrigin("*")
public class TodoController {

	Logger log = LoggerFactory.getLogger(TodoController.class);

	private final TodoService todoService;
	private final UserDetailsService userDetailsService;
	private final UserService userService;


	public TodoController(TodoService todoService,
			UserDetailsService userDetailsService,UserService userService) {
		this.todoService = todoService;
		this.userDetailsService=userDetailsService;
		this.userService=userService;
	}
	
	   // http://localhost:8080/view/todos
		@RequestMapping("/todos")
		public String todos(Model model) {

			log.info("Entering into TodoController :: todos");
			List<TodoDto> todos = todoService.getAllTodos();

			model.addAttribute("todos", todos);
			log.info("Exiting into TodoController :: todos");
			return "todos.html";
		}
	
	// handler method to handle user registration form request
	@PreAuthorize("hasRole('USER')")
    @GetMapping("/createTodo")
    public String createTodo(Model model){
    	log.info("Entering into TodoController :: createTodo");
        // create model object to store form data
        TodoDto user = new TodoDto();
        model.addAttribute("user", user);
        log.info("Exiting into TodoController :: createTodo");
        return "createTodo";
    }
	
	   // handler method to handle user registration form submit request
	//@PreAuthorize("hasAnyRole('ADMIN','USER')")
	@PreAuthorize("hasRole('USER')")
    @PostMapping("/save")
    public String todo(@Valid @ModelAttribute("user") TodoDto todoDto,
                               BindingResult result,
                               Model model,Principal principal){
        try {
        	log.info("Entering into TodoController :: todo");
			
			
			if(result.hasErrors()){
			    model.addAttribute("user", todoDto);
			    return "/createTodo";
			}
			
			UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());

			log.info("Entering into TodoController :: todo"+userDetails);
			
			User user = userService.findUserByEmail(userDetails.getUsername());

			
			//User user=userRepository.findByEmail(todoDto.getEmail());
			
			if(user!=null) {
				
				todoDto.setUserId(user.getId());
				
			}else {
				return "redirect:/todos/createTodo?existingNoEmail";
			}
			
			todoService.addTodo(todoDto);
			log.info("Exiting into TodoController :: todo");
		
			return "redirect:/user-view";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }

	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	@GetMapping("/getTodoById/{id}")
	public ResponseEntity<TodoDto> getTodoById(@Valid @PathVariable("id") Long todoId) {
		log.info("Entering into TodoController :: getTodoById");
		TodoDto todoDto = todoService.getTodo(todoId);
		log.info("Exiting into TodoController :: getTodoById");
		return new ResponseEntity<>(todoDto, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	@GetMapping("/getAllTodos")
	public ResponseEntity<List<TodoDto>> getAllTodos() {
		log.info("Entering into TodoController :: getAllTodos");
		List<TodoDto> todos = todoService.getAllTodos();
		log.info("Exiting into TodoController :: getAllTodos");
		return ResponseEntity.ok(todos);
	}

	// Build Update Todo REST API
	
	@GetMapping("/updateTodoById/{id}")
	public ResponseEntity<TodoDto> updateTodo(@RequestBody TodoDto todoDto, @PathVariable("id") Long todoId) {
		log.info("Entering into TodoController :: updateTodo");
		TodoDto updatedTodo = todoService.updateTodo(todoDto, todoId);
		log.info("Exiting into TodoController :: updateTodo");
		return ResponseEntity.ok(updatedTodo);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	@GetMapping("/deleteTodoById/{id}")
	public String deleteTodoById(@PathVariable("id") Long todoId) {
		log.info("Entering into TodoController :: deleteTodoById");
		
		
		todoService.deleteTodo(todoId);
		log.info("Exiting into TodoController :: deleteTodoById");
		return "redirect:/user-view";
	}

	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	@PatchMapping("{id}/complete")
	public ResponseEntity<TodoDto> completeTodo(@PathVariable("id") Long todoId) {
		log.info("Entering into TodoController :: completeTodo");
		TodoDto updatedTodo = todoService.completeTodo(todoId);
		log.info("Exiting into TodoController :: completeTodo");
		return ResponseEntity.ok(updatedTodo);
	}

	   @PreAuthorize("hasAnyRole('ADMIN','USER')")
	@PatchMapping("{id}/in-complete")
	public ResponseEntity<TodoDto> inCompleteTodo(@PathVariable("id") Long todoId) {
		TodoDto updatedTodo = todoService.inCompleteTodo(todoId);
		return ResponseEntity.ok(updatedTodo);
	}

}
