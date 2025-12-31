package com.example.nagoyameshi.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class BackStoreInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (!"GET".equalsIgnoreCase(request.getMethod()))
			return true;
		if (!(handler instanceof HandlerMethod handlerMethod))
			return true;

		StoreBack annotation = handlerMethod.getMethodAnnotation(StoreBack.class);

		if (annotation == null)
			return true; // 注釈がついていない＝対象外

		String name = annotation.value();
		String full = BackLink.currentFullUrl(request);
		BackLink.put(request, name, full);

		return true;
	}
}
