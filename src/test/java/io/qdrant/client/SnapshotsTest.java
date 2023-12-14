package io.qdrant.client;

import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.SnapshotsService;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import io.qdrant.client.container.QdrantContainer;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static io.qdrant.client.grpc.SnapshotsService.SnapshotDescription;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class SnapshotsTest {
	@Container
	private static final QdrantContainer QDRANT_CONTAINER = new QdrantContainer();
	private QdrantClient client;
	private ManagedChannel channel;
	private String testName;

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
			List<SnapshotDescription> snapshots = client.listSnapshotAsync(collectionName).get();
			for (SnapshotDescription snapshot : snapshots) {
				client.deleteSnapshotAsync(collectionName, snapshot.getName()).get();
			}
			client.deleteCollectionAsync(collectionName).get();
		}

		List<SnapshotDescription> snapshots = client.listFullSnapshotAsync().get();
		for (SnapshotDescription snapshot : snapshots) {
			client.deleteFullSnapshotAsync(snapshot.getName()).get();
		}

		client.close();
		channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
	}

	@Test
	public void createSnapshot() throws ExecutionException, InterruptedException {
		createCollection(testName);
		client.createSnapshotAsync(testName).get();
	}

	@Test
	public void deleteSnapshot() throws ExecutionException, InterruptedException {
		createCollection(testName);
		SnapshotDescription snapshotDescription = client.createSnapshotAsync(testName).get();
		client.deleteSnapshotAsync(testName, snapshotDescription.getName()).get();
	}

	@Test
	public void deleteSnapshot_with_missing_snapshot() {
		ExecutionException exception =
			assertThrows(ExecutionException.class, () -> client.deleteSnapshotAsync(testName, "snapshot_1").get());
		Throwable cause = exception.getCause();
		assertEquals(StatusRuntimeException.class, cause.getClass());
		StatusRuntimeException underlyingException = (StatusRuntimeException) cause;
		assertEquals(Status.Code.NOT_FOUND, underlyingException.getStatus().getCode());
	}

	@Test
	public void listSnapshots() throws ExecutionException, InterruptedException {
		createCollection(testName);
		client.createSnapshotAsync(testName).get();
		// snapshots are timestamped named to second precision. Wait more than 1 second to ensure we get 2 snapshots
		Thread.sleep(2000);
		client.createSnapshotAsync(testName).get();

		List<SnapshotDescription> snapshotDescriptions = client.listSnapshotAsync(testName).get();
		assertEquals(2, snapshotDescriptions.size());
	}

	@Test
	public void createFullSnapshot() throws ExecutionException, InterruptedException {
		createCollection(testName);
		createCollection(testName + "2");
		client.createFullSnapshotAsync().get();
	}

	@Test
	public void deleteFullSnapshot() throws ExecutionException, InterruptedException {
		createCollection(testName);
		createCollection(testName + "2");
		SnapshotDescription snapshotDescription = client.createFullSnapshotAsync().get();
		client.deleteFullSnapshotAsync(snapshotDescription.getName()).get();
	}

	@Test
	public void deleteFullSnapshot_with_missing_snapshot() {
		ExecutionException exception =
			assertThrows(ExecutionException.class, () -> client.deleteFullSnapshotAsync("snapshot_1").get());
		Throwable cause = exception.getCause();
		assertEquals(StatusRuntimeException.class, cause.getClass());
		StatusRuntimeException underlyingException = (StatusRuntimeException) cause;
		assertEquals(Status.Code.NOT_FOUND, underlyingException.getStatus().getCode());
	}

	@Test
	public void listFullSnapshots() throws ExecutionException, InterruptedException {
		createCollection(testName);
		createCollection(testName + 2);
		client.createFullSnapshotAsync().get();
		// snapshots are timestamped named to second precision. Wait more than 1 second to ensure we get 2 snapshots
		Thread.sleep(2000);
		client.createFullSnapshotAsync().get();

		List<SnapshotDescription> snapshotDescriptions = client.listFullSnapshotAsync().get();
		assertEquals(2, snapshotDescriptions.size());
	}

	@Test
	public void testDownloadSnapshot() throws ExecutionException, InterruptedException, IOException {
		URL restApiUrl = new URL("http://" + QDRANT_CONTAINER.getHttpHostAddress());
		createCollection(testName);

		assertEquals(client.listSnapshotAsync(testName).get().size(), 0);

		// Test with snapshot name
		SnapshotsService.SnapshotDescription response = client.createSnapshotAsync(testName).get();
		String snapshotName = response.getName();

		Path path = FileSystems.getDefault().getPath("./test.snapshot");

		client.downloadSnapshot(path, testName, snapshotName, restApiUrl);
		assertTrue(path.toFile().exists());

		// Test without snapshot name
		path = FileSystems.getDefault().getPath("./test_2.snapshot");
		client.downloadSnapshot(path, testName, null, restApiUrl);
		assertTrue(path.toFile().exists());
	}

	private void createCollection(String collectionName) throws ExecutionException, InterruptedException {
		Collections.CreateCollection request = Collections.CreateCollection.newBuilder()
			.setCollectionName(collectionName)
			.setVectorsConfig(Collections.VectorsConfig.newBuilder()
				.setParams(Collections.VectorParams.newBuilder()
					.setDistance(Collections.Distance.Cosine)
					.setSize(4)
					.build())
				.build())
			.build();

		client.createCollectionAsync(request).get();
	}
}
