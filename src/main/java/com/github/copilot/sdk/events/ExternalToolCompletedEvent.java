/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event: external_tool.completed
 * <p>
 * Emitted when an external tool invocation has been resolved. All connected
 * clients receive this broadcast so they can dismiss any UI for the request.
 *
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ExternalToolCompletedEvent extends AbstractSessionEvent {

    @JsonProperty("data")
    private ExternalToolCompletedData data;

    @Override
    public String getType() {
        return "external_tool.completed";
    }

    public ExternalToolCompletedData getData() {
        return data;
    }

    public void setData(ExternalToolCompletedData data) {
        this.data = data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExternalToolCompletedData(@JsonProperty("requestId") String requestId) {
    }
}
