package com.github.luigidb.thumbnailservice.services.impl;

import com.github.luigidb.thumbnailservice.exceptions.StorageException;
import com.github.luigidb.thumbnailservice.exceptions.StorageFileNotFoundException;
import com.github.luigidb.thumbnailservice.services.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class FileSystemStorage implements StorageService {

    private final Path rootLocation;

    public FileSystemStorage(String folder) {
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
            Optional<Resource> resource = getResourceByName(filename);
            if(resource.isPresent())
                return resource.get();
            else
                throw new StorageFileNotFoundException("Could not read file: " + filename);
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
    public boolean exist(String filename) {
        try {
            return getResourceByName(filename).isPresent();
        } catch (MalformedURLException e) {
            return false;
        }

    }

    private Optional<Resource> getResourceByName(String filename) throws MalformedURLException {
        Path file = load(filename);
        Resource resource = new UrlResource(file.toUri());
        if (resource.exists() || resource.isReadable())
            return Optional.of(resource);
        else
            return Optional.empty();
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