package com.example.nagoyameshi.event;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.nagoyameshi.service.RejoinTokenService;

@Component
public class RejoinEventListener {
	private final RejoinTokenService rejoinTokenService;
	private final JavaMailSender javaMailSender;

	public RejoinEventListener(RejoinTokenService rejoinTokenService, JavaMailSender javaMailSender) {
		this.rejoinTokenService = rejoinTokenService;
		this.javaMailSender = javaMailSender;
	}

	@EventListener
	private void onRejoinEvent(RejoinEvent rejoinEvent) {
		String email = rejoinEvent.getEmail();
		String token = UUID.randomUUID().toString();
		rejoinTokenService.createRejoinToken(email, token);

		String senderAddress = "rb22003-1377@sti.chubu.ac.jp";
		String recipientAddress = email;
		String subject = "メール認証";
		String confirmationUrl = rejoinEvent.getRequestUrl() + "/verify?token=" + token;
		String message = "以下のリンクをクリックして再入会を完了してください。";

		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom(senderAddress);
		mailMessage.setTo(recipientAddress);
		mailMessage.setSubject(subject);
		mailMessage.setText(message + "\n" + confirmationUrl);
		javaMailSender.send(mailMessage);
	}

}
