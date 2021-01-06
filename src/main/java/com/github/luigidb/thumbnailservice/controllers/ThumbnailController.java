package com.github.luigidb.thumbnailservice.controllers;

import com.github.luigidb.thumbnailservice.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ThumbnailController {

    private final StorageService storageService;

    @Autowired
    public ThumbnailController(StorageService storageService) {
        this.storageService = storageService;
    }


    @PostMapping("/thumbnails")
    @ResponseBody
    public ResponseEntity<HttpStatus> uploadImage(@RequestParam("file") MultipartFile file,
                                                  RedirectAttributes redirectAttributes) {
        storageService.store(file);
        redirectAttributes.addFlashAttribute("message","You successfully uploaded " + file.getOriginalFilename() + "!");

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }
}
