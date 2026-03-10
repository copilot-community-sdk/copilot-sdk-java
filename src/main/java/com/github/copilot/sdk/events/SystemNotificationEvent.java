/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event: system.notification
 * <p>
 * Emitted to deliver a system-level notification to the client. The
 * notification text is typically wrapped in {@code <system_notification>} XML
 * tags and the {@code kind} field identifies what triggered the notification
 * (e.g., a background agent completing).
 *
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SystemNotificationEvent extends AbstractSessionEvent {

    @JsonProperty("data")
    private SystemNotificationData data;

    @Override
    public String getType() {
        return "system.notification";
    }

    public SystemNotificationData getData() {
        return data;
    }

    public void setData(SystemNotificationData data) {
        this.data = data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SystemNotificationData(@JsonProperty("content") String content, @JsonProperty("kind") Object kind) {
    }
}
