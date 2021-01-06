package com.github.luigidb.thumbnailservice.utils;

import org.junit.jupiter.api.Test;

import static com.github.luigidb.thumbnailservice.utils.UtilityMethods.getThumbnailName;
import static org.junit.jupiter.api.Assertions.*;

class UtilityMethodsTest {

    @Test
    void assertTheThumbnailFileName() {
        assertEquals("thumbnail_something.jpg", getThumbnailName("something.jpg"));
    }
}