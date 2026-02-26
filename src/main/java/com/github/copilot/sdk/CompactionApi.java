/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Provides session compaction APIs for a Copilot session.
 * <p>
 * Session compaction reduces the context size by summarizing older conversation
 * history. This allows longer conversations without hitting context limits.
 * <p>
 * Access via {@link CopilotSession#getCompaction()}.
 *
 * @since 1.0.10
 */
public final class CompactionApi {

    private final JsonRpcClient rpc;
    private final String sessionId;

    CompactionApi(JsonRpcClient rpc, String sessionId) {
        this.rpc = rpc;
        this.sessionId = sessionId;
    }

    /**
     * Compacts the session history to reduce context size.
     *
     * @return a future that resolves with the compaction result
     */
    public CompletableFuture<CompactResult> compact() {
        return rpc.invoke("session.compaction.compact", Map.of("sessionId", sessionId), CompactResult.class);
    }

    /**
     * Result of a compaction operation.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompactResult {

        @JsonProperty("success")
        private boolean success;

        @JsonProperty("tokensRemoved")
        private double tokensRemoved;

        @JsonProperty("messagesRemoved")
        private double messagesRemoved;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public double getTokensRemoved() {
            return tokensRemoved;
        }

        public void setTokensRemoved(double tokensRemoved) {
            this.tokensRemoved = tokensRemoved;
        }

        public double getMessagesRemoved() {
            return messagesRemoved;
        }

        public void setMessagesRemoved(double messagesRemoved) {
            this.messagesRemoved = messagesRemoved;
        }
    }
}
