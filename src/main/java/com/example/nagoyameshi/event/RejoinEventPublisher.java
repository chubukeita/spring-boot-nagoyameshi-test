package com.example.nagoyameshi.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.nagoyameshi.entity.User;

@Component
public class RejoinEventPublisher {
	private final ApplicationEventPublisher applicationEventPublisher;

	public RejoinEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public void publishRejoinEvent(User user, String requestUrl) {
		applicationEventPublisher.publishEvent(new RejoinEvent(this, user, requestUrl));
	}
}
