/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk.json;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Log severity level for session log messages.
 *
 * @see com.github.copilot.sdk.CopilotSession#log(String,
 *      SessionLogRequestLevel, Boolean)
 * @since 1.0.0
 */
public enum SessionLogRequestLevel {

    @JsonProperty("info")
    INFO,

    @JsonProperty("warning")
    WARNING,

    @JsonProperty("error")
    ERROR
}
