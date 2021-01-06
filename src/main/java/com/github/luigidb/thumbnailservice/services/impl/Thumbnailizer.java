package com.github.luigidb.thumbnailservice.services.impl;

import com.github.luigidb.thumbnailservice.services.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import static com.github.luigidb.thumbnailservice.utils.UtilityMethods.getThumbnailName;

@Service
public class Thumbnailizer {

    private static final Logger logger = LoggerFactory.getLogger(Thumbnailizer.class);

    @Autowired
    @Qualifier("EphemeralStorage")
    StorageService storageService;

    @Autowired
    @Qualifier("ThumbnailStorage")
    StorageService persistentService;

    @Async
    public void asyncThumbnail(String file) {
        Path originalImage = storageService.load(file);
        try {
            Image img = ImageIO
                    .read(originalImage.toFile())
                    .getScaledInstance(100, 100, BufferedImage.SCALE_SMOOTH);
            persistentService.store(img, getThumbnailName(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
