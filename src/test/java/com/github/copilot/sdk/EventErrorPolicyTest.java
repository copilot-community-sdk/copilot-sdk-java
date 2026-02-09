/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link EventErrorPolicy}.
 */
class EventErrorPolicyTest {

    @Test
    void testEnumValues() {
        EventErrorPolicy[] values = EventErrorPolicy.values();
        assertEquals(2, values.length);
        assertEquals(EventErrorPolicy.SUPPRESS_AND_LOG_ERRORS, values[0]);
        assertEquals(EventErrorPolicy.PROPAGATE_AND_LOG_ERRORS, values[1]);
    }

    @Test
    void testValueOf() {
        assertEquals(EventErrorPolicy.SUPPRESS_AND_LOG_ERRORS,
                EventErrorPolicy.valueOf("SUPPRESS_AND_LOG_ERRORS"));
        assertEquals(EventErrorPolicy.PROPAGATE_AND_LOG_ERRORS,
                EventErrorPolicy.valueOf("PROPAGATE_AND_LOG_ERRORS"));
    }

    @Test
    void testEnumNotNull() {
        assertNotNull(EventErrorPolicy.SUPPRESS_AND_LOG_ERRORS);
        assertNotNull(EventErrorPolicy.PROPAGATE_AND_LOG_ERRORS);
    }
}
