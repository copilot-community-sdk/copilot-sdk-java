/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event: external_tool.requested
 * <p>
 * Emitted when a tool registered by an external SDK client is requested. In
 * protocol v3, tool calls are broadcast as session events to all connected
 * clients. The client that registered the tool handles the invocation and
 * responds via {@code session.tools.handlePendingToolCall}.
 *
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ExternalToolRequestedEvent extends AbstractSessionEvent {

    @JsonProperty("data")
    private ExternalToolRequestedData data;

    @Override
    public String getType() {
        return "external_tool.requested";
    }

    public ExternalToolRequestedData getData() {
        return data;
    }

    public void setData(ExternalToolRequestedData data) {
        this.data = data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExternalToolRequestedData(@JsonProperty("requestId") String requestId,
            @JsonProperty("sessionId") String sessionId, @JsonProperty("toolCallId") String toolCallId,
            @JsonProperty("toolName") String toolName, @JsonProperty("arguments") Object arguments) {
    }
}
