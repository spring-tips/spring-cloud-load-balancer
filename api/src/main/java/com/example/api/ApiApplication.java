package com.example.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@SpringBootApplication
public class ApiApplication {

	@Bean
	RouterFunction<ServerResponse> routes() {
		return route()
			.GET("/greetings", r -> ok().body(Mono.just(Map.of("greeting", "Hello, world!")), Map.class))
			.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

}
