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

import com.cometbid.sample.it.test.wiremock.base.WireMockInitializer;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.springframework.boot.test.context.SpringBootTest.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.cometbid.sample.test.SpringBootIntegrationTestsWiremock;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 *
 * @author samueladebowale
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = { SpringBootIntegrationTestsWiremock.class })
@ContextConfiguration(initializers = {WireMockInitializer.class})
class TodoControllerIT {

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private WebTestClient webTestClient;

    @LocalServerPort
    private Integer port;

    @AfterEach
    public void afterEach() {
        this.wireMockServer.resetAll();
    }

    @Test
    void testGetAllTodosShouldReturnDataFromClient() {
        this.wireMockServer.stubFor(
                WireMock.get("/todos")
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                        .withBody(
                                                "[{\"userId\": 1,\"id\": 1,\"title\": \"Learn Spring Boot 3.0\", \"completed\": false},"
                                                + "{\"userId\": 1,\"id\": 2,\"title\": \"Learn WireMock\", \"completed\": true}]")));

        this.webTestClient
                .get()
                .uri("/api/todos")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$[0].title")
                .isEqualTo("Learn Spring Boot 3.0")
                .jsonPath("$.length()")
                .isEqualTo(2);
    }

    @Test
    void testGetAllTodosShouldPropagateErrorMessageFromClient() {
        this.wireMockServer.stubFor(
                WireMock.get("/todos")
                        .willReturn(aResponse()
                                .withStatus(403)
                                .withFixedDelay(2000)) // milliseconds
        );

        this.webTestClient
                .get()
                .uri("http://localhost:" + port + "/api/todos")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }
}
