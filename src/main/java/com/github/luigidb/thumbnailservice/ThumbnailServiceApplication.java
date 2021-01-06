package com.github.luigidb.thumbnailservice;

import com.github.luigidb.thumbnailservice.services.StorageProperties;
import com.github.luigidb.thumbnailservice.services.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class ThumbnailServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThumbnailServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
			storageService.deleteAll();
			storageService.init();
		};
	}
}
