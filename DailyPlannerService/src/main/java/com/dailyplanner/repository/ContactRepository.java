package com.dailyplanner.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dailyplanner.entity.Contact;


public interface ContactRepository extends JpaRepository<Contact, Long>{

}

