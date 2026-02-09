/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.copilot.sdk.JsonRpcClient;

/**
 * Tests for {@link ModelInfo} and related model DTO classes.
 */
class ModelInfoTest {

    private final ObjectMapper mapper = JsonRpcClient.getObjectMapper();

    @Test
    void testModelInfoFluentSetters() {
        ModelInfo model = new ModelInfo()
                .setId("claude-sonnet-4.5")
                .setName("Claude Sonnet 4.5");

        assertEquals("claude-sonnet-4.5", model.getId());
        assertEquals("Claude Sonnet 4.5", model.getName());
    }

    @Test
    void testModelInfoWithCapabilities() {
        ModelCapabilities capabilities = new ModelCapabilities();
        ModelInfo model = new ModelInfo()
                .setId("gpt-5")
                .setCapabilities(capabilities);

        assertEquals("gpt-5", model.getId());
        assertNotNull(model.getCapabilities());
    }

    @Test
    void testModelInfoJsonSerialization() throws Exception {
        ModelInfo model = new ModelInfo()
                .setId("test-model")
                .setName("Test Model");

        String json = mapper.writeValueAsString(model);

        assertNotNull(json);
        assertEquals(true, json.contains("\"id\":\"test-model\""));
        assertEquals(true, json.contains("\"name\":\"Test Model\""));
    }

    @Test
    void testModelInfoJsonDeserialization() throws Exception {
        String json = "{\"id\":\"claude-opus-4\",\"name\":\"Claude Opus 4\"}";

        ModelInfo model = mapper.readValue(json, ModelInfo.class);

        assertEquals("claude-opus-4", model.getId());
        assertEquals("Claude Opus 4", model.getName());
    }

    @Test
    void testModelInfoWithReasoningEfforts() {
        ModelInfo model = new ModelInfo()
                .setId("reasoning-model")
                .setSupportedReasoningEfforts(List.of("low", "medium", "high"))
                .setDefaultReasoningEffort("medium");

        assertEquals("reasoning-model", model.getId());
        assertNotNull(model.getSupportedReasoningEfforts());
        assertEquals(3, model.getSupportedReasoningEfforts().size());
        assertEquals("medium", model.getDefaultReasoningEffort());
    }

    @Test
    void testModelInfoReasoningEffortsSerialization() throws Exception {
        ModelInfo model = new ModelInfo()
                .setId("test")
                .setSupportedReasoningEfforts(List.of("low", "high"))
                .setDefaultReasoningEffort("low");

        String json = mapper.writeValueAsString(model);

        assertEquals(true, json.contains("\"supportedReasoningEfforts\""));
        assertEquals(true, json.contains("\"defaultReasoningEffort\":\"low\""));
    }

    @Test
    void testModelInfoWithoutReasoningEfforts() {
        ModelInfo model = new ModelInfo()
                .setId("standard-model")
                .setName("Standard Model");

        assertNull(model.getSupportedReasoningEfforts());
        assertNull(model.getDefaultReasoningEffort());
    }

    @Test
    void testModelInfoRoundTrip() throws Exception {
        ModelInfo original = new ModelInfo()
                .setId("test-model-1")
                .setName("Test Model 1")
                .setSupportedReasoningEfforts(List.of("low", "medium"))
                .setDefaultReasoningEffort("low");

        String json = mapper.writeValueAsString(original);
        ModelInfo deserialized = mapper.readValue(json, ModelInfo.class);

        assertEquals(original.getId(), deserialized.getId());
        assertEquals(original.getName(), deserialized.getName());
        assertEquals(original.getDefaultReasoningEffort(), deserialized.getDefaultReasoningEffort());
    }

    @Test
    void testModelInfoWithAllFields() {
        ModelCapabilities capabilities = new ModelCapabilities();
        ModelPolicy policy = new ModelPolicy();
        ModelBilling billing = new ModelBilling();

        ModelInfo model = new ModelInfo()
                .setId("full-model")
                .setName("Full Model")
                .setCapabilities(capabilities)
                .setPolicy(policy)
                .setBilling(billing)
                .setSupportedReasoningEfforts(List.of("low"))
                .setDefaultReasoningEffort("low");

        assertEquals("full-model", model.getId());
        assertNotNull(model.getCapabilities());
        assertNotNull(model.getPolicy());
        assertNotNull(model.getBilling());
        assertNotNull(model.getSupportedReasoningEfforts());
    }

    @Test
    void testModelCapabilitiesFluentSetters() {
        ModelSupports supports = new ModelSupports();
        ModelLimits limits = new ModelLimits();

        ModelCapabilities capabilities = new ModelCapabilities()
                .setSupports(supports)
                .setLimits(limits);

        assertNotNull(capabilities.getSupports());
        assertNotNull(capabilities.getLimits());
    }

    @Test
    void testComplexModelDeserialization() throws Exception {
        String json = "{"
                + "\"id\":\"test-model\","
                + "\"name\":\"Test\","
                + "\"capabilities\":{"
                + "  \"supports\":{},"
                + "  \"limits\":{}"
                + "},"
                + "\"supportedReasoningEfforts\":[\"low\",\"high\"],"
                + "\"defaultReasoningEffort\":\"low\""
                + "}";

        ModelInfo model = mapper.readValue(json, ModelInfo.class);

        assertEquals("test-model", model.getId());
        assertEquals("Test", model.getName());
        assertNotNull(model.getCapabilities());
        assertNotNull(model.getSupportedReasoningEfforts());
        assertEquals(2, model.getSupportedReasoningEfforts().size());
        assertEquals("low", model.getDefaultReasoningEffort());
    }
}
