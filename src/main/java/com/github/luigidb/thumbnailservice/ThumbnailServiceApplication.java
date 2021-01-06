package com.github.luigidb.thumbnailservice;

import com.github.luigidb.thumbnailservice.services.StorageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableAsync
public class ThumbnailServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThumbnailServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner init(@Qualifier("EphemeralStorage") StorageService storageService) {
		return (args) -> {
			storageService.deleteAll();
			storageService.init();
		};
	}

	@Bean
	CommandLineRunner initPersistentStorage(@Qualifier("ThumbnailStorage") StorageService storageService) {
		return (args) -> {
			storageService.init();
		};
	}

	@Bean
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(8);
		executor.setMaxPoolSize(16);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("Thumbnailizer");
		executor.initialize();
		return executor;
	}

}
