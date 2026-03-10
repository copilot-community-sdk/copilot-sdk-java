/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event: command.queued
 * <p>
 * Emitted when a slash command has been queued for approval.
 *
 * @since 1.0.14
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CommandQueuedEvent extends AbstractSessionEvent {

    @JsonProperty("data")
    private CommandQueuedData data;

    @Override
    public String getType() {
        return "command.queued";
    }

    public CommandQueuedData getData() {
        return data;
    }

    public void setData(CommandQueuedData data) {
        this.data = data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CommandQueuedData(@JsonProperty("requestId") String requestId,
            @JsonProperty("command") String command) {
    }
}
