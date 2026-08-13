package me.shinsunyoung.springbootdeveloper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing // automation update for created_at, updated_at
@SpringBootApplication
public class SpringbootDeveloperApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootDeveloperApplication.class, args);
	}

}
