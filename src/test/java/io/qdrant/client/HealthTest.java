package io.qdrant.client;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.qdrant.QdrantContainer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static io.qdrant.client.grpc.QdrantOuterClass.HealthCheckReply;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
class HealthTest {
	@Container
	private static final QdrantContainer QDRANT_CONTAINER = new QdrantContainer(DockerImage.QDRANT_IMAGE);
	private QdrantClient client;
	private ManagedChannel channel;

	@BeforeEach
	public void setup() {
		channel = Grpc.newChannelBuilder(
				QDRANT_CONTAINER.getGrpcHostAddress(),
				InsecureChannelCredentials.create())
			.build();
		QdrantGrpcClient grpcClient = QdrantGrpcClient.newBuilder(channel).build();
		client = new QdrantClient(grpcClient);
	}

	@AfterEach
	public void teardown() throws Exception {
		client.close();
		channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
	}

	@Test
	public void healthCheck() throws ExecutionException, InterruptedException {
		HealthCheckReply healthCheckReply = client.healthCheckAsync().get();
		assertNotNull(healthCheckReply.getTitle());
		assertNotNull(healthCheckReply.getVersion());
	}
}
