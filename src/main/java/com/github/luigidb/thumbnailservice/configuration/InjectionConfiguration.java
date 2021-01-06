package com.github.luigidb.thumbnailservice.configuration;

import com.github.luigidb.thumbnailservice.services.impl.FileSystemStorage;
import com.github.luigidb.thumbnailservice.services.StorageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InjectionConfiguration {

    @Bean
    @Qualifier("EphemeralStorage")
    StorageService getEphemeralStorage() {
        return new FileSystemStorage("temp");
    }

    @Bean
    @Qualifier("ThumbnailStorage")
    StorageService getPersistentStorage() {
        return new FileSystemStorage("persistent");
    }
}
