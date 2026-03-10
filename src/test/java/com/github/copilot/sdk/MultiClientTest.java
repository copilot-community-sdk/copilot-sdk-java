/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *--------------------------------------------------------------------------------------------*/

package com.github.copilot.sdk;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.copilot.sdk.events.ExternalToolCompletedEvent;
import com.github.copilot.sdk.events.ExternalToolRequestedEvent;
import com.github.copilot.sdk.json.CopilotClientOptions;
import com.github.copilot.sdk.json.MessageOptions;
import com.github.copilot.sdk.json.PermissionHandler;
import com.github.copilot.sdk.json.ResumeSessionConfig;
import com.github.copilot.sdk.json.SessionConfig;
import com.github.copilot.sdk.json.ToolDefinition;
import com.github.copilot.sdk.json.ToolResultObject;

/**
 * Tests for multi-client scenarios using protocol v3 broadcast events.
 *
 * <p>
 * These tests verify that tool calls and completion events are broadcast to all
 * connected clients, enabling multi-client session sharing. Each test uses a
 * primary TCP client (client1) and a secondary client (client2) that connects
 * to the same CLI server instance.
 * </p>
 */
public class MultiClientTest {

    private static E2ETestContext ctx;
    private static CopilotClient client1;

    @BeforeAll
    static void setup() throws Exception {
        ctx = E2ETestContext.create();
        client1 = ctx.createClient(false); // TCP mode so client2 can connect
        // Trigger initial connection to read the port
        var initSession = client1
                .createSession(new SessionConfig().setOnPermissionRequest(PermissionHandler.APPROVE_ALL)).get();
        initSession.close();
    }

    @AfterAll
    static void teardown() throws Exception {
        if (client1 != null) {
            client1.forceStop().get();
        }
        if (ctx != null) {
            ctx.close();
        }
    }

    private CopilotClient createClient2() {
        Integer port = client1.getActualPort();
        assertNotNull(port, "Client1 must be using TCP mode");
        return new CopilotClient(new CopilotClientOptions().setCliUrl("localhost:" + port));
    }

    @Test
    void bothClientsSeeToolRequestAndCompletionEvents() throws Exception {
        ctx.configureForTest("multi_client", "both_clients_see_tool_request_and_completion_events");

        var tool = ToolDefinition.create("magic_number", "Returns a magic number",
                java.util.Map.of("type", "object", "properties",
                        java.util.Map.of("seed", java.util.Map.of("type", "string")), "required",
                        java.util.List.of("seed")),
                invocation -> {
                    String seed = invocation.getArguments().get("seed").toString();
                    return CompletableFuture.completedFuture(ToolResultObject.success("MAGIC_" + seed + "_42"));
                });

        var session1 = client1.createSession(new SessionConfig().setOnPermissionRequest(PermissionHandler.APPROVE_ALL)
                .setTools(java.util.List.of(tool))).get();

        try (var client2 = createClient2()) {
            var session2 = client2.resumeSession(session1.getSessionId(),
                    new ResumeSessionConfig().setOnPermissionRequest(PermissionHandler.APPROVE_ALL)).get();

            try {
                var client1Requested = new CompletableFuture<Boolean>();
                var client2Requested = new CompletableFuture<Boolean>();
                var client1Completed = new CompletableFuture<Boolean>();
                var client2Completed = new CompletableFuture<Boolean>();

                session1.on(evt -> {
                    if (evt instanceof ExternalToolRequestedEvent)
                        client1Requested.complete(true);
                    if (evt instanceof ExternalToolCompletedEvent)
                        client1Completed.complete(true);
                });
                session2.on(evt -> {
                    if (evt instanceof ExternalToolRequestedEvent)
                        client2Requested.complete(true);
                    if (evt instanceof ExternalToolCompletedEvent)
                        client2Completed.complete(true);
                });

                var response = session1
                        .sendAndWait(new MessageOptions()
                                .setPrompt("Use the magic_number tool with seed 'hello' and tell me the result"))
                        .get(30, TimeUnit.SECONDS);

                assertNotNull(response);
                assertTrue(response.getData().content().contains("MAGIC_hello_42"),
                        "Expected response to contain MAGIC_hello_42, got: " + response.getData().content());

                // Wait for broadcast events to arrive on both clients
                CompletableFuture.allOf(client1Requested, client2Requested, client1Completed, client2Completed).get(10,
                        TimeUnit.SECONDS);
            } finally {
                session2.close();
            }
        } finally {
            session1.close();
        }
    }

    @Test
    void twoClientsRegisterDifferentToolsAndAgentUsesBoth() throws Exception {
        ctx.configureForTest("multi_client", "two_clients_register_different_tools_and_agent_uses_both");

        var toolA = ToolDefinition.create("city_lookup", "Returns a city name for a given country code",
                java.util.Map.of("type", "object", "properties",
                        java.util.Map.of("countryCode", java.util.Map.of("type", "string")), "required",
                        java.util.List.of("countryCode")),
                invocation -> {
                    String code = invocation.getArguments().get("countryCode").toString();
                    return CompletableFuture.completedFuture(ToolResultObject.success("CITY_FOR_" + code));
                });

        var toolB = ToolDefinition.create("currency_lookup", "Returns a currency for a given country code",
                java.util.Map.of("type", "object", "properties",
                        java.util.Map.of("countryCode", java.util.Map.of("type", "string")), "required",
                        java.util.List.of("countryCode")),
                invocation -> {
                    String code = invocation.getArguments().get("countryCode").toString();
                    return CompletableFuture.completedFuture(ToolResultObject.success("CURRENCY_FOR_" + code));
                });

        var session1 = client1.createSession(new SessionConfig().setOnPermissionRequest(PermissionHandler.APPROVE_ALL)
                .setTools(java.util.List.of(toolA))).get();

        try (var client2 = createClient2()) {
            var session2 = client2
                    .resumeSession(session1.getSessionId(), new ResumeSessionConfig()
                            .setOnPermissionRequest(PermissionHandler.APPROVE_ALL).setTools(java.util.List.of(toolB)))
                    .get();

            try {
                var response1 = session1
                        .sendAndWait(new MessageOptions()
                                .setPrompt("Use the city_lookup tool with countryCode 'US' and tell me the result."))
                        .get(30, TimeUnit.SECONDS);
                assertNotNull(response1);
                assertTrue(response1.getData().content().contains("CITY_FOR_US"),
                        "Expected CITY_FOR_US in: " + response1.getData().content());

                var response2 = session1
                        .sendAndWait(new MessageOptions().setPrompt(
                                "Now use the currency_lookup tool with countryCode 'US' and tell me the result."))
                        .get(30, TimeUnit.SECONDS);
                assertNotNull(response2);
                assertTrue(response2.getData().content().contains("CURRENCY_FOR_US"),
                        "Expected CURRENCY_FOR_US in: " + response2.getData().content());
            } finally {
                session2.close();
            }
        } finally {
            session1.close();
        }
    }
}
