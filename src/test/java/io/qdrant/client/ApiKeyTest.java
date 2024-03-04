package io.qdrant.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.qdrant.QdrantContainer;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qdrant.client.grpc.QdrantOuterClass.HealthCheckReply;
import io.qdrant.client.grpc.QdrantOuterClass.HealthCheckRequest;

@Testcontainers
public class ApiKeyTest {
	@Container
	private static final QdrantContainer QDRANT_CONTAINER = new QdrantContainer(DockerImage.QDRANT_IMAGE).withEnv("QDRANT__SERVICE__API_KEY", "password!");
	private ManagedChannel channel;

	@BeforeEach
	public void setup() {
		channel = Grpc.newChannelBuilder(
				QDRANT_CONTAINER.getGrpcHostAddress(),
				InsecureChannelCredentials.create())
			.build();
	}

	@AfterEach
	public void teardown() throws InterruptedException {
		channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
	}

	@Test
	public void client_with_api_key_can_connect() throws ExecutionException, InterruptedException {
		HealthCheckReply healthCheckReply;
		try (QdrantGrpcClient grpcClient = QdrantGrpcClient.newBuilder(channel).withApiKey("password!").build()) {
			healthCheckReply = grpcClient.qdrant().healthCheck(HealthCheckRequest.getDefaultInstance()).get();
		}

		assertNotNull(healthCheckReply.getTitle());
		assertNotNull(healthCheckReply.getVersion());
	}

	@Test
	public void client_without_api_key_cannot_connect() {
		try (QdrantGrpcClient grpcClient = QdrantGrpcClient.newBuilder(channel).build()) {
			ExecutionException executionException = assertThrows(
				ExecutionException.class,
				() -> grpcClient.qdrant().healthCheck(HealthCheckRequest.getDefaultInstance()).get());
			Throwable cause = executionException.getCause();
			assertEquals(StatusRuntimeException.class, cause.getClass());
			StatusRuntimeException statusRuntimeException = (StatusRuntimeException) cause;
			assertEquals(Status.Code.PERMISSION_DENIED, statusRuntimeException.getStatus().getCode());
		}
	}
}
