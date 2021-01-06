package com.github.luigidb.thumbnailservice.controllers;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class ThumbnailControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    @Qualifier("EphemeralStorage")
    private StorageService storageService;

    @MockBean
    @Qualifier("ThumbnailStorage")
    StorageService persistentService;

    @MockBean
    Thumbnailizer thumbnailizer;

    @Test
    public void shouldSaveUploadedFile() throws Exception {

        InputStream image = getClass().getClassLoader().getResourceAsStream("tux.jpg");

        doNothing().when(thumbnailizer).asyncThumbnail(any());

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "tux.jpg",
                "image/jpeg",
                image.readAllBytes()
        );
        this.mvc.perform(
                multipart("/thumbnails")
                        .file(multipartFile)
        )
                .andExpect(status().is2xxSuccessful());

        ArgumentCaptor<MultipartFile> argument = ArgumentCaptor.forClass(MultipartFile.class);
        verify(storageService).store(argument.capture());
        assertTrue(IOUtils.contentEquals(argument.getValue().getInputStream(),
                multipartFile.getInputStream()));
    }

}