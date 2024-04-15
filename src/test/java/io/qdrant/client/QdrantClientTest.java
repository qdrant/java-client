package io.qdrant.client;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.qdrant.QdrantContainer;

@Testcontainers
class QdrantClientTest {

	@Container
	private static final QdrantContainer QDRANT_CONTAINER = new QdrantContainer(DockerImage.QDRANT_IMAGE);
	private QdrantClient client;

	@BeforeEach
	public void setup() {
		ManagedChannel channel = Grpc.newChannelBuilder(
				QDRANT_CONTAINER.getGrpcHostAddress(),
				InsecureChannelCredentials.create())
			.build();
		QdrantGrpcClient grpcClient = QdrantGrpcClient.newBuilder(channel, true).build();
		client = new QdrantClient(grpcClient);
	}

	@AfterEach
	public void teardown() {
		client.close();
	}

	@Test
	void canAccessChannelOnGrpcClient() {
		Assertions.assertTrue(client.grpcClient().channel().authority().startsWith("localhost"));
	}
}
