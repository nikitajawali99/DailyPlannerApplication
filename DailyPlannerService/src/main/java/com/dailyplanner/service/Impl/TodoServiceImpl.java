package com.dailyplanner.service.Impl;


import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.dailyplanner.dto.TodoDto;
import com.dailyplanner.entity.Todo;
import com.dailyplanner.exception.ResourceNotFoundException;
import com.dailyplanner.repository.TodoRepository;
import com.dailyplanner.service.TodoService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TodoServiceImpl implements TodoService {

	private TodoRepository todoRepository;

	private ModelMapper modelMapper;
	
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	@Transactional
	public TodoDto addTodo(TodoDto todoDto) {

		try {
			Todo todo = modelMapper.map(todoDto, Todo.class);
			todo.setCreatedDate(new Date());

			// Date firstDate = todo.getTargetDate();
			// Date secondDate = todo.getCreatedDate();

			// long diffInMillies = Math.abs(secondDate.getTime() - firstDate.getTime());
			// long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
			// todo.setRemainingDaysToComplete(diff);
			
			
			
			Todo savedTodo = todoRepository.save(todo);
			TodoDto savedTodoDto = modelMapper.map(savedTodo, TodoDto.class);
			return savedTodoDto;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return todoDto;

	}

	@Override
	@Transactional
	public TodoDto getTodo(Long id) {

		try {
			Todo todo = todoRepository.findById(id)
					.orElseThrow(() -> new ResourceNotFoundException("Todo not found with id:" + id));

			return modelMapper.map(todo, TodoDto.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	@Transactional
	public List<TodoDto> getAllTodos() {
		List<Todo> todos = null;
		try {
			todos = todoRepository.findAll();

			return todos.stream().map((todo) -> modelMapper.map(todo, TodoDto.class)).collect(Collectors.toList());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public TodoDto updateTodo(TodoDto todoDto, Long id) {
		
	

		Todo todo = todoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Todo not found with id : " + id));
		todo.setTitle(todoDto.getTitle());
		todo.setDescription(todoDto.getDescription());
		todo.setCompleted(todoDto.isCompleted());
		todo.setTargetDate(todoDto.getTargetDate());
		Todo updatedTodo = todoRepository.save(todo);

		return modelMapper.map(updatedTodo, TodoDto.class);
	}

	@Override
	@Transactional
	public void deleteTodo(Long id) {

		try {
			todoRepository.findById(id)
					.orElseThrow(() -> new ResourceNotFoundException("Todo not found with id : " + id));

			todoRepository.deleteById(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	@Transactional
	public TodoDto completeTodo(Long id) {

		Todo todo = todoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Todo not found with id : " + id));

		todo.setCompleted(Boolean.TRUE);

		Todo updatedTodo = todoRepository.save(todo);

		return modelMapper.map(updatedTodo, TodoDto.class);
	}

	@Override
	@Transactional
	public TodoDto inCompleteTodo(Long id) {

		Todo todo = todoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Todo not found with id : " + id));

		todo.setCompleted(Boolean.FALSE);

		Todo updatedTodo = todoRepository.save(todo);

		return modelMapper.map(updatedTodo, TodoDto.class);
	}

	@Override
	@Transactional
	public List<TodoDto> getUserTodoById(Long id) {
		
		List<TodoDto> todoDtoList = null;
		TodoDto todoDto = null;
		

		StringBuilder sqlQuery = new StringBuilder(
				"SELECT t.id,t.title,t.description,t.created_date,t.target_date FROM user_management.todos t left join users u " + " on t.users_id=u.id where u.id=:id");

		Query query = entityManager.createNativeQuery(sqlQuery.toString());
		
		query.setParameter("id", id);
		
			try {
				
			List<Object[]> obj = query.getResultList();
			todoDtoList = new ArrayList<>();
			
			if(!obj.isEmpty()) {
		
				for (Object[] record : obj) {
					
					todoDto = new TodoDto();
					todoDto.setUserId(Long.parseLong(String.valueOf(record[0])));
					todoDto.setTitle(String.valueOf(record[1]));
					todoDto.setDescription(String.valueOf(record[2]));
					todoDto.setCreatedDate((Date)record[3]);
					todoDto.setTargetDate((Date)record[4]);
					todoDtoList.add(todoDto);
				}
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
			return todoDtoList;
		
	}

	@Override
	@Transactional
	public TodoDto updateTodoById(Long id) {
		
       Todo userDto = todoRepository.findById(id).get();
		
        TodoDto savedUserDto = new TodoDto();
		savedUserDto.setId(userDto.getId());
		savedUserDto.setTitle(userDto.getTitle());
		savedUserDto.setDescription(userDto.getDescription());
		savedUserDto.setTargetDate(userDto.getTargetDate());
		
		
		return savedUserDto;
	}
}