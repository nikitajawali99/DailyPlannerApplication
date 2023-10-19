package com.dailyplanner.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

import com.dailyplanner.entity.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationCompleteEvent extends ApplicationEvent {

	Logger log = LoggerFactory.getLogger(RegistrationCompleteEvent.class);

	private static final long serialVersionUID = 1L;
	private User user;
	private String confirmationUrl;

	public RegistrationCompleteEvent(User user, String confirmationUrl) {
		super(user);
		this.user = user;
		this.confirmationUrl = confirmationUrl;
	}

}
