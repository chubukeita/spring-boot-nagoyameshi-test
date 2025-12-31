package com.example.nagoyameshi.event;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.nagoyameshi.service.ResetTokenService;

@Component
public class ResetEventListener {
	private final ResetTokenService resetTokenService;
	private final JavaMailSender javaMailSender;

	public ResetEventListener(ResetTokenService resetTokenService, JavaMailSender javaMailSender) {
		this.resetTokenService = resetTokenService;
		this.javaMailSender = javaMailSender;
	}

	@EventListener
	private void onResetEvent(ResetEvent resetEvent) {
		String email = resetEvent.getEmail();
		String token = UUID.randomUUID().toString();
		resetTokenService.createResetToken(email, token);

		String senderAddress = "rb22003-1377@sti.chubu.ac.jp";
		String recipientAddress = email;
		String subject = "メール認証";
		String confirmationUrl = resetEvent.getRequestUrl() + "Verification?token=" + token;
		String message = "以下のリンクをクリックしてパスワードの再設定を完了してください。";

		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom(senderAddress);
		mailMessage.setTo(recipientAddress);
		mailMessage.setSubject(subject);
		mailMessage.setText(message + "\n" + confirmationUrl);
		javaMailSender.send(mailMessage);
	}

}
