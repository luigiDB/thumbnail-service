package com.github.luigidb.thumbnailservice.controllers;

import com.github.luigidb.thumbnailservice.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

@Controller
public class ThumbnailController {

    private final StorageService storageService;
    private final StorageService persistentService;

    @Autowired
    public ThumbnailController(
            @Qualifier("EphemeralStorage") StorageService storageService,
            @Qualifier("ThumbnailStorage") StorageService persistentService) {
        this.storageService = storageService;
        this.persistentService = persistentService;
    }


    @PostMapping("/thumbnails")
    @ResponseBody
    public ResponseEntity<HttpStatus> uploadImage(@RequestParam("file") MultipartFile file) {
        System.out.println(file);
        storageService.store(file);

        Path originalImage = storageService.load(file.getOriginalFilename());
        try {
            Image img = ImageIO
                    .read(originalImage.toFile())
                    .getScaledInstance(100, 100, BufferedImage.SCALE_SMOOTH);
            persistentService.store(img, "thumbnail_"+file.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("/thumbnails/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getRequestStatus(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.IMAGE_JPEG)
                .body(file);
    }

    @GetMapping("/thumbnails/{filename:.+}/result")
    @ResponseBody
    public ResponseEntity<Resource> retrieveThumbnail(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.IMAGE_JPEG)
                .body(file);
    }

    @DeleteMapping("/thumbnails/{filename:.+}/result")
    @ResponseBody
    public ResponseEntity<HttpStatus> deleteThumbnail(@PathVariable String filename) {
        storageService.delete(filename);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }
}
