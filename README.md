# Spring Tips: Spring Cloud Loadbalancer 

Hi, Spring fans! Welcome to another installment of Spring Tips! In this installment we're going to look at a new feature in Spring Cloud, Spring Cloud Loadbalancer. Spring Cloud Loadbalancer is a generic abstraction that can do the work that we use dto with Netflix' Ribbon. Netflix Ribbon is still supported in Spring Cloud, but it's days may be numbered like so much else of the Netflix microservices stack, so we've provided an abstraction to support an alternative. 

## The Service Registry 

In order for us to use the SCLB, we need to have a service registry up and running. A service registry makes it trivial to progamatically query for the location of a given sgvice in a system. There are a number of popular implementations including Apache Zookeeper, Netflix's Eureka, Hashicorp Consul, and others. Yo ucan even use Kubernetes and Cloud Foundry as service registries. Spring Cloud provides an abstration, DisvoferyClient, that you can use to talk to these service registies in a generic fashion. There are several patterns that a service registry enables that just arent possible with good 'ol DNS. One thing i love to do is client side loadbalancing. Client-side loadbalancing requires the cliet code to make the decision about which node should be invoked to respond to the request. There are an number of instances of the services out there and their suitabilty to handle a particular request is something each client can decide. It's even better if it can mmake the decision _before_ launching a request that might otherwise be doomed to failure. It saves time, unburdens the services with tedious flow control requirements, and makes our system mor edynamic since we can _query_ its topology. 

You can run any service registry you like. I like to use Netlfix Eureka for these sorts of things because its simpler to setup. Lets setup an new instance. You could download and run a stock standard image if you want, but I want to use the pre-configured instance that's provided as part of SPring CLoud. 

Go to the Spring Initializer, choose `Eureka Server` and `Lombok`. I named mine `eureka-service`. Hit `Generate`.

Most of the work of using the built-in Eureka Service is int he configuration, which I've reprinted below. 

```properties
server.port=8761
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

Then you'll need to write customize te Java class. Add the `@EnableEurekaServer` annotation to your class.

```java
package com.example.eurekaservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaServiceApplication.class, args);
	}

}
```

You can run that now. It'll be available on port `8761` and other clients will connect ot that port by dfault. 


## A Simple API 
Let's now turn to the API. Our API is as trivial a these things come. We just want an endpoitn to which our client can issue requests. 

Go to tthe SPring INitializr, generate a new proejct with `Reactive Web` and `Lombok` and the `Eureka Discovery Client`. That last bit is the important part. You're not going to see it used int he following Java vode. It's [all autoconfiguration, which we _also_ covered waaay back in 2016](https://www.youtube.com/watch?v=Sw7I70vjN0E&feature=emb_title), that runs at application startup. The autoconfiguration will automatically register the application with the specified registry (in this case, we're using the `DiscoveryClient` implementation for Netflix's Eureka) using the `spring.application.name` property.

Specify the following properties. 

```properties
spring.application.name=api
server.port=9000
```

Our HTTP endpoint is basically a "Hello, world!" that uses the functional reactive HTTP style that we [introduced in anither Sprnig Tips video waaaay back in 2017](https://www.youtube.com/watch?v=JjrAmhlTjug).



```java
package com.example.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
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
			.GET("/greetings", r -> ok().bodyValue(Map.of("greetings", "Hello, world!")))
			.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}
} 
```

Run the application and you'll see it reflected in the Netlfix Eureka instance. 


## The Load-Balancing Client 

All right, we made it thus