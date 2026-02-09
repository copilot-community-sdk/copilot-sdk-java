/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests for {@link SystemMessageMode}.
 */
class SystemMessageModeTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testEnumValues() {
        SystemMessageMode[] values = SystemMessageMode.values();
        assertEquals(2, values.length);
        assertEquals(SystemMessageMode.APPEND, values[0]);
        assertEquals(SystemMessageMode.REPLACE, values[1]);
    }

    @Test
    void testValueOf() {
        assertEquals(SystemMessageMode.APPEND, SystemMessageMode.valueOf("APPEND"));
        assertEquals(SystemMessageMode.REPLACE, SystemMessageMode.valueOf("REPLACE"));
    }

    @Test
    void testGetValue() {
        assertEquals("append", SystemMessageMode.APPEND.getValue());
        assertEquals("replace", SystemMessageMode.REPLACE.getValue());
    }

    @Test
    void testJsonSerialization() throws Exception {
        String appendJson = mapper.writeValueAsString(SystemMessageMode.APPEND);
        assertEquals("\"append\"", appendJson);

        String replaceJson = mapper.writeValueAsString(SystemMessageMode.REPLACE);
        assertEquals("\"replace\"", replaceJson);
    }

    @Test
    void testJsonDeserialization() throws Exception {
        SystemMessageMode append = mapper.readValue("\"append\"", SystemMessageMode.class);
        assertEquals(SystemMessageMode.APPEND, append);

        SystemMessageMode replace = mapper.readValue("\"replace\"", SystemMessageMode.class);
        assertEquals(SystemMessageMode.REPLACE, replace);
    }

    @Test
    void testEnumNotNull() {
        assertNotNull(SystemMessageMode.APPEND);
        assertNotNull(SystemMessageMode.REPLACE);
        assertNotNull(SystemMessageMode.APPEND.getValue());
        assertNotNull(SystemMessageMode.REPLACE.getValue());
    }
}
