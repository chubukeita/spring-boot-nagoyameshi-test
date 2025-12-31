package com.example.nagoyameshi.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserWithdrawalForm {
	// 退会理由（必須、最大200文字まで）
	@NotBlank(message = "退会理由を入力してください。")
	@Size(max = 200, message = "退会理由は500文字以内で入力してください。")
	private String deleteReason;

}
