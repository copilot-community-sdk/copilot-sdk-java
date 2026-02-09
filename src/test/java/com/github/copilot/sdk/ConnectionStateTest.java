/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ConnectionState}.
 */
class ConnectionStateTest {

    @Test
    void testEnumValues() {
        ConnectionState[] values = ConnectionState.values();
        assertEquals(4, values.length);
        assertEquals(ConnectionState.DISCONNECTED, values[0]);
        assertEquals(ConnectionState.CONNECTING, values[1]);
        assertEquals(ConnectionState.CONNECTED, values[2]);
        assertEquals(ConnectionState.ERROR, values[3]);
    }

    @Test
    void testValueOf() {
        assertEquals(ConnectionState.DISCONNECTED, ConnectionState.valueOf("DISCONNECTED"));
        assertEquals(ConnectionState.CONNECTING, ConnectionState.valueOf("CONNECTING"));
        assertEquals(ConnectionState.CONNECTED, ConnectionState.valueOf("CONNECTED"));
        assertEquals(ConnectionState.ERROR, ConnectionState.valueOf("ERROR"));
    }

    @Test
    void testEnumNotNull() {
        assertNotNull(ConnectionState.DISCONNECTED);
        assertNotNull(ConnectionState.CONNECTING);
        assertNotNull(ConnectionState.CONNECTED);
        assertNotNull(ConnectionState.ERROR);
    }

    @Test
    void testEnumOrdering() {
        ConnectionState[] values = ConnectionState.values();
        assertEquals(0, ConnectionState.DISCONNECTED.ordinal());
        assertEquals(1, ConnectionState.CONNECTING.ordinal());
        assertEquals(2, ConnectionState.CONNECTED.ordinal());
        assertEquals(3, ConnectionState.ERROR.ordinal());
    }
}
