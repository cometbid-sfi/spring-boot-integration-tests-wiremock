/*
 * The MIT License
 *
 * Copyright 2024 samueladebowale.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.cometbid.sample.it.test.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.util.List;
import org.cometbid.sample.test.SpringBootIntegrationTestsWiremock;
import org.cometbid.sample.test.configuration.WebClientConfig;
import org.cometbid.sample.test.controller.TodoController;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 *
 * @author samueladebowale
 */
//@ContextConfiguration(classes=SpringBootIntegrationTestsWiremock.class, loader=AnnotationConfigContextLoader.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = { SpringBootIntegrationTestsWiremock.class })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@TestPropertySource(locations = "classpath:application-integrationtest.properties")
class ManualSetupIT {

    @Autowired
    private WebTestClient webTestClient;

    private static WireMockServer wireMockServer;

    @DynamicPropertySource
    static void overrideWebClientBaseUrl(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("todo_base_url", wireMockServer::baseUrl);
    }

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void clearWireMock() {
        System.out.println("Stored stubbings: " + wireMockServer.getStubMappings().size());
        wireMockServer.resetAll();
        System.out.println("Stored stubbings after reset: " + wireMockServer.getStubMappings().size());
    }

    @Test
    void testWireMock() {
        System.out.println(wireMockServer.baseUrl());
        assertTrue(wireMockServer.isRunning());
    }

    @Test
    @Order(1)
    void basicWireMockExample() {
        wireMockServer.stubFor(
                WireMock.get("/todos")
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                        .withBody("[]")));

        this.webTestClient
                .get()
                .uri("/api/todos")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.length()")
                .isEqualTo(0);
    }

    @Test
    @Order(2)
    void wireMockRequestMatching() {
        wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/users"))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                        .withBody("[]")));

        this.webTestClient.get().uri("/api/todos").exchange().expectStatus().is5xxServerError();
    }

    @Test
    void wireMockRequestMatchingPriority() {
        wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/todos"))
                        .atPriority(1)
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                        .withBody("[]")));

        wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/todos"))
                        .atPriority(10)
                        .willReturn(aResponse().withStatus(500)));

        this.webTestClient
                .get()
                .uri("/api/todos")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.length()")
                .isEqualTo(0);
    }

    @Test
    void wireMockRequestMatchingWithData() {
        wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/todos"))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("todo-api/response-200.json")
                                        .withFixedDelay(1_000)));

        this.webTestClient
                .get()
                .uri("/api/todos")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.length()")
                .isEqualTo(3)
                .jsonPath("$[0].title")
                .isEqualTo("delectus aut autem");

        wireMockServer.verify(
                exactly(1),
                getRequestedFor(urlEqualTo("/todos"))
                        .withHeader("Accept", equalTo("application/json"))
                        .withHeader("X-Auth", equalTo("duke")));

        List<ServeEvent> events = wireMockServer.getAllServeEvents();

        events.get(0).getRequest().containsHeader("X-Auth");

        List<LoggedRequest> allUnmatchedRequests = wireMockServer.findAllUnmatchedRequests();

        assertEquals(0, allUnmatchedRequests.size());
    }
}
