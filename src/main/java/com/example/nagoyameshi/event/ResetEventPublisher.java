package com.example.nagoyameshi.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ResetEventPublisher {
	private final ApplicationEventPublisher applicationEventPublisher;

	public ResetEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public void publishResetEvent(String email, String requestUrl) {
		applicationEventPublisher.publishEvent(new ResetEvent(this, email, requestUrl));
	}
}
