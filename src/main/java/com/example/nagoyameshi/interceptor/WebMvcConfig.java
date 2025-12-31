package com.example.nagoyameshi.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	private final BackStoreInterceptor backStoreInterceptor;

	public WebMvcConfig(BackStoreInterceptor backStoreInterceptor) {
		this.backStoreInterceptor = backStoreInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(backStoreInterceptor).addPathPatterns("/**"); // 全コントローラに適用（@StoreBackがなければ何もしない）
	}
}
