/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event: system.notification
 * <p>
 * Emitted when a background agent or shell task completes, carrying structured
 * metadata about the completion.
 *
 * @since 1.0.14
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
    public record SystemNotificationData(@JsonProperty("content") String content,
            @JsonProperty("kind") java.util.Map<String, Object> kind) {
    }
}
