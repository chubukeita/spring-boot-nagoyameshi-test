package com.example.nagoyameshi.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = ImageFileValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ImageFile {
	String message() default "画像ファイルは jpg, jpeg, png, bmp, gif, svg, webp 形式かつ 5MB 以下にしてください。";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
