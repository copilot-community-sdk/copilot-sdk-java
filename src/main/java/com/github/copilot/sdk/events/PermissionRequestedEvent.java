/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.copilot.sdk.json.PermissionRequest;

/**
 * Event: permission.requested
 * <p>
 * Emitted by a protocol v3 server when a permission is required for an
 * operation. The SDK handles this event automatically by invoking the
 * registered permission handler and responding via the
 * {@code session.permissions.handlePendingPermissionRequest} RPC.
 *
 * @since 1.0.14
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PermissionRequestedEvent extends AbstractSessionEvent {

    @JsonProperty("data")
    private PermissionRequestedData data;

    @Override
    public String getType() {
        return "permission.requested";
    }

    public PermissionRequestedData getData() {
        return data;
    }

    public void setData(PermissionRequestedData data) {
        this.data = data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PermissionRequestedData(@JsonProperty("requestId") String requestId,
            @JsonProperty("permissionRequest") PermissionRequest permissionRequest) {
    }
}
