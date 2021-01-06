package com.github.luigidb.thumbnailservice.services;

import com.github.luigidb.thumbnailservice.controllers.StorageException;
import com.github.luigidb.thumbnailservice.controllers.StorageFileNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    public FileSystemStorageService(String folder) {
        this.rootLocation = Paths.get(folder);
    }

    @Override
    public void store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            String originalFilename = file.getOriginalFilename();
            InputStream fileContent = file.getInputStream();

            Path destinationFile = rootLocation
                    .resolve(Paths.get(originalFilename))
                    .normalize()
                    .toAbsolutePath();
            if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
                //This way files can only be stored in the standard path
                throw new StorageException("Cannot store file outside current directory.");
            }
            try (InputStream inputStream = fileContent) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    @Override
    public void store(Image img, String filename) {
        Path destinationFile = rootLocation
                .resolve(Paths.get(filename))
                .normalize()
                .toAbsolutePath();
        if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
            //This way files can only be stored in the standard path
            throw new StorageException("Cannot store file outside current directory.");
        }
        try {
            ImageIO.write(
                    convertToBufferedImage(img),
                    "png",
                    destinationFile.toFile());
        } catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    public static BufferedImage convertToBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bi = new BufferedImage(
                img.getWidth(null), img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics2D = bi.createGraphics();
        graphics2D.drawImage(img, 0, 0, null);
        graphics2D.dispose();

        return bi;
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void delete(String filename) {
        try {
            Files.deleteIfExists(rootLocation.resolve(filename));
        } catch (IOException e) {
            //do nothing
        }
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}