package com.example.nagoyameshi.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

import lombok.Data;

@Data
public class SignupForm {
	@NotBlank(message = "氏名を入力してください。")
	private String name;

	@NotBlank(message = "フリガナを入力してください。")
	private String furigana;

	@NotBlank(message = "郵便番号を入力してください。")
	@Pattern(regexp = "^[0-9]{7}$", message = "郵便番号は7桁の半角数字で入力してください。")
	private String postalCode;

	@NotBlank(message = "住所を入力してください。")
	private String address;

	@NotBlank(message = "電話番号を入力してください。")
	@Pattern(regexp = "^[0-9]{10,11}$", message = "電話番号は10桁または11桁の半角数字で入力してください。")
	private String phoneNumber;

	@Pattern(regexp = "^$|^[0-9]{8}$", message = "誕生日は8桁の半角数字で入力してください。")
	private String birthday;

	private String occupation;

	@NotBlank(message = "メールアドレスを入力してください。")
	@Email(message = "メールアドレスは正しい形式で入力してください。")
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

	// もし引数が2つある場合はこのように書くこともできる
	// しかし、今回の場合は、signupFormのpasswordとpasswordConfirmationを比較するだけなので、引数は必要ない
	// 上記の書き方で十分（1つのインスタンスでnewして使うことを考えると、newした時点でフィールド変数にpasswordやpasswordConfirmationに既に値は入っているので、
	// それらの値を使って、パスワードを比較することができる→なので、今回は引数は必要ない
	// 引数が必要な場合は、引数を使う方がいい（例えば、パスワードとパスワード（確認用）を比較する際に、パスワードとパスワード確認用が別のオブジェクトで管理されている場合など）
	// 本当はisSamePasswordメソッド自体の引数を書かなくなったため、このメソッドを利用しているAuthControllerの方でも、テストが通っているかどうかを確認することが必要
	// 今回は、AuthControllerTestを作っていないので、testがとおっているかどうかの確認まではできなかったが、本来はそれでテストをやって通るかどうかを検証する必要がある。
	// 考え方としては、カプセル化や単一責任原則に近い考え方で、signupFormという一つに役割をまとめられるオブジェクトに、集約した方がいい。
	// だから、今回はSignupFormの方でまとめて処理することにした

	//	以下の書き方も同様にできるが、これは引数を使う場合の書き方で、今回はパスワードと、確認用パスワードが同じ一つのオブジェクトにあるので、
	//	同じ引数を使わない方がいいと思う
	//	public boolean isSamePassword(String password, String passwordConfirmation) {
	//		return this.password.equals(this.passwordConfirmation);
	//	}
}
