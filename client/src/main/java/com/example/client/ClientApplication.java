package com.example.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import static com.example.client.ClientApplication.*;

@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

	@Bean
	@LoadBalanced
	WebClient.Builder builder() {
		return WebClient.builder();
	}

	@Bean
	WebClient webClient(WebClient.Builder builder) {
		return builder.build();
	}

	static Flux<Greeting> call(WebClient http, String url) {
		return http.get().uri(url).retrieve().bodyToFlux(Greeting.class);
	}

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Greeting {
	private String greetings;
}

@Component
@Log4j2
class ConfiguredWebClientRunner {

	ConfiguredWebClientRunner(WebClient http) {
		call(http, "http://api/greetings").subscribe(greeting -> log.info("configured: " + greeting.toString()));
	}
}

@Component
@Log4j2
class WebClientRunner {

	WebClientRunner(ReactiveLoadBalancer.Factory<ServiceInstance> serviceInstanceFactory) {

		var filter = new ReactorLoadBalancerExchangeFilterFunction(serviceInstanceFactory);

		var http = WebClient.builder()
			.filter(filter)
			.build();

		call(http, "http://api/greetings").subscribe(greeting -> log.info("filter: " + greeting.toString()));
	}
}

@Component
@Log4j2
class ReactiveLoadBalancerFactoryRunner {

	ReactiveLoadBalancerFactoryRunner(ReactiveLoadBalancer.Factory<ServiceInstance> serviceInstanceFactory) {
		var http = WebClient.builder().build();
		ReactiveLoadBalancer<ServiceInstance> api = serviceInstanceFactory.getInstance("api");
		Flux<Response<ServiceInstance>> chosen = Flux.from(api.choose());
		chosen
			.map(responseServiceInstance -> {
				ServiceInstance server = responseServiceInstance.getServer();
				var url = "http://" + server.getHost() + ':' + server.getPort() + "/greetings";
				log.info(url);
				return url;
			})
			.flatMap(url -> call(http, url))
			.subscribe(greeting -> log.info("manual: " + greeting.toString()));

	}
}