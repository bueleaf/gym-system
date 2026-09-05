package com.example.integration.cucumber;

import org.slf4j.LoggerFactory;
import org.testcontainers.activemq.ActiveMQContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;

public class MessagingEnvironment
{
    private static final Network network = Network.newNetwork();

    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withNetwork(network)
                    .withNetworkAliases("postgres")
                    .withDatabaseName("gym_db")
                    .withUsername("gym_user")
                    .withPassword("gym_password");

    private static final MongoDBContainer mongoDb =
            new MongoDBContainer("mongo:7")
                    .withNetwork(network)
                    .withNetworkAliases("mongo");

    private static final ActiveMQContainer activeMQ =
            new ActiveMQContainer("apache/activemq-classic:5.18.7")
                    .withNetwork(network)
                    .withNetworkAliases("activemq");

    private static final GenericContainer<?> gymContainer =
            new GenericContainer<>(
                    new ImageFromDockerfile("gym:1", false)
                            .withFileFromPath(
                                    ".",
                                    Path.of("../gym")
                            )
            )
                    .withNetwork(network)
                    .withNetworkAliases("gym")
                    .withExposedPorts(8080)
                    .withEnv(
                            "SPRING_DATASOURCE_URL",
                            "jdbc:postgresql://postgres:5432/gym_db"
                    )
                    .withEnv(
                            "SPRING_DATASOURCE_USERNAME",
                            "gym_user"
                    )
                    .withEnv(
                            "SPRING_DATASOURCE_PASSWORD",
                            "gym_password"
                    )
                    .withEnv(
                            "SPRING_JPA_HIBERNATE_DDL_AUTO",
                            "create-drop"
                    )
                    .withEnv(
                            "SPRING_SQL_INIT_MODE",
                            "always"
                    )
                    .withEnv(
                            "SPRING_JPA_DEFER_DATASOURCE_INITIALIZATION",
                            "true"
                    )
                    .withEnv(
                            "SPRING_ACTIVEMQ_BROKER_URL",
                            "tcp://activemq:61616"
                    )
                    .withEnv(
                            "MESSAGING_TRAINER_WORKLOAD_QUEUE",
                            "trainer-workload"
                    )
                    .waitingFor(
                            Wait.forHttp("/actuator/health")
                                    .forStatusCode(200)
                    );

    private static final GenericContainer<?> aggregatorContainer =
            new GenericContainer<>(
                    new ImageFromDockerfile("aggregator:1", false)
                            .withFileFromPath(
                                    ".",
                                    Path.of("../training-aggregator")
                            )
            )
                    .withNetwork(network)
                    .withNetworkAliases("aggregator")
                    .withExposedPorts(8081)
                    .withEnv(
                            "SPRING_DATA_MONGODB_URI",
                            "mongodb://mongo:27017/gym_db"
                    )
                    .withEnv(
                            "SPRING_ACTIVEMQ_BROKER_URL",
                            "tcp://activemq:61616"
                    )
                    .withEnv(
                            "MESSAGING_TRAINER_WORKLOAD_QUEUE",
                            "trainer-workload"
                    )
                    .waitingFor(
                            Wait.forListeningPort()
                    );

    public static void start()
    {
        postgres.start();
        mongoDb.start();
        activeMQ.start();

        gymContainer.start();
        aggregatorContainer.start();
    }

    public static void stop()
    {
        aggregatorContainer.stop();
        gymContainer.stop();

        postgres.stop();
        mongoDb.stop();
        activeMQ.stop();

        network.close();
    }

    public static String gymUrl()
    {
        return "http://" + gymContainer.getHost()
                + ":" + gymContainer.getMappedPort(8080);
    }

    public static String aggregatorUrl()
    {
        return "http://" + aggregatorContainer.getHost()
                + ":" + aggregatorContainer.getMappedPort(8081);
    }
}
