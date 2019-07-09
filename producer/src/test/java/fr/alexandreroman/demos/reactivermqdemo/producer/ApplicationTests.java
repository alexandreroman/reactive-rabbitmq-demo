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

package fr.alexandreroman.demos.reactivermqdemo.producer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTests {
    @LocalServerPort
    private int webPort;
    private WebTestClient webClient;

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
    public void testSendMessage() {
        webClient.get().uri("/").exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(s -> s.startsWith("Sent message to broker: "));
    }
}
