package com.github.luigidb.thumbnailservice;

import com.github.luigidb.thumbnailservice.services.StorageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
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
			storageService.deleteAll();
			storageService.init();
		};
	}
}
