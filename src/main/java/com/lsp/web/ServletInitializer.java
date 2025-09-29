package com.lsp.web;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import java.util.TimeZone;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
		return application.sources(LspApplication.class);
	}

}
