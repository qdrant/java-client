package io.qdrant.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.qdrant.QdrantContainer;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Collections.AliasDescription;
import io.qdrant.client.grpc.Collections.CollectionInfo;
import io.qdrant.client.grpc.Collections.CollectionStatus;
import io.qdrant.client.grpc.Collections.CreateCollection;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Collections.VectorsConfig;

@Testcontainers
class CollectionsTest {
	@Container
	private static final QdrantContainer QDRANT_CONTAINER = new QdrantContainer(DockerImage.QDRANT_IMAGE);
	private QdrantClient client;
	private ManagedChannel channel;
	private String testName;

	private static CreateCollection getCreateCollection(String collectionName) {
		return CreateCollection.newBuilder()
			.setCollectionName(collectionName)
			.setVectorsConfig(VectorsConfig.newBuilder()
				.setParams(VectorParams.newBuilder()
					.setDistance(Distance.Cosine)
					.setSize(4)
					.build())
				.build())
			.build();
	}

	@BeforeEach
	public void setup(TestInfo testInfo) {
		testName = testInfo.getDisplayName().replace("()", "");
		channel = Grpc.newChannelBuilder(
				QDRANT_CONTAINER.getGrpcHostAddress(),
				InsecureChannelCredentials.create())
			.build();
		QdrantGrpcClient grpcClient = QdrantGrpcClient.newBuilder(channel).build();
		client = new QdrantClient(grpcClient);
	}

	@AfterEach
	public void teardown() throws Exception {
		List<String> collectionNames = client.listCollectionsAsync().get();
		for (String collectionName : collectionNames) {
			client.deleteCollectionAsync(collectionName).get();
		}
		client.close();
		channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
	}

	@Test
	void createCollection() throws ExecutionException, InterruptedException {
		client.createCollectionAsync(
			testName,
			VectorParams.newBuilder()
				.setDistance(Distance.Cosine)
				.setSize(4)
				.build()).get();

		List<String> collections = client.listCollectionsAsync().get();
		assertTrue(collections.contains(testName));
	}

	@Test
	void createCollection_for_existing_collection() throws ExecutionException, InterruptedException {
		CreateCollection createCollection = getCreateCollection(testName);
		client.createCollectionAsync(createCollection).get();

		ExecutionException exception = assertThrows(ExecutionException.class, () ->
			client.createCollectionAsync(createCollection).get());
		assertEquals(StatusRuntimeException.class, exception.getCause().getClass());
	}

	@Test
	void createCollection_with_no_collection_name() {
		CreateCollection createCollection = CreateCollection.newBuilder()
			.setVectorsConfig(VectorsConfig.newBuilder()
				.setParams(VectorParams.newBuilder()
					.setDistance(Distance.Cosine)
					.setSize(4)
					.build())
				.build())
			.build();

		assertThrows(
			IllegalArgumentException.class,
			() -> client.createCollectionAsync(createCollection).get());
	}

	@Test
	public void recreateCollection() throws ExecutionException, InterruptedException {
		CreateCollection createCollection = getCreateCollection(testName);
		client.createCollectionAsync(createCollection).get();
		client.recreateCollectionAsync(createCollection).get();

		List<String> collections = client.listCollectionsAsync().get();
		assertTrue(collections.contains(testName));
	}

	@Test
	public void updateCollection() throws ExecutionException, InterruptedException {
		CreateCollection createCollection = getCreateCollection(testName);
		client.createCollectionAsync(createCollection).get();
		client.updateCollectionAsync(Collections.UpdateCollection.newBuilder()
			.setCollectionName(testName)
			.setVectorsConfig(Collections.VectorsConfigDiff.newBuilder()
				.setParams(Collections.VectorParamsDiff.newBuilder()
					.setOnDisk(false)
					.build())
				.build())
			.build()).get();
	}

	@Test
	public void updateCollection_with_no_changes() throws ExecutionException, InterruptedException {
		CreateCollection createCollection = getCreateCollection(testName);
		client.createCollectionAsync(createCollection).get();
		client.updateCollectionAsync(Collections.UpdateCollection.newBuilder()
			.setCollectionName(testName)
			.build()).get();
	}

	@Test
	public void deleteCollection() throws ExecutionException, InterruptedException {
		CreateCollection createCollection = getCreateCollection(testName);
		client.createCollectionAsync(createCollection).get();
		client.deleteCollectionAsync(testName).get();

		List<String> collections = client.listCollectionsAsync().get();
		assertTrue(collections.isEmpty());
	}

	@Test
	public void deleteCollection_with_missing_collection() {
		ExecutionException exception = assertThrows(ExecutionException.class, () -> client.deleteCollectionAsync(testName).get());
		Throwable cause = exception.getCause();
		assertEquals(QdrantException.class, cause.getClass());
	}

	@Test
	public void getCollectionInfo() throws ExecutionException, InterruptedException {
		CreateCollection createCollection = getCreateCollection(testName);
		client.createCollectionAsync(createCollection).get();
		CollectionInfo collectionInfo = client.getCollectionInfoAsync(testName).get();
		assertEquals(CollectionStatus.Green, collectionInfo.getStatus());
	}

	@Test
	public void getCollectionInfo_with_missing_collection() {
		ExecutionException exception = assertThrows(ExecutionException.class, () ->
			client.getCollectionInfoAsync(testName).get());
		Throwable cause = exception.getCause();
		assertEquals(StatusRuntimeException.class, cause.getClass());
		StatusRuntimeException underlyingException = (StatusRuntimeException) cause;
		assertEquals(Status.Code.NOT_FOUND, underlyingException.getStatus().getCode());
	}

	@Test
	public void collectionExists() throws ExecutionException, InterruptedException {
		assertFalse(client.collectionExistsAsync(testName).get());

		CreateCollection createCollection = getCreateCollection(testName);
		client.createCollectionAsync(createCollection).get();
		assertTrue(client.collectionExistsAsync(testName).get());

		client.deleteCollectionAsync(testName).get();
		assertFalse(client.collectionExistsAsync(testName).get());
	}

	@Test
	public void createAlias() throws ExecutionException, InterruptedException {
		CreateCollection createCollection = getCreateCollection(testName);
		client.createCollectionAsync(createCollection).get();
		client.createAliasAsync("alias_1", testName).get();
	}

	@Test
	public void listAliases() throws ExecutionException, InterruptedException {
		CreateCollection createCollection = getCreateCollection(testName);
		client.createCollectionAsync(createCollection).get();
		client.createAliasAsync("alias_1", testName).get();
		client.createAliasAsync("alias_2", testName).get();

		List<AliasDescription> aliasDescriptions = client.listAliasesAsync().get();

		List<AliasDescription> sorted = aliasDescriptions.stream()
			.sorted(Comparator.comparing(AliasDescription::getAliasName))
			.collect(Collectors.toList());

		assertEquals(2, sorted.size());
		assertEquals("alias_1", sorted.get(0).getAliasName());
		assertEquals(testName, sorted.get(0).getCollectionName());
		assertEquals("alias_2", sorted.get(1).getAliasName());
		assertEquals(testName, sorted.get(1).getCollectionName());
	}

	@Test
	public void listCollectionAliases() throws ExecutionException, InterruptedException {
		CreateCollection createCollection = getCreateCollection(testName);
		client.createCollectionAsync(createCollection).get();
		client.createAliasAsync("alias_1", testName).get();
		client.createAliasAsync("alias_2", testName).get();

		List<String> aliases = client.listCollectionAliasesAsync(testName).get();

		List<String> sorted = aliases.stream()
			.sorted()
			.collect(Collectors.toList());

		assertEquals(2, sorted.size());
		assertEquals("alias_1", sorted.get(0));
		assertEquals("alias_2", sorted.get(1));
	}

	@Test
	public void renameAlias() throws ExecutionException, InterruptedException {
		CreateCollection createCollection = getCreateCollection(testName);
		client.createCollectionAsync(createCollection).get();
		client.createAliasAsync("alias_1", testName).get();
		client.renameAliasAsync("alias_1", "alias_2").get();

		List<String> aliases = client.listCollectionAliasesAsync(testName).get();

		List<String> sorted = aliases.stream()
			.sorted()
			.collect(Collectors.toList());

		assertEquals(1, sorted.size());
		assertEquals("alias_2", sorted.get(0));
	}

	@Test
	public void deleteAlias() throws ExecutionException, InterruptedException {
		CreateCollection createCollection = getCreateCollection(testName);
		client.createCollectionAsync(createCollection).get();
		client.createAliasAsync("alias_1", testName).get();
		client.deleteAliasAsync("alias_1").get();

		List<String> aliases = client.listCollectionAliasesAsync(testName).get();
		assertTrue(aliases.isEmpty());
	}
}
