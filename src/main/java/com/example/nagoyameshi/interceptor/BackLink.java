package com.example.nagoyameshi.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class BackLink {
	private BackLink() {
	}

	public static String key(String name) {
		return "BACK_URL::" + name;
	}

	// 現在リクエストから完全URLを組み立て
	public static String currentFullUrl(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String queryString = request.getQueryString();
		return (queryString == null || queryString.isBlank()) ? uri : uri + "?" + queryString;
	}

	// セッションに保存
	public static void put(HttpServletRequest request, String name, String url) {
		HttpSession session = request.getSession(true);
		session.setAttribute(key(name), url);
	}

	// セッションから取得。なければdefaultUrlを返す
	public static String get(HttpServletRequest request, String name, String defaultUrl) {
		HttpSession session = request.getSession(false);
		Object v = (session == null) ? null : session.getAttribute(key(name));
		return (v instanceof String s && !s.isBlank()) ? s : defaultUrl;
	}
}
