/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.github.copilot.sdk.json.CustomAgentConfig;
import com.github.copilot.sdk.json.MessageOptions;
import com.github.copilot.sdk.json.PermissionHandler;
import com.github.copilot.sdk.json.SessionConfig;

/**
 * Tests for agent selection and session compaction APIs.
 *
 * <p>
 * These tests use the shared CapiProxy infrastructure for deterministic API
 * response replay. Snapshots are stored in
 * test/snapshots/agent_and_compact_rpc/.
 * </p>
 */
public class AgentAndCompactRpcTest {

    private static E2ETestContext ctx;

    @BeforeAll
    static void setup() throws Exception {
        ctx = E2ETestContext.create();
    }

    @AfterAll
    static void teardown() throws Exception {
        if (ctx != null) {
            ctx.close();
        }
    }

    /**
     * Verifies that available custom agents can be listed.
     *
     * <p>
     * Requires live server — no snapshot available.
     * </p>
     */
    @Test
    @Disabled("Requires live server - no snapshot available for agent.list")
    void testShouldListAvailableCustomAgents(TestInfo testInfo) throws Exception {
        var customAgents = List.of(
                new CustomAgentConfig().setName("test-agent").setDisplayName("Test Agent")
                        .setDescription("A test agent").setPrompt("You are a test agent."),
                new CustomAgentConfig().setName("another-agent").setDisplayName("Another Agent")
                        .setDescription("Another test agent").setPrompt("You are another agent."));

        try (CopilotClient client = ctx.createClient()) {
            CopilotSession session = client.createSession(new SessionConfig()
                    .setOnPermissionRequest(PermissionHandler.APPROVE_ALL).setCustomAgents(customAgents)).get();

            var result = session.getAgent().list().get(30, TimeUnit.SECONDS);
            assertNotNull(result.getAgents());
            assertEquals(2, result.getAgents().size());
            assertEquals("test-agent", result.getAgents().get(0).getName());
            assertEquals("Test Agent", result.getAgents().get(0).getDisplayName());
            assertEquals("A test agent", result.getAgents().get(0).getDescription());
            assertEquals("another-agent", result.getAgents().get(1).getName());

            session.close();
        }
    }

    /**
     * Verifies that null is returned when no agent is selected.
     *
     * <p>
     * Requires live server — no snapshot available.
     * </p>
     */
    @Test
    @Disabled("Requires live server - no snapshot available for agent.getCurrent")
    void testShouldReturnNullWhenNoAgentIsSelected(TestInfo testInfo) throws Exception {
        var customAgents = List.of(new CustomAgentConfig().setName("test-agent").setDisplayName("Test Agent")
                .setDescription("A test agent").setPrompt("You are a test agent."));

        try (CopilotClient client = ctx.createClient()) {
            CopilotSession session = client.createSession(new SessionConfig()
                    .setOnPermissionRequest(PermissionHandler.APPROVE_ALL).setCustomAgents(customAgents)).get();

            var result = session.getAgent().getCurrent().get(30, TimeUnit.SECONDS);
            assertNull(result.getAgent());

            session.close();
        }
    }

    /**
     * Verifies that an agent can be selected and retrieved as current.
     *
     * <p>
     * Requires live server — no snapshot available.
     * </p>
     */
    @Test
    @Disabled("Requires live server - no snapshot available for agent.select")
    void testShouldSelectAndGetCurrentAgent(TestInfo testInfo) throws Exception {
        var customAgents = List.of(new CustomAgentConfig().setName("test-agent").setDisplayName("Test Agent")
                .setDescription("A test agent").setPrompt("You are a test agent."));

        try (CopilotClient client = ctx.createClient()) {
            CopilotSession session = client.createSession(new SessionConfig()
                    .setOnPermissionRequest(PermissionHandler.APPROVE_ALL).setCustomAgents(customAgents)).get();

            // Select the agent
            var selectResult = session.getAgent().select("test-agent").get(30, TimeUnit.SECONDS);
            assertNotNull(selectResult.getAgent());
            assertEquals("test-agent", selectResult.getAgent().getName());
            assertEquals("Test Agent", selectResult.getAgent().getDisplayName());

            // Verify getCurrent returns the selected agent
            var currentResult = session.getAgent().getCurrent().get(30, TimeUnit.SECONDS);
            assertNotNull(currentResult.getAgent());
            assertEquals("test-agent", currentResult.getAgent().getName());

            session.close();
        }
    }

    /**
     * Verifies that the current agent can be deselected.
     *
     * <p>
     * Requires live server — no snapshot available.
     * </p>
     */
    @Test
    @Disabled("Requires live server - no snapshot available for agent.deselect")
    void testShouldDeselectCurrentAgent(TestInfo testInfo) throws Exception {
        var customAgents = List.of(new CustomAgentConfig().setName("test-agent").setDisplayName("Test Agent")
                .setDescription("A test agent").setPrompt("You are a test agent."));

        try (CopilotClient client = ctx.createClient()) {
            CopilotSession session = client.createSession(new SessionConfig()
                    .setOnPermissionRequest(PermissionHandler.APPROVE_ALL).setCustomAgents(customAgents)).get();

            // Select then deselect
            session.getAgent().select("test-agent").get(30, TimeUnit.SECONDS);
            session.getAgent().deselect().get(30, TimeUnit.SECONDS);

            // Verify no agent is selected
            var currentResult = session.getAgent().getCurrent().get(30, TimeUnit.SECONDS);
            assertNull(currentResult.getAgent());

            session.close();
        }
    }

    /**
     * Verifies that an empty list is returned when no custom agents are configured.
     *
     * <p>
     * Requires live server — no snapshot available.
     * </p>
     */
    @Test
    @Disabled("Requires live server - no snapshot available for agent.list (no agents)")
    void testShouldReturnEmptyListWhenNoCustomAgentsConfigured(TestInfo testInfo) throws Exception {
        try (CopilotClient client = ctx.createClient()) {
            CopilotSession session = client
                    .createSession(new SessionConfig().setOnPermissionRequest(PermissionHandler.APPROVE_ALL)).get();

            var result = session.getAgent().list().get(30, TimeUnit.SECONDS);
            assertTrue(result.getAgents() == null || result.getAgents().isEmpty(),
                    "Expected empty agent list when no custom agents configured");

            session.close();
        }
    }

    /**
     * Verifies that session history can be compacted after messages.
     *
     * @see Snapshot:
     *      agent_and_compact_rpc/should_compact_session_history_after_messages
     */
    @Test
    @Disabled("Requires CLI with session.compaction.compact support (see upstream commit 9d998fb)")
    void testShouldCompactSessionHistoryAfterMessages(TestInfo testInfo) throws Exception {
        ctx.configureForTest("agent_and_compact_rpc", "should_compact_session_history_after_messages");

        try (CopilotClient client = ctx.createClient()) {
            CopilotSession session = client
                    .createSession(new SessionConfig().setOnPermissionRequest(PermissionHandler.APPROVE_ALL)).get();

            // Send a message to create some history
            session.sendAndWait(new MessageOptions().setPrompt("What is 2+2?")).get(60, TimeUnit.SECONDS);

            // Compact the session
            var result = session.getCompaction().compact().get(30, TimeUnit.SECONDS);
            assertNotNull(result);

            session.close();
        }
    }
}
