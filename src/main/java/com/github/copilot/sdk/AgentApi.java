/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.copilot.sdk.json.AgentInfo;

/**
 * Provides agent selection and management APIs for a Copilot session.
 * <p>
 * This API allows listing available custom agents, selecting an agent for the
 * current session, and deselecting the currently active agent.
 * <p>
 * Access via {@link CopilotSession#getAgent()}.
 *
 * @since 1.0.10
 */
public final class AgentApi {

    private final JsonRpcClient rpc;
    private final String sessionId;

    AgentApi(JsonRpcClient rpc, String sessionId) {
        this.rpc = rpc;
        this.sessionId = sessionId;
    }

    /**
     * Lists all available custom agents in this session.
     *
     * @return a future that resolves with the list of available agents
     */
    public CompletableFuture<ListResult> list() {
        return rpc.invoke("session.agent.list", Map.of("sessionId", sessionId), ListResult.class);
    }

    /**
     * Gets the currently selected agent.
     *
     * @return a future that resolves with the current agent, or {@code null} agent
     *         if none is selected
     */
    public CompletableFuture<GetCurrentResult> getCurrent() {
        return rpc.invoke("session.agent.getCurrent", Map.of("sessionId", sessionId), GetCurrentResult.class);
    }

    /**
     * Selects an agent by name.
     *
     * @param name
     *            the name of the agent to select
     * @return a future that resolves with the selected agent
     */
    public CompletableFuture<SelectResult> select(String name) {
        return rpc.invoke("session.agent.select", Map.of("sessionId", sessionId, "name", name), SelectResult.class);
    }

    /**
     * Deselects the currently active agent, reverting to the default agent.
     *
     * @return a future that completes when the agent is deselected
     */
    public CompletableFuture<Void> deselect() {
        return rpc.invoke("session.agent.deselect", Map.of("sessionId", sessionId), Void.class);
    }

    /**
     * Result of listing available agents.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ListResult {

        @JsonProperty("agents")
        private List<AgentInfo> agents;

        public List<AgentInfo> getAgents() {
            return agents;
        }

        public void setAgents(List<AgentInfo> agents) {
            this.agents = agents;
        }
    }

    /**
     * Result of getting the current agent.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GetCurrentResult {

        @JsonProperty("agent")
        private AgentInfo agent;

        /** Returns the currently selected agent, or {@code null} if none. */
        public AgentInfo getAgent() {
            return agent;
        }

        public void setAgent(AgentInfo agent) {
            this.agent = agent;
        }
    }

    /**
     * Result of selecting an agent.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SelectResult {

        @JsonProperty("agent")
        private AgentInfo agent;

        public AgentInfo getAgent() {
            return agent;
        }

        public void setAgent(AgentInfo agent) {
            this.agent = agent;
        }
    }
}
