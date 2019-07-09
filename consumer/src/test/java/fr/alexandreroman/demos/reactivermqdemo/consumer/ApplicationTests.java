/*
 * Copyright (c) 2019 Pivotal Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.alexandreroman.demos.reactivermqdemo.consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ApplicationTests {
    @LocalServerPort
    private int webPort;
    private WebTestClient webClient;

    @Autowired
    private Source source;

    @Before
    public void beforeTest() {
        webClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + webPort).build();
    }

    @Test
    public void contextLoads() {
    }

    @Test
    public void testActuatorHealth() {
        webClient.get().uri("/actuator/health").exchange().expectStatus().isOk();
    }

    @Test
    public void testListener() {
        final Superhero superhero = new Superhero("John Doe", "Invisibility");
        source.output().send(MessageBuilder.withPayload(superhero).build());

        /*
        webClient.get().uri("/").exchange().expectStatus().isOk()
                .expectBody(String.class).isEqualTo("John Doe (Invisibility)");

         */
    }

    @SpringBootApplication
    @EnableBinding(Source.class)
    public static class MyProcessor {
    }
}
