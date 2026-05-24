package com.example.nagoyameshi.service.errorMessage;

import lombok.Data;

@Data
public class RejoinResult {
	public static final String SUCCESS_MESSAGE = "再入会が完了しました。ログインパスワードは過去に本アプリで使用していたパスワードを引き続きご利用ください。";
	public static final String SUCCESS_BUTTON_TEXT = "ログインページへ";
	public static final String SUCCESS_BUTTON_URL = "/login";

	public static final int ERROR_ID_ALREADY_ENABLED = 1;
	public static final int ERROR_ID_INVALID_TOKEN = 2;
	public static final int ERROR_ID_USER_NOT_FOUND = 3;

	public static final String ERROR_MESSAGE_ALREADY_ENABLED = "既にご利用中のアカウントです。";
	public static final String ERROR_MESSAGE_INVALID_TOKEN = "トークンが無効です。";
	public static final String ERROR_MESSAGE_USER_NOT_FOUND = "該当メールアドレスのユーザーが見つかりません。";

	public static final String NEXT_ACTION_ALREADY_ENABLED = "現在入会中です。<br>以下のボタンより、ホーム画面に戻って<br>引き続き本サービスをご利用いただけます。";
	public static final String NEXT_ACTION_INVALID_TOKEN = "恐れ入りますが、<br>一度開いたURLを再度開くことはできないので、<br>再度メール認証からやり直してください。";
	public static final String NEXT_ACTION_USER_NOT_FOUND = "恐れ入りますが、<br>登録したメールアドレスをご確認のうえ、<br>再度メール認証からやり直してください。";

	public static final String ERROR_BUTTON_TEXT_ALREADY_ENABLED = "ログイン画面へ戻る";
	public static final String ERROR_BUTTON_URL_ALREADY_ENABLED = "/login";
	public static final String ERROR_BUTTON_TEXT_INVALID_TOKEN = "ホームへ戻る";
	public static final String ERROR_BUTTON_URL_INVALID_TOKEN = "/";
	public static final String ERROR_BUTTON_TEXT_USER_NOT_FOUND = "再入会画面に戻る";
	public static final String ERROR_BUTTON_URL_USER_NOT_FOUND = "/rejoin";

	private final boolean success;
	private final String successMessage;
	private final Integer errorId;
	private final String errorMessage;
	private final String nextActionMessage;
	private final String buttonText;
	private final String buttonUrl;

	private RejoinResult(boolean success, String successMessage, Integer errorId, String errorMessage,
			String nextActionMessage, String buttonText, String buttonUrl) {
		this.success = success;
		this.successMessage = successMessage;
		this.errorId = errorId;
		this.errorMessage = errorMessage;
		this.nextActionMessage = nextActionMessage;
		this.buttonText = buttonText;
		this.buttonUrl = buttonUrl;
	}

	public static RejoinResult success(String successMessage, String buttonText, String buttonUrl) {
		return new RejoinResult(true, successMessage, null, null, null, buttonText, buttonUrl);
	}

	public static RejoinResult success() {
		return success(SUCCESS_MESSAGE, SUCCESS_BUTTON_TEXT, SUCCESS_BUTTON_URL);
	}

	public static RejoinResult error(Integer errorId, String errorMessage, String nextActionMessage,
			String buttonText, String buttonUrl) {
		return new RejoinResult(false, null, errorId, errorMessage, nextActionMessage, buttonText, buttonUrl);
	}

	public static RejoinResult alreadyEnabledError() {
		return error(
				ERROR_ID_ALREADY_ENABLED,
				ERROR_MESSAGE_ALREADY_ENABLED,
				NEXT_ACTION_ALREADY_ENABLED,
				ERROR_BUTTON_TEXT_ALREADY_ENABLED,
				ERROR_BUTTON_URL_ALREADY_ENABLED);
	}

	public static RejoinResult invalidTokenError() {
		return error(
				ERROR_ID_INVALID_TOKEN,
				ERROR_MESSAGE_INVALID_TOKEN,
				NEXT_ACTION_INVALID_TOKEN,
				ERROR_BUTTON_TEXT_INVALID_TOKEN,
				ERROR_BUTTON_URL_INVALID_TOKEN);
	}

	public static RejoinResult userNotFoundError() {
		return error(
				ERROR_ID_USER_NOT_FOUND,
				ERROR_MESSAGE_USER_NOT_FOUND,
				NEXT_ACTION_USER_NOT_FOUND,
				ERROR_BUTTON_TEXT_USER_NOT_FOUND,
				ERROR_BUTTON_URL_USER_NOT_FOUND);
	}
}