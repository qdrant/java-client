package io.qdrant.client.container;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

public class QdrantContainer extends GenericContainer<QdrantContainer> {

    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("qdrant/qdrant");
    private static final String DEFAULT_TAG = System.getProperty("qdrantVersion");
    private static final int GRPC_PORT = 6334;
    private static final int HTTP_PORT = 6333;

    public QdrantContainer() {
        this(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));
    }

    public QdrantContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    public QdrantContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);

        addExposedPorts(GRPC_PORT, HTTP_PORT);

        setWaitStrategy(new LogMessageWaitStrategy().withRegEx(".*Actix runtime found; starting in Actix runtime.*"));
    }

    public QdrantContainer withApiKey(String apiKey) {
        return withEnv("QDRANT__SERVICE__API_KEY", apiKey);
    }

    public String getGrpcHostAddress() {
        return getHost() + ":" + getMappedPort(GRPC_PORT);
    }

    public String getHttpHostAddress() {
        return getHost() + ":" + getMappedPort(HTTP_PORT);
    }
}
