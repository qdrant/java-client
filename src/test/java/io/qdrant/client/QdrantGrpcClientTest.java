package io.qdrant.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.qdrant.QdrantContainer;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.qdrant.client.grpc.QdrantOuterClass;

@Testcontainers
class QdrantGrpcClientTest {

	@Container
	private static final QdrantContainer QDRANT_CONTAINER = new QdrantContainer(DockerImage.QDRANT_IMAGE);
	private QdrantGrpcClient client;

	@BeforeEach
	public void setup() {
		client = QdrantGrpcClient.newBuilder(
				Grpc.newChannelBuilder(
						QDRANT_CONTAINER.getGrpcHostAddress(),
						InsecureChannelCredentials.create())
					.build())
			.build();
	}

	@AfterEach
	public void teardown() {
		client.close();
	}

	@Test
	void healthCheck() throws ExecutionException, InterruptedException {
		QdrantOuterClass.HealthCheckReply healthCheckReply =
			client.qdrant().healthCheck(QdrantOuterClass.HealthCheckRequest.getDefaultInstance()).get();

		assertNotNull(healthCheckReply.getTitle());
		assertNotNull(healthCheckReply.getVersion());
	}
}