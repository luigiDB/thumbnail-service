package com.github.luigidb.thumbnailservice.services.impl;

import com.github.luigidb.thumbnailservice.services.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import static com.github.luigidb.thumbnailservice.utils.UtilityMethods.getThumbnailName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


class ThumbnailizerTest {

    private final String FILENAME = "filename.jpg";
    private StorageService tempStorage;
    private StorageService persistentStorage;
    private Thumbnailizer thumbnailizer;

    @BeforeEach
    void setUp() {
        tempStorage = mock(StorageService.class);
        persistentStorage = mock(StorageService.class);
        thumbnailizer = new Thumbnailizer(tempStorage, persistentStorage);
    }

    @Test
    void testThatAThumbnailIsCreated() throws URISyntaxException, IOException {
        URL originalImageUrl = getClass().getClassLoader().getResource("tux.jpg");
        URL thumbnailUrl = getClass().getClassLoader().getResource("thumbnail_tux.jpg");
        Path path = new File(thumbnailUrl.toURI()).toPath();
        when(tempStorage.load(FILENAME)).thenReturn(path);

        thumbnailizer.asyncThumbnail(FILENAME);

        verify(tempStorage).delete(FILENAME);
        ArgumentCaptor<Image> argument = ArgumentCaptor.forClass(Image.class);
        ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
        verify(persistentStorage).store(argument.capture(), filenameCaptor.capture());
        assertTrue(argument.getValue() instanceof Image);
        assertEquals(getThumbnailName(FILENAME), filenameCaptor.getValue());

    }
}