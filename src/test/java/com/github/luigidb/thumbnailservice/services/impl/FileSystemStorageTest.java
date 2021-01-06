package com.github.luigidb.thumbnailservice.services.impl;

import com.github.luigidb.thumbnailservice.exceptions.StorageException;
import com.github.luigidb.thumbnailservice.exceptions.StorageFileNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileSystemStorageTest {

    private FileSystemStorage fileSystemStorage;
    private Path tempDir;


    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        this.tempDir = tempDir;
        fileSystemStorage = new FileSystemStorage(this.tempDir);
    }

    @Test
    void testInit() {
        fileSystemStorage.init();
        assertTrue(tempDir.toFile().isDirectory());
    }

    @Test
    void testThatStoreThrowExceptionIfTheFileIsEmpty() {
        MultipartFile mock = mock(MultipartFile.class);
        when(mock.isEmpty()).thenReturn(true);

        assertThrows(
                StorageException.class,
                () -> fileSystemStorage.store(mock),
            "Failed to store empty file."
        );
    }

    @Test
    void testThatStoreSaveTheFile() throws IOException {
        MultipartFile mock = mock(MultipartFile.class);
        when(mock.isEmpty()).thenReturn(false);
        when(mock.getOriginalFilename()).thenReturn("something");
        when(mock.getInputStream()).thenReturn(mock(InputStream.class));

        fileSystemStorage.store(mock);
        assertTrue(tempDir.resolve("something").normalize().toAbsolutePath().toFile().exists());
    }


    @Test
    void testExist() throws IOException {
        assertFalse(fileSystemStorage.exist("something"));
        new File(tempDir.toFile(), "something").createNewFile();
        assertTrue(fileSystemStorage.exist("something"));
    }

    @Test
    void testLoadAsResource() throws IOException {
        assertThrows(
                StorageFileNotFoundException.class,
                () -> fileSystemStorage.loadAsResource("something"),
                "Could not read file: something"
        );

        new File(tempDir.toFile(), "something").createNewFile();
        assertNotNull(fileSystemStorage.loadAsResource("something"));
    }

    @Test
    void testDeleteWhenFileExist() throws IOException {
        File file = new File(tempDir.toFile(), "something");
        file.createNewFile();
        fileSystemStorage.delete("something");
        assertFalse(file.isFile());
    }

    @Test
    void testDeleteWhenFileDonTExist() throws IOException {
        File file = new File(tempDir.toFile(), "something");
        fileSystemStorage.delete("something");
        assertFalse(file.isFile());
    }

    @Test
    void testDeleteAll() throws IOException {
        File file = new File(tempDir.toFile(), "something");
        file.createNewFile();
        File file2 = new File(tempDir.toFile(), "something2");
        file2.createNewFile();
        fileSystemStorage.deleteAll();
        assertFalse(file.isFile());
        assertFalse(file2.isFile());
    }
}