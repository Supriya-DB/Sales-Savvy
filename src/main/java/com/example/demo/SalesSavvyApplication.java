package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class InsightCartApplication {

	public static void main(String[] args) {
		SpringApplication.run(InsightCartApplication.class, args);
	}

}
