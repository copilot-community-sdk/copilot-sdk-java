/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event: command.completed
 * <p>
 * Emitted when a queued command has been resolved. Clients should dismiss any
 * pending UI for the associated request.
 *
 * @since 1.0.14
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CommandCompletedEvent extends AbstractSessionEvent {

    @JsonProperty("data")
    private CommandCompletedData data;

    @Override
    public String getType() {
        return "command.completed";
    }

    public CommandCompletedData getData() {
        return data;
    }

    public void setData(CommandCompletedData data) {
        this.data = data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CommandCompletedData(@JsonProperty("requestId") String requestId) {
    }
}
