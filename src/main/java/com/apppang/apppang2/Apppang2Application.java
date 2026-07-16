package com.apppang.apppang2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Apppang2Application {
	public static void main(String[] args) {
		SpringApplication.run(Apppang2Application.class, args);
	}
}
