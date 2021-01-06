package com.github.luigidb.thumbnailservice.controllers;

import com.github.luigidb.thumbnailservice.entities.BaseReply;
import com.github.luigidb.thumbnailservice.exceptions.StorageException;
import com.github.luigidb.thumbnailservice.exceptions.StorageFileNotFoundException;
import com.github.luigidb.thumbnailservice.services.StorageService;
import com.github.luigidb.thumbnailservice.services.impl.Thumbnailizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.github.luigidb.thumbnailservice.utils.UtilityMethods.getThumbnailName;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
public class ThumbnailController {

    private final StorageService tempStorage;
    private final StorageService persistentStorage;
    private final Thumbnailizer thumbnailizer;

    @Autowired
    public ThumbnailController(
            @Qualifier("EphemeralStorage") StorageService tempStorage,
            @Qualifier("ThumbnailStorage") StorageService persistentStorage,
            Thumbnailizer thumbnailizer) {
        this.tempStorage = tempStorage;
        this.persistentStorage = persistentStorage;
        this.thumbnailizer = thumbnailizer;
    }

    @PostMapping("/thumbnails")
    @ResponseBody
    public ResponseEntity<EntityModel<BaseReply>> uploadImage(@RequestParam("file") MultipartFile file) {
        tempStorage.store(file);

        thumbnailizer.asyncThumbnail(file.getOriginalFilename());

        return new ResponseEntity<>(
                EntityModel.of(new BaseReply(),
                        linkTo(methodOn(ThumbnailController.class).retrieveThumbnail(getThumbnailName(file.getOriginalFilename()))).withRel("thumbnail"),
                        linkTo(methodOn(ThumbnailController.class).processing(file.getOriginalFilename())).withRel("status")
                ),
                HttpStatus.ACCEPTED);
    }

    @GetMapping("/thumbnails/{filename:.+}")
    @ResponseBody
    public ResponseEntity<EntityModel<BaseReply>> processing(@PathVariable String filename) {
        if (persistentStorage.exist(getThumbnailName(filename)))
            return new ResponseEntity<>(EntityModel.of(new BaseReply(),
                    linkTo(methodOn(ThumbnailController.class).retrieveThumbnail(getThumbnailName(filename))).withRel("thumbnail")
            ), HttpStatus.SEE_OTHER);
        else
            return new ResponseEntity<>(EntityModel.of(new BaseReply()), HttpStatus.NOT_FOUND);
    }

    @GetMapping("/thumbnails/{filename:.+}/result")
    @ResponseBody
    public ResponseEntity<Resource> retrieveThumbnail(@PathVariable String filename) {
        Resource file = persistentStorage.loadAsResource(filename);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.IMAGE_JPEG)
                .body(file);
    }

    @DeleteMapping("/thumbnails/{filename:.+}/result")
    @ResponseBody
    public ResponseEntity<HttpStatus> deleteThumbnail(@PathVariable String filename) {
        persistentStorage.delete(filename);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<?> handleStorageException(StorageFileNotFoundException exc) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
