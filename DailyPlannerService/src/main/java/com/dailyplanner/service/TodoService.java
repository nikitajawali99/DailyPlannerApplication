package com.dailyplanner.service;

import java.util.List;

import com.dailyplanner.dto.TodoDto;

public interface TodoService {
	
	TodoDto addTodo(TodoDto todoDto);

    TodoDto getTodo(Long id);

    List<TodoDto> getAllTodos();

    TodoDto updateTodo(TodoDto todoDto, Long id);

    void deleteTodo(Long id);

    TodoDto completeTodo(Long id);

    TodoDto inCompleteTodo(Long id);

    List<TodoDto> getUserTodoById(Long id);

	TodoDto updateTodoById(Long id);

}
