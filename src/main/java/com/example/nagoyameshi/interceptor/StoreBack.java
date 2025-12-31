package com.example.nagoyameshi.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StoreBack {
	// 同じグループ（一覧⇔詳細）で共有するキー名
	String value();
}
