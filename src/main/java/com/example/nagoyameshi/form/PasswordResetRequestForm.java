package com.example.nagoyameshi.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PasswordResetRequestForm {
	@NotBlank(message = "メールアドレスを入力してください。")
	@Email(message = "メールアドレスは正しい形式で入力してください。")
	private String email;
}
