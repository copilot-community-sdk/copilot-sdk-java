/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.copilot.sdk.JsonRpcClient;

/**
 * Tests for JSON-RPC protocol classes: {@link JsonRpcRequest},
 * {@link JsonRpcResponse}, and {@link JsonRpcError}.
 */
class JsonRpcProtocolTest {

    private final ObjectMapper mapper = JsonRpcClient.getObjectMapper();

    @Test
    void testJsonRpcRequestSerialization() throws Exception {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setId(1L);
        request.setMethod("test.method");
        request.setParams(Map.of("key", "value"));

        String json = mapper.writeValueAsString(request);

        assertNotNull(json);
        assertEquals(true, json.contains("\"jsonrpc\":\"2.0\""));
        assertEquals(true, json.contains("\"id\":1"));
        assertEquals(true, json.contains("\"method\":\"test.method\""));
    }

    @Test
    void testJsonRpcRequestDeserialization() throws Exception {
        String json = "{\"jsonrpc\":\"2.0\",\"id\":42,\"method\":\"session.create\",\"params\":{\"model\":\"test\"}}";

        JsonRpcRequest request = mapper.readValue(json, JsonRpcRequest.class);

        assertEquals("2.0", request.getJsonrpc());
        assertEquals(42L, request.getId());
        assertEquals("session.create", request.getMethod());
        assertNotNull(request.getParams());
    }

    @Test
    void testJsonRpcResponseWithResult() throws Exception {
        JsonRpcResponse response = new JsonRpcResponse();
        response.setJsonrpc("2.0");
        response.setId(1);
        response.setResult(Map.of("success", true));

        String json = mapper.writeValueAsString(response);

        assertNotNull(json);
        assertEquals(true, json.contains("\"jsonrpc\":\"2.0\""));
        assertEquals(true, json.contains("\"id\":1"));
        assertEquals(true, json.contains("\"result\""));
        assertEquals(false, json.contains("\"error\""));
    }

    @Test
    void testJsonRpcResponseWithError() throws Exception {
        JsonRpcError error = new JsonRpcError();
        error.setCode(-32700);
        error.setMessage("Parse error");

        JsonRpcResponse response = new JsonRpcResponse();
        response.setJsonrpc("2.0");
        response.setId(1);
        response.setError(error);

        String json = mapper.writeValueAsString(response);

        assertNotNull(json);
        assertEquals(true, json.contains("\"error\""));
        assertEquals(true, json.contains("\"code\":-32700"));
        assertEquals(true, json.contains("\"message\":\"Parse error\""));
        assertEquals(false, json.contains("\"result\""));
    }

    @Test
    void testJsonRpcResponseDeserialization() throws Exception {
        String json = "{\"jsonrpc\":\"2.0\",\"id\":5,\"result\":{\"sessionId\":\"abc-123\"}}";

        JsonRpcResponse response = mapper.readValue(json, JsonRpcResponse.class);

        assertEquals("2.0", response.getJsonrpc());
        assertEquals(5, response.getId());
        assertNotNull(response.getResult());
        assertNull(response.getError());
    }

    @Test
    void testJsonRpcErrorSerialization() throws Exception {
        JsonRpcError error = new JsonRpcError();
        error.setCode(-32601);
        error.setMessage("Method not found");
        error.setData(Map.of("method", "unknown.method"));

        String json = mapper.writeValueAsString(error);

        assertNotNull(json);
        assertEquals(true, json.contains("\"code\":-32601"));
        assertEquals(true, json.contains("\"message\":\"Method not found\""));
        assertEquals(true, json.contains("\"data\""));
    }

    @Test
    void testJsonRpcErrorWithoutData() throws Exception {
        JsonRpcError error = new JsonRpcError();
        error.setCode(-32600);
        error.setMessage("Invalid Request");

        String json = mapper.writeValueAsString(error);

        assertNotNull(json);
        assertEquals(true, json.contains("\"code\":-32600"));
        assertEquals(false, json.contains("\"data\""));
    }

    @Test
    void testJsonRpcErrorDeserialization() throws Exception {
        String json = "{\"code\":-32603,\"message\":\"Internal error\",\"data\":\"Stack trace here\"}";

        JsonRpcError error = mapper.readValue(json, JsonRpcError.class);

        assertEquals(-32603, error.getCode());
        assertEquals("Internal error", error.getMessage());
        assertNotNull(error.getData());
    }

    @Test
    void testJsonRpcRequestWithoutParams() throws Exception {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setId(10L);
        request.setMethod("ping");

        String json = mapper.writeValueAsString(request);

        assertNotNull(json);
        assertEquals(false, json.contains("\"params\""));
    }

    @Test
    void testStandardErrorCodes() {
        JsonRpcError parseError = new JsonRpcError();
        parseError.setCode(-32700);
        parseError.setMessage("Parse error");
        assertEquals(-32700, parseError.getCode());

        JsonRpcError invalidRequest = new JsonRpcError();
        invalidRequest.setCode(-32600);
        invalidRequest.setMessage("Invalid Request");
        assertEquals(-32600, invalidRequest.getCode());

        JsonRpcError methodNotFound = new JsonRpcError();
        methodNotFound.setCode(-32601);
        methodNotFound.setMessage("Method not found");
        assertEquals(-32601, methodNotFound.getCode());

        JsonRpcError invalidParams = new JsonRpcError();
        invalidParams.setCode(-32602);
        invalidParams.setMessage("Invalid params");
        assertEquals(-32602, invalidParams.getCode());

        JsonRpcError internalError = new JsonRpcError();
        internalError.setCode(-32603);
        internalError.setMessage("Internal error");
        assertEquals(-32603, internalError.getCode());
    }

    @Test
    void testRoundTripJsonRpcRequest() throws Exception {
        JsonRpcRequest original = new JsonRpcRequest();
        original.setJsonrpc("2.0");
        original.setId(99L);
        original.setMethod("test.roundtrip");
        original.setParams(Map.of("test", "data"));

        String json = mapper.writeValueAsString(original);
        JsonRpcRequest deserialized = mapper.readValue(json, JsonRpcRequest.class);

        assertEquals(original.getJsonrpc(), deserialized.getJsonrpc());
        assertEquals(original.getId(), deserialized.getId());
        assertEquals(original.getMethod(), deserialized.getMethod());
        assertNotNull(deserialized.getParams());
    }

    @Test
    void testRoundTripJsonRpcResponse() throws Exception {
        JsonRpcResponse original = new JsonRpcResponse();
        original.setJsonrpc("2.0");
        original.setId(77);
        original.setResult(Map.of("status", "ok"));

        String json = mapper.writeValueAsString(original);
        JsonRpcResponse deserialized = mapper.readValue(json, JsonRpcResponse.class);

        assertEquals(original.getJsonrpc(), deserialized.getJsonrpc());
        assertEquals(original.getId(), deserialized.getId());
        assertNotNull(deserialized.getResult());
        assertNull(deserialized.getError());
    }
}
