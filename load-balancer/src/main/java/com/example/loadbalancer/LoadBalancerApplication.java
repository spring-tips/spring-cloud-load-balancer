package com.example.loadbalancer;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.function.Consumer;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Log4j2
@SpringBootApplication
public class LoadBalancerApplication {

	@Bean
	@LoadBalanced
	WebClient.Builder loadBalancedWebClientBuilder() {
		return WebClient.builder();
	}

	@Bean
	@LoadBalanced
	WebClient client(WebClient.Builder builder) {
		return builder.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(LoadBalancerApplication.class, args);
	}

}

@Log4j2
@Component
class Runner {

	Runner(WebClient client  /*ReactiveLoadBalancer lbc*/) {

		client
			.get()
			.uri("http://api/greetings")
			.retrieve()
			.bodyToMono(String.class)
			.subscribe(s -> log.info("new greeting: " + s));
	}

}