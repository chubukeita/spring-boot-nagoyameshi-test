package com.example.nagoyameshi.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class RejoinEventPublisher {
	private final ApplicationEventPublisher applicationEventPublisher;

	public RejoinEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public void publishRejoinEvent(String email, String requestUrl) {
		applicationEventPublisher.publishEvent(new RejoinEvent(this, email, requestUrl));
	}
}
