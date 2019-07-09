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

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.http.MediaType;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Date;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableReactiveMongoRepositories
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

class ReactiveMongoConfig extends AbstractReactiveMongoConfiguration {
    @Value("${spring.data.mongodb.uri}")
    private String mongoDbUri;

    @Override
    public MongoClient reactiveMongoClient() {
        return MongoClients.create(mongoDbUri);
    }

    @Override
    protected String getDatabaseName() {
        return "reactive-rabbitmq-demo-consumer";
    }
}

@RestController
@RequiredArgsConstructor
class ConsumerController {
    private final SuperheroDocumentRepository repo;

    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    Mono<String> getSuperheroes() {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "received"))
                .map(this::toString)
                .collect(Collectors.joining("\n"));
    }

    private String toString(SuperheroDocument doc) {
        return doc.getName() + " (" + doc.getPower() + ")";
    }
}

interface SinkWithErrorChannel extends Sink {
    String ERROR = "error";

    @Input(ERROR)
    SubscribableChannel error();
}

@Slf4j
@EnableBinding(SinkWithErrorChannel.class)
@RequiredArgsConstructor
class SuperheroListener {
    private final SuperheroDocumentRepository repo;

    @StreamListener(SinkWithErrorChannel.INPUT)
    public void onSuperhero(Superhero superhero) throws IOException {
        if (isEvil(superhero)) {
            throw new IOException("This is not a real superhero: " + superhero.getName());
        }
        log.info("Received superhero: {}", superhero);
        repo.save(toDocument(superhero)).block();
    }

    private static SuperheroDocument toDocument(Superhero s) {
        final SuperheroDocument doc = new SuperheroDocument();
        doc.setName(s.getName());
        doc.setPower(s.getPower());
        doc.setReceived(new Date());
        return doc;
    }

    private static boolean isEvil(Superhero s) {
        return s.getName().startsWith("Darth ");
    }
}

@Slf4j
@EnableBinding(SinkWithErrorChannel.class)
class SuperheroErrorListener {
    @StreamListener(SinkWithErrorChannel.ERROR)
    public void onEvilSuperhero(Superhero superhero) {
        log.warn("Received EVIL superhero: {}", superhero);
    }
}

@Data
class Superhero {
    private final String name;
    private final String power;
}

@Data
@Document
class SuperheroDocument {
    @Id
    private String id;
    private String name;
    private String power;
    private Date received;
}

interface SuperheroDocumentRepository extends ReactiveSortingRepository<SuperheroDocument, String> {
}
