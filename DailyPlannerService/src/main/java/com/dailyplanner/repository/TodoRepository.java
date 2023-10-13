package com.dailyplanner.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dailyplanner.entity.Todo;


public interface TodoRepository extends JpaRepository<Todo, Long> {

}
