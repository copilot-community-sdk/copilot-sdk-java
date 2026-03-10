/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Event: external_tool.requested
 * <p>
 * Emitted by a protocol v3 server when a registered tool is invoked. The SDK
 * handles this event automatically by executing the tool and responding via the
 * {@code session.tools.handlePendingToolCall} RPC.
 *
 * @since 1.0.14
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
    public static class ExternalToolRequestedData {

        @JsonProperty("requestId")
        private String requestId;

        @JsonProperty("sessionId")
        private String sessionId;

        @JsonProperty("toolCallId")
        private String toolCallId;

        @JsonProperty("toolName")
        private String toolName;

        @JsonProperty("arguments")
        private JsonNode arguments;

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getToolCallId() {
            return toolCallId;
        }

        public void setToolCallId(String toolCallId) {
            this.toolCallId = toolCallId;
        }

        public String getToolName() {
            return toolName;
        }

        public void setToolName(String toolName) {
            this.toolName = toolName;
        }

        public JsonNode getArguments() {
            return arguments;
        }

        public void setArguments(JsonNode arguments) {
            this.arguments = arguments;
        }
    }
}
