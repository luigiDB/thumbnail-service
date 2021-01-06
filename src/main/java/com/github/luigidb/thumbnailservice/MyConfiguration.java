package com.github.luigidb.thumbnailservice;

import com.github.luigidb.thumbnailservice.services.FileSystemStorageService;
import com.github.luigidb.thumbnailservice.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyConfiguration {

    @Bean
    @Qualifier("EphemeralStorage")
    StorageService getEphemeralStorage() {
        return new FileSystemStorageService("temp");
    }

    @Bean
    @Qualifier("ThumbnailStorage")
    StorageService getPersistentStorage() {
        return new FileSystemStorageService("persistent");
    }
}
