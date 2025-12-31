package com.example.nagoyameshi.validation;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ImageFileValidator implements ConstraintValidator<ImageFile, MultipartFile> {

	private static final String[] ALLOWED_TYPES = {
			"image/jpeg", "image/jpg", "image/png", "image/bmp",
			"image/gif", "image/svg+xml", "image/webp"
	};

	// 最大許容サイズ（5MB）
	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

	@Override
	public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
		// ファイルが空ならバリデーション対象外（NotNullと併用を想定）
		if (value == null || value.isEmpty()) {
			return true;
		}

		String contentType = value.getContentType();
		if (contentType == null) {
			return false;
		}

		boolean isAllowedType = false;
		for (String allowedType : ALLOWED_TYPES) {
			if (contentType.equalsIgnoreCase(allowedType)) {
				isAllowedType = true;
				break;
			}
		}

		// ファイルタイプが許可されていない
		if (!isAllowedType) {
			return false;
		}

		// ファイルサイズが大きすぎる
		if (value.getSize() > MAX_FILE_SIZE) {
			return false;
		}

		return true;
	}
}