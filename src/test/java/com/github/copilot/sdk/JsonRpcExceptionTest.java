/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link JsonRpcException}.
 */
class JsonRpcExceptionTest {

    @Test
    void testConstructor() {
        JsonRpcException exception = new JsonRpcException(-32700, "Parse error");

        assertEquals(-32700, exception.getCode());
        assertEquals("Parse error", exception.getMessage());
        assertNotNull(exception);
    }

    @Test
    void testStandardErrorCodes() {
        JsonRpcException parseError = new JsonRpcException(-32700, "Parse error");
        assertEquals(-32700, parseError.getCode());

        JsonRpcException invalidRequest = new JsonRpcException(-32600, "Invalid request");
        assertEquals(-32600, invalidRequest.getCode());

        JsonRpcException methodNotFound = new JsonRpcException(-32601, "Method not found");
        assertEquals(-32601, methodNotFound.getCode());

        JsonRpcException invalidParams = new JsonRpcException(-32602, "Invalid params");
        assertEquals(-32602, invalidParams.getCode());

        JsonRpcException internalError = new JsonRpcException(-32603, "Internal error");
        assertEquals(-32603, internalError.getCode());
    }

    @Test
    void testIsRuntimeException() {
        // Verify that JsonRpcException remains a RuntimeException in the type hierarchy.
        assertEquals(RuntimeException.class, JsonRpcException.class.getSuperclass());
    }

    @Test
    void testCustomErrorCode() {
        JsonRpcException customException = new JsonRpcException(-32000, "Custom server error");
        assertEquals(-32000, customException.getCode());
        assertEquals("Custom server error", customException.getMessage());
    }

    @Test
    void testMessageAccess() {
        String errorMessage = "Detailed error information";
        JsonRpcException exception = new JsonRpcException(-32603, errorMessage);

        assertEquals(errorMessage, exception.getMessage());
        assertTrue(exception.toString().contains(errorMessage));
    }
}
