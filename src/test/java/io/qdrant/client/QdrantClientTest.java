package io.qdrant.client;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Points.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.qdrant.QdrantContainer;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static io.qdrant.client.ConditionFactory.matchKeyword;
import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

import static io.qdrant.client.WithPayloadSelectorFactory.enable;
import static org.assertj.core.api.Assertions.assertThat;

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

	@Test
	void createListDeleteCollectionAsyncReturnsExpectedResponse() {
		// When
		CompletableFuture<Collections.CollectionOperationResponse> future = client.createCollectionAsync("test_collection",
			Collections.VectorParams.newBuilder().setDistance(Collections.Distance.Dot).setSize(4).build());

		// Then
		// creation
		assertThat(future).succeedsWithin(Duration.ofMillis(500));
		assertThat(future).isCompletedWithValueMatching(Collections.CollectionOperationResponse::getResult);

		// list
		client.listCollectionsAsync().thenCompose(collections -> {
			assertThat(collections).contains("test_collection");

			// delete
			return client.deleteCollectionAsync("test_collection");
		}).thenAccept(response -> {
			assertThat(response.getResult()).isTrue();
		});
	}

	@Test
	void upsertListDeleteVectorAsyncReturnsExpectedResponse() {
		// When
		CompletableFuture<Collections.CollectionOperationResponse> collectionFuture = client.createCollectionAsync("test_collection",
			Collections.VectorParams.newBuilder().setDistance(Collections.Distance.Dot).setSize(4).build());

		// Then
		assertThat(collectionFuture).succeedsWithin(Duration.ofMillis(500));
		assertThat(collectionFuture).isCompletedWithValueMatching(Collections.CollectionOperationResponse::getResult);

		// When
		CompletableFuture<UpdateResult> vectorFuture = client.upsertAsync("test_collection", List.of(
			PointStruct.newBuilder().setId(id(1))
				.setVectors(vectors(0.05f, 0.61f, 0.76f, 0.74f))
				.putAllPayload(Map.of("city", value("Berlin")))
				.build(),
			PointStruct.newBuilder()
				.setId(id(2))
				.setVectors(vectors(0.19f, 0.81f, 0.75f, 0.11f))
				.putAllPayload(Map.of("city", value("London")))
				.build(),
			PointStruct.newBuilder()
				.setId(id(3))
				.setVectors(vectors(0.36f, 0.55f, 0.47f, 0.94f))
				.putAllPayload(Map.of("city", value("Moscow")))
				.build()));

		// Then
		assertThat(vectorFuture).succeedsWithin(Duration.ofMillis(500));
		assertThat(vectorFuture).isCompletedWithValueMatching(response -> response.getStatus().equals(UpdateStatus.Completed));

		// When
		CompletableFuture<List<ScoredPoint>> searchFuture = client.searchAsync(SearchPoints.newBuilder()
			.setCollectionName("test_collection")
			.setLimit(3)
			.setFilter(Filter.newBuilder().addMust(matchKeyword("city", "London")))
			.addAllVector(List.of(0.2f, 0.1f, 0.9f, 0.7f))
			.setWithPayload(enable(true))
			.build());

		// Then
		assertThat(searchFuture).succeedsWithin(Duration.ofMillis(500));
		assertThat(searchFuture).isCompletedWithValueMatching(list -> list.size() == 1 && list.get(0).getId().getNum() == 2);

		// When
		CompletableFuture<UpdateResult> deleteFuture = client.deleteAsync("test_collection", List.of(id(1), id(2), id(3)));

		// Then
		assertThat(deleteFuture).succeedsWithin(Duration.ofMillis(500));
		assertThat(deleteFuture).isCompletedWithValueMatching(response -> response.getStatus().equals(UpdateStatus.Completed));
	}
}
