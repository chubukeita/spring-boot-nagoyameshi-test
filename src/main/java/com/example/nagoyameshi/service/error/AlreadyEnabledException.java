package com.example.nagoyameshi.service.error;

public class AlreadyEnabledException extends RuntimeException {
	public AlreadyEnabledException() {
		super("既にご利用中のアカウントです。");
	}
}
