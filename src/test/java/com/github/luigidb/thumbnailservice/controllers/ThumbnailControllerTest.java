package com.github.luigidb.thumbnailservice.controllers;

import com.github.luigidb.thumbnailservice.exceptions.StorageFileNotFoundException;
import com.github.luigidb.thumbnailservice.services.StorageService;
import com.github.luigidb.thumbnailservice.services.impl.Thumbnailizer;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

import static com.github.luigidb.thumbnailservice.utils.UtilityMethods.getThumbnailName;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class ThumbnailControllerTest {

    private final String IMAGE_NAME = "name.jpg";

    @Autowired
    private MockMvc mvc;

    @MockBean
    @Qualifier("EphemeralStorage")
    private StorageService tempStorage;

    @MockBean
    @Qualifier("ThumbnailStorage")
    StorageService persistentStorage;

    @MockBean
    Thumbnailizer thumbnailizer;

    @Test
    public void testThatWhenANewImageIsProvidedTheRequestIsAccepted() throws Exception {
        InputStream image = getClass().getClassLoader().getResourceAsStream("tux.jpg");
        doNothing().when(thumbnailizer).asyncThumbnail(any());

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "tux.jpg",
                "image/jpeg",
                image.readAllBytes()
        );
        this.mvc
                .perform(multipart("/thumbnails").file(multipartFile))
                .andExpect(status().is2xxSuccessful());

        ArgumentCaptor<MultipartFile> argument = ArgumentCaptor.forClass(MultipartFile.class);
        verify(tempStorage).store(argument.capture());
        assertTrue(IOUtils.contentEquals(argument.getValue().getInputStream(), multipartFile.getInputStream()));
    }

    @Test
    public void testThatWhenAThumbnailHasAlreadyBeenCreatedTheMiddleApiReturnARedirect() throws Exception {
        when(persistentStorage.exist(getThumbnailName(IMAGE_NAME))).thenReturn(true);
        this.mvc
                .perform(get("/thumbnails/" + IMAGE_NAME))
                .andExpect(status().is3xxRedirection())
                .andExpect(content().json(
                        String.format("{\"_links\":{\"thumbnail\":{\"href\":\"http://localhost/thumbnails/thumbnail_%s/result\"}}}", IMAGE_NAME)
                ));
    }

    @Test
    public void testThatWhenAThumbnailHasNotAlreadyBeenCreatedTheMiddleApiReturnANotFound() throws Exception {
        when(persistentStorage.exist(getThumbnailName(IMAGE_NAME))).thenReturn(false);
        this.mvc
                .perform(get("/thumbnails/" + IMAGE_NAME))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testThatWhenAThumbnailIsPresentTheApiReturnTheThumbnail() throws Exception {
        Resource resource = new UrlResource(getClass().getClassLoader().getResource("tux.jpg"));
        when(persistentStorage.loadAsResource(getThumbnailName(IMAGE_NAME))).thenReturn(resource);
        this.mvc
                .perform(get(String.format("/thumbnails/%s/result", getThumbnailName(IMAGE_NAME))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(header().stringValues("MediaType.IMAGE_JPEG"));
    }

    @Test
    public void testThatWhenAThumbnailIsNotPresentTheApiReturnNotFound() throws Exception {
        when(persistentStorage.loadAsResource(getThumbnailName(IMAGE_NAME))).thenThrow(new StorageFileNotFoundException("not found"));
        this.mvc
                .perform(get(String.format("/thumbnails/%s/result", getThumbnailName(IMAGE_NAME))))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testThatADeleteOperationIsPerformed() throws Exception {
        this.mvc
                .perform(delete(String.format("/thumbnails/%s/result", getThumbnailName(IMAGE_NAME))))
                .andExpect(status().isAccepted());
        verify(persistentStorage, times(1)).delete(getThumbnailName(IMAGE_NAME));
    }

    @Test
    public void testThatADeleteOperationOnAnNonExistentThumbnailIsPerformed() throws Exception {
        this.mvc
                .perform(delete(String.format("/thumbnails/%s/result", getThumbnailName(IMAGE_NAME))))
                .andExpect(status().isAccepted());
    }


    @Test
    void testThatServer() {
    }
}