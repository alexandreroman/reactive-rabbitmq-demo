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

import com.github.javafaker.Faker;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.MediaType;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Random;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
@Slf4j
@RequiredArgsConstructor
@EnableBinding(Source.class)
class ProducerController {
    private final Source source;
    private final Faker faker = new Faker();
    private final Random random = new Random();

    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    Mono<String> sendMessage() {
        final com.github.javafaker.Superhero randomSuperhero = faker.superhero();
        final boolean evil = random.nextInt(10) == 0;
        final String superheroName = evil ? "Darth " + randomSuperhero.name() : randomSuperhero.name();
        final Superhero superhero = new Superhero(superheroName, randomSuperhero.power());
        source.output().send(MessageBuilder.withPayload(superhero).build());

        log.info("Message sent to broker: {}", superhero);

        return Mono.just("Sent message to broker: " + superhero);
    }
}

@Data
class Superhero {
    private final String name;
    private final String power;
}
