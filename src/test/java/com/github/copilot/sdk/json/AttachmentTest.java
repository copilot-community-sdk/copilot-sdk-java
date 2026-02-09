/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.copilot.sdk.JsonRpcClient;

/**
 * Tests for {@link Attachment} JSON serialization and deserialization.
 */
class AttachmentTest {

    private final ObjectMapper mapper = JsonRpcClient.getObjectMapper();

    @Test
    void testFluentSetters() {
        Attachment attachment = new Attachment()
                .setType("file")
                .setPath("/path/to/file.txt")
                .setDisplayName("Test File");

        assertEquals("file", attachment.getType());
        assertEquals("/path/to/file.txt", attachment.getPath());
        assertEquals("Test File", attachment.getDisplayName());
    }

    @Test
    void testGettersAndSetters() {
        Attachment attachment = new Attachment();

        attachment.setType("file");
        assertEquals("file", attachment.getType());

        attachment.setPath("/path/to/source.java");
        assertEquals("/path/to/source.java", attachment.getPath());

        attachment.setDisplayName("Main Source");
        assertEquals("Main Source", attachment.getDisplayName());
    }

    @Test
    void testJsonSerialization() throws Exception {
        Attachment attachment = new Attachment()
                .setType("file")
                .setPath("/src/main.java")
                .setDisplayName("Main.java");

        String json = mapper.writeValueAsString(attachment);

        assertNotNull(json);
        assertEquals(true, json.contains("\"type\":\"file\""));
        assertEquals(true, json.contains("\"path\":\"/src/main.java\""));
        assertEquals(true, json.contains("\"displayName\":\"Main.java\""));
    }

    @Test
    void testJsonDeserialization() throws Exception {
        String json = "{\"type\":\"file\",\"path\":\"/test.txt\",\"displayName\":\"Test\"}";

        Attachment attachment = mapper.readValue(json, Attachment.class);

        assertEquals("file", attachment.getType());
        assertEquals("/test.txt", attachment.getPath());
        assertEquals("Test", attachment.getDisplayName());
    }

    @Test
    void testRoundTripSerialization() throws Exception {
        Attachment original = new Attachment()
                .setType("file")
                .setPath("/path/to/document.pdf")
                .setDisplayName("Important Document");

        String json = mapper.writeValueAsString(original);
        Attachment deserialized = mapper.readValue(json, Attachment.class);

        assertEquals(original.getType(), deserialized.getType());
        assertEquals(original.getPath(), deserialized.getPath());
        assertEquals(original.getDisplayName(), deserialized.getDisplayName());
    }

    @Test
    void testNullFieldsNotSerialized() throws Exception {
        Attachment attachment = new Attachment().setType("file");

        String json = mapper.writeValueAsString(attachment);

        assertNotNull(json);
        assertEquals(true, json.contains("\"type\":\"file\""));
        assertEquals(false, json.contains("\"path\""));
        assertEquals(false, json.contains("\"displayName\""));
    }

    @Test
    void testPartialDeserialization() throws Exception {
        String json = "{\"type\":\"file\"}";

        Attachment attachment = mapper.readValue(json, Attachment.class);

        assertEquals("file", attachment.getType());
        assertNull(attachment.getPath());
        assertNull(attachment.getDisplayName());
    }

    @Test
    void testEmptyAttachment() {
        Attachment attachment = new Attachment();

        assertNull(attachment.getType());
        assertNull(attachment.getPath());
        assertNull(attachment.getDisplayName());
    }
}
