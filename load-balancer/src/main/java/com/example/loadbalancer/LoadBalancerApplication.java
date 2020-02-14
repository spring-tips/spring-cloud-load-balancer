package com.example.loadbalancer;

import lombok.extern.log4j.Log4j2;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

import static com.example.loadbalancer.LogUtils.*;
import static com.example.loadbalancer.LogUtils.log;
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

@Component
@Log4j2
class ReactiveLoadBalancerFactoryRunner {

	ReactiveLoadBalancerFactoryRunner(ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory) {
		ReactiveLoadBalancer<ServiceInstance> loadBalancer = loadBalancerFactory.getInstance("api");
		Publisher<Response<ServiceInstance>> choose = loadBalancer.choose();
		Flux<Response<ServiceInstance>> responseOfServiceInstance = Flux.from(choose);
		responseOfServiceInstance.subscribe(serviceInstanceResponse -> {
			ServiceInstance server = serviceInstanceResponse.getServer();
			log(getClass(), server.getHost() + ':' + server.getPort());
		});

	}
}

@Log4j2
@Component
class WebClientRunner {

	WebClientRunner(WebClient client) {

		client
			.get()
			.uri("http://api/greetings")
			.retrieve()
			.bodyToMono(String.class)
			.subscribe(s -> log(getClass(), s));
	}


}

@Log4j2
abstract class LogUtils {

	public static void log(Class<?> clz, String msg) {
		log.info("--------------------------------------");
		log.info(clz.getName() + " : " + msg);
		log.info("--------------------------------------");
	}
}