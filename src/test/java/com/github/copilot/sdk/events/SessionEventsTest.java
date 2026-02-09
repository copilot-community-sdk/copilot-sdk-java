/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.copilot.sdk.JsonRpcClient;

/**
 * Tests for session event classes.
 */
class SessionEventsTest {

    private final ObjectMapper mapper = JsonRpcClient.getObjectMapper();

    @Test
    void testSessionIdleEventType() {
        SessionIdleEvent event = new SessionIdleEvent();
        assertEquals("session.idle", event.getType());
    }

    @Test
    void testSessionIdleEventSerialization() throws Exception {
        SessionIdleEvent event = new SessionIdleEvent();
        event.setData(new SessionIdleEvent.SessionIdleData());

        String json = mapper.writeValueAsString(event);
        assertNotNull(json);
        assertEquals(true, json.contains("\"type\":\"session.idle\""));
    }

    @Test
    void testSessionIdleEventDeserialization() throws Exception {
        String json = "{\"type\":\"session.idle\",\"data\":{}}";

        SessionIdleEvent event = mapper.readValue(json, SessionIdleEvent.class);

        assertEquals("session.idle", event.getType());
        assertNotNull(event.getData());
    }

    @Test
    void testSessionStartEventType() {
        SessionStartEvent event = new SessionStartEvent();
        assertEquals("session.start", event.getType());
    }

    @Test
    void testSessionStartEventDataGettersAndSetters() {
        SessionStartEvent.SessionStartData data = new SessionStartEvent.SessionStartData();

        data.setSessionId("test-session-123");
        assertEquals("test-session-123", data.getSessionId());

        data.setVersion(1.0);
        assertEquals(1.0, data.getVersion(), 0.01);

        data.setProducer("test-producer");
        assertEquals("test-producer", data.getProducer());

        data.setCopilotVersion("1.0.0");
        assertEquals("1.0.0", data.getCopilotVersion());

        OffsetDateTime now = OffsetDateTime.now();
        data.setStartTime(now);
        assertEquals(now, data.getStartTime());

        data.setSelectedModel("gpt-5");
        assertEquals("gpt-5", data.getSelectedModel());
    }

    @Test
    void testSessionStartEventSerialization() throws Exception {
        SessionStartEvent.SessionStartData data = new SessionStartEvent.SessionStartData();
        data.setSessionId("abc-123");
        data.setVersion(2.0);
        data.setSelectedModel("test-model");

        SessionStartEvent event = new SessionStartEvent();
        event.setData(data);

        String json = mapper.writeValueAsString(event);

        assertNotNull(json);
        assertEquals(true, json.contains("\"type\":\"session.start\""));
        assertEquals(true, json.contains("\"sessionId\":\"abc-123\""));
        assertEquals(true, json.contains("\"version\":2.0"));
    }

    @Test
    void testSessionStartEventDeserialization() throws Exception {
        String json = "{"
                + "\"type\":\"session.start\","
                + "\"data\":{"
                + "  \"sessionId\":\"test-123\","
                + "  \"version\":1.5,"
                + "  \"producer\":\"cli\","
                + "  \"copilotVersion\":\"1.2.3\","
                + "  \"selectedModel\":\"claude-opus-4\""
                + "}}";

        SessionStartEvent event = mapper.readValue(json, SessionStartEvent.class);

        assertEquals("session.start", event.getType());
        assertNotNull(event.getData());
        assertEquals("test-123", event.getData().getSessionId());
        assertEquals(1.5, event.getData().getVersion(), 0.01);
        assertEquals("cli", event.getData().getProducer());
        assertEquals("1.2.3", event.getData().getCopilotVersion());
        assertEquals("claude-opus-4", event.getData().getSelectedModel());
    }

    @Test
    void testSessionStartEventWithTimestamp() throws Exception {
        String json = "{"
                + "\"type\":\"session.start\","
                + "\"data\":{"
                + "  \"sessionId\":\"test\","
                + "  \"startTime\":\"2024-01-15T10:30:00Z\""
                + "}}";

        SessionStartEvent event = mapper.readValue(json, SessionStartEvent.class);

        assertNotNull(event.getData());
        assertNotNull(event.getData().getStartTime());
    }

    @Test
    void testSessionStartEventPartialData() throws Exception {
        String json = "{"
                + "\"type\":\"session.start\","
                + "\"data\":{"
                + "  \"sessionId\":\"minimal\""
                + "}}";

        SessionStartEvent event = mapper.readValue(json, SessionStartEvent.class);

        assertEquals("session.start", event.getType());
        assertEquals("minimal", event.getData().getSessionId());
        assertNull(event.getData().getProducer());
        assertNull(event.getData().getCopilotVersion());
    }

    @Test
    void testSessionEventRoundTrip() throws Exception {
        SessionStartEvent.SessionStartData data = new SessionStartEvent.SessionStartData();
        data.setSessionId("roundtrip-test");
        data.setVersion(3.0);
        data.setSelectedModel("test-model");

        SessionStartEvent original = new SessionStartEvent();
        original.setData(data);

        String json = mapper.writeValueAsString(original);
        SessionStartEvent deserialized = mapper.readValue(json, SessionStartEvent.class);

        assertEquals(original.getType(), deserialized.getType());
        assertEquals(original.getData().getSessionId(), deserialized.getData().getSessionId());
        assertEquals(original.getData().getVersion(), deserialized.getData().getVersion(), 0.01);
        assertEquals(original.getData().getSelectedModel(), deserialized.getData().getSelectedModel());
    }
}
