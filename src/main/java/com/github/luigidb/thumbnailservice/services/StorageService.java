package com.github.luigidb.thumbnailservice.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.nio.file.Path;

public interface StorageService {

    void init();

    void deleteAll();

    void store(MultipartFile file);

    void store(Image img, String filename);

    Path load(String filename);

    Resource loadAsResource(String filename);

    void delete(String filename);
}
