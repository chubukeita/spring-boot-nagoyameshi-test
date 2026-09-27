package com.example.nagoyameshi.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RejoinForm {

	@NotBlank(message = "メールアドレスを入力してください。")
	@Email(message = "メールアドレスの形式が正しくありません。")
	private String email;

	@NotBlank(message = "パスワードを入力してください。")
	@Length(min = 8, message = "パスワードは8文字以上で入力してください。")
	private String password;

	@NotBlank(message = "パスワード（確認用）を入力してください。")
	private String passwordConfirmation;

	// パスワードとパスワード（確認用）の入力値が一致するかどうかをチェックする(Serviceの仕事ではない)
	// 2つの場所の入力じゃなくて、1つの入力だからそこでまとめて処理した方がいい。SignupFormの方がいい
	public boolean isSamePassword() {
		return password.equals(passwordConfirmation);
	}

}
