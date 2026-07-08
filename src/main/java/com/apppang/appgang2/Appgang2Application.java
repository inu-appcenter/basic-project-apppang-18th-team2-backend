package com.apppang.appgang2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Appgang2Application {

	public static void main(String[] args) {
		SpringApplication.run(Appgang2Application.class, args);
	}

}
