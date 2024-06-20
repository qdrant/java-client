package io.qdrant.client;

import static io.qdrant.client.ConditionFactory.hasId;
import static io.qdrant.client.ConditionFactory.matchKeyword;
import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.TargetVectorFactory.targetVector;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorFactory.vector;
import static io.qdrant.client.VectorsFactory.vectors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Points;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.qdrant.QdrantContainer;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.qdrant.client.grpc.Collections.CollectionInfo;
import io.qdrant.client.grpc.Collections.CreateCollection;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.PayloadSchemaType;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Collections.VectorsConfig;
import io.qdrant.client.grpc.Points.BatchResult;
import io.qdrant.client.grpc.Points.DiscoverPoints;
import io.qdrant.client.grpc.Points.Filter;
import io.qdrant.client.grpc.Points.PointGroup;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.PointVectors;
import io.qdrant.client.grpc.Points.PointsIdsList;
import io.qdrant.client.grpc.Points.PointsSelector;
import io.qdrant.client.grpc.Points.PointsUpdateOperation;
import io.qdrant.client.grpc.Points.PointsUpdateOperation.ClearPayload;
import io.qdrant.client.grpc.Points.PointsUpdateOperation.UpdateVectors;
import io.qdrant.client.grpc.Points.RecommendPointGroups;
import io.qdrant.client.grpc.Points.RecommendPoints;
import io.qdrant.client.grpc.Points.RetrievedPoint;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.ScrollPoints;
import io.qdrant.client.grpc.Points.ScrollResponse;
import io.qdrant.client.grpc.Points.SearchPointGroups;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.Points.UpdateResult;
import io.qdrant.client.grpc.Points.UpdateStatus;
import io.qdrant.client.grpc.Points.Vectors;

@Testcontainers
class PointsTest {
	@Container
	private static final QdrantContainer QDRANT_CONTAINER = new QdrantContainer(DockerImage.QDRANT_IMAGE);
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
			client.deleteCollectionAsync(collectionName).get();
		}
		client.close();
		channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
	}

	@Test
	public void retrieve() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		List<RetrievedPoint> points = client.retrieveAsync(
			testName,
			id(9),
			null).get();

		assertEquals(1, points.size());
		RetrievedPoint point = points.get(0);
		assertEquals(id(9), point.getId());
		assertEquals(ImmutableSet.of("foo", "bar", "date"), point.getPayloadMap().keySet());
		assertEquals(value("goodbye"), point.getPayloadMap().get("foo"));
		assertEquals(value(2), point.getPayloadMap().get("bar"));
		assertEquals(Vectors.getDefaultInstance(), point.getVectors());
	}

	@Test
	public void retrieve_with_vector_without_payload() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		List<RetrievedPoint> points = client.retrieveAsync(
			testName,
			id(8),
			false,
			true,
			null).get();

		assertEquals(1, points.size());
		RetrievedPoint point = points.get(0);
		assertEquals(id(8), point.getId());
		assertTrue(point.getPayloadMap().isEmpty());
		assertEquals(Vectors.VectorsOptionsCase.VECTOR, point.getVectors().getVectorsOptionsCase());
	}

	@Test
	public void setPayload() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		client.setPayloadAsync(
			testName,
			ImmutableMap.of("bar", value("some bar")),
			id(9),
			null,
			null,
			null).get();

		List<RetrievedPoint> points =
			client.retrieveAsync(testName, id(9), null).get();

		assertEquals(1, points.size());
		RetrievedPoint point = points.get(0);
		assertEquals(id(9), point.getId());
		assertEquals(ImmutableSet.of("foo", "bar", "date"), point.getPayloadMap().keySet());
		assertEquals(value("some bar"), point.getPayloadMap().get("bar"));
		assertEquals(value("goodbye"), point.getPayloadMap().get("foo"));
	}

	@Test
	public void overwritePayload() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		client.overwritePayloadAsync(
			testName,
			ImmutableMap.of("bar", value("some bar")),
			id(9),
			null,
			null,
			null).get();

		List<RetrievedPoint> points =
			client.retrieveAsync(testName, id(9), null).get();

		assertEquals(1, points.size());
		RetrievedPoint point = points.get(0);
		assertEquals(id(9), point.getId());
		assertEquals(ImmutableSet.of("bar"), point.getPayloadMap().keySet());
		assertEquals(value("some bar"), point.getPayloadMap().get("bar"));
	}

	@Test
	public void deletePayload() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		client.setPayloadAsync(
			testName,
			ImmutableMap.of("bar", value("some bar")),
			id(9),
			null,
			null,
			null).get();

		client.deletePayloadAsync(testName, ImmutableList.of("foo"), id(9), null, null, null).get();

		List<RetrievedPoint> points =
			client.retrieveAsync(testName, id(9), null).get();

		assertEquals(1, points.size());
		RetrievedPoint point = points.get(0);
		assertEquals(id(9), point.getId());
		assertEquals(ImmutableSet.of("bar", "date"), point.getPayloadMap().keySet());
		assertEquals(value("some bar"), point.getPayloadMap().get("bar"));
	}

	@Test
	public void clearPayload() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		client.clearPayloadAsync(testName, id(9), true, null, null).get();

		List<RetrievedPoint> points =
			client.retrieveAsync(testName, id(9), null).get();

		assertEquals(1, points.size());
		RetrievedPoint point = points.get(0);
		assertEquals(id(9), point.getId());
		assertTrue(point.getPayloadMap().isEmpty());
	}

	@Test
	public void createFieldIndex() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		UpdateResult result = client.createPayloadIndexAsync(
			testName,
			"foo",
			PayloadSchemaType.Keyword,
			null,
			null,
			null,
			null).get();

		assertEquals(UpdateStatus.Completed, result.getStatus());

		result = client.createPayloadIndexAsync(
			testName,
			"bar",
			PayloadSchemaType.Integer,
			null,
			null,
			null,
			null).get();

		assertEquals(UpdateStatus.Completed, result.getStatus());

		CollectionInfo collectionInfo = client.getCollectionInfoAsync(testName).get();
		assertEquals(ImmutableSet.of("foo", "bar"), collectionInfo.getPayloadSchemaMap().keySet());
		assertEquals(PayloadSchemaType.Keyword, collectionInfo.getPayloadSchemaMap().get("foo").getDataType());
		assertEquals(PayloadSchemaType.Integer, collectionInfo.getPayloadSchemaMap().get("bar").getDataType());
	}

	@Test
	public void createDatetimeFieldIndex() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		UpdateResult result = client.createPayloadIndexAsync(
			testName,
			"date",
			PayloadSchemaType.Datetime,
			null,
			null,
			null,
			null).get();

		assertEquals(UpdateStatus.Completed, result.getStatus());

		CollectionInfo collectionInfo = client.getCollectionInfoAsync(testName).get();
		assertEquals(ImmutableSet.of("date"), collectionInfo.getPayloadSchemaMap().keySet());
		assertEquals(PayloadSchemaType.Datetime, collectionInfo.getPayloadSchemaMap().get("date").getDataType());
	}

	@Test
	public void deleteFieldIndex() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		UpdateResult result = client.createPayloadIndexAsync(
			testName,
			"foo",
			PayloadSchemaType.Keyword,
			null,
			null,
			null,
			null).get();
		assertEquals(UpdateStatus.Completed, result.getStatus());

		result = client.deletePayloadIndexAsync(
			testName,
			"foo",
			null,
			null,
			null).get();
		assertEquals(UpdateStatus.Completed, result.getStatus());
	}

	@Test
	public void search() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		List<ScoredPoint> points = client.searchAsync(
			SearchPoints.newBuilder()
				.setCollectionName(testName)
				.setWithPayload(WithPayloadSelectorFactory.enable(true))
				.addAllVector(ImmutableList.of(10.4f, 11.4f))
				.setLimit(1)
				.build()).get();

		assertEquals(1, points.size());
		ScoredPoint point = points.get(0);
		assertEquals(id(9), point.getId());
		assertEquals(ImmutableSet.of("foo", "bar", "date"), point.getPayloadMap().keySet());
		assertEquals(value("goodbye"), point.getPayloadMap().get("foo"));
		assertEquals(value(2), point.getPayloadMap().get("bar"));
		assertFalse(point.getVectors().hasVector());
	}

	@Test
	public void searchBatch() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		List<BatchResult> batchResults = client.searchBatchAsync(testName,
			ImmutableList.of(
				SearchPoints.newBuilder()
					.addAllVector(ImmutableList.of(10.4f, 11.4f))
					.setLimit(1)
					.build(),
				SearchPoints.newBuilder()
					.addAllVector(ImmutableList.of(3.4f, 4.4f))
					.setLimit(1)
					.build()
			), null).get();

		assertEquals(2, batchResults.size());
		BatchResult result = batchResults.get(0);
		assertEquals(1, result.getResultCount());
		assertEquals(id(9), result.getResult(0).getId());
		result = batchResults.get(1);
		assertEquals(1, result.getResultCount());
		assertEquals(id(8), result.getResult(0).getId());
	}

	@Test
	public void searchGroups() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		client.upsertAsync(
			testName,
			ImmutableList.of(
				PointStruct.newBuilder()
					.setId(id(10))
					.setVectors(VectorsFactory.vectors(30f, 31f))
					.putAllPayload(ImmutableMap.of("foo", value("hello")))
					.build()
			)
		).get();

		List<PointGroup> groups = client.searchGroupsAsync(SearchPointGroups.newBuilder()
			.setCollectionName(testName)
			.addAllVector(ImmutableList.of(10.4f, 11.4f))
			.setGroupBy("foo")
			.setGroupSize(2)
			.setLimit(10)
			.build()
		).get();

		assertEquals(2, groups.size());
		assertEquals(1, groups.stream().filter(g -> g.getHitsCount() == 2).count());
		assertEquals(1, groups.stream().filter(g -> g.getHitsCount() == 1).count());
	}

	@Test
	public void scroll() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		ScrollResponse scrollResponse = client.scrollAsync(ScrollPoints.newBuilder()
			.setCollectionName(testName)
			.setLimit(1)
			.build()
		).get();

		assertEquals(1, scrollResponse.getResultCount());
		assertTrue(scrollResponse.hasNextPageOffset());

		scrollResponse = client.scrollAsync(ScrollPoints.newBuilder()
			.setCollectionName(testName)
			.setLimit(1)
			.setOffset(scrollResponse.getNextPageOffset())
			.build()
		).get();

		assertEquals(1, scrollResponse.getResultCount());
		assertFalse(scrollResponse.hasNextPageOffset());

		scrollResponse = client.scrollAsync(ScrollPoints.newBuilder()
			.setCollectionName(testName)
			.setLimit(1)
			.setOrderBy(Points.OrderBy.newBuilder()
				.setDirection(Points.Direction.Desc)
				.setKey("bar").build())
			.build()
		).get();

		assertEquals(1, scrollResponse.getResultCount());
		assertFalse(scrollResponse.hasNextPageOffset());
	}

	@Test
	public void recommend() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		List<ScoredPoint> points = client.recommendAsync(RecommendPoints.newBuilder()
			.setCollectionName(testName)
			.addPositive(id(8))
			.setLimit(1)
			.build()
		).get();

		assertEquals(1, points.size());
		assertEquals(id(9), points.get(0).getId());
	}

	@Test
	public void recommendBatch() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		List<BatchResult> batchResults = client.recommendBatchAsync(testName,
			ImmutableList.of(
				RecommendPoints.newBuilder()
					.setCollectionName(testName)
					.addPositive(id(8))
					.setLimit(1)
					.build(),
				RecommendPoints.newBuilder()
					.setCollectionName(testName)
					.addPositive(id(9))
					.setLimit(1)
					.build()
			),
			null
		).get();

		assertEquals(2, batchResults.size());
		BatchResult result = batchResults.get(0);
		assertEquals(1, result.getResultCount());
		assertEquals(id(9), result.getResult(0).getId());
		result = batchResults.get(1);
		assertEquals(1, result.getResultCount());
		assertEquals(id(8), result.getResult(0).getId());
	}

	@Test
	public void recommendGroups() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		client.upsertAsync(
			testName,
			ImmutableList.of(
				PointStruct.newBuilder()
					.setId(id(10))
					.setVectors(VectorsFactory.vectors(30f, 31f))
					.putAllPayload(ImmutableMap.of("foo", value("hello")))
					.build()
			)
		).get();

		List<PointGroup> groups = client.recommendGroupsAsync(RecommendPointGroups.newBuilder()
			.setCollectionName(testName)
			.setGroupBy("foo")
			.addPositive(id(9))
			.setGroupSize(2)
			.setLimit(10)
			.build()
		).get();

		assertEquals(1, groups.size());
		assertEquals(2, groups.get(0).getHitsCount());
	}

	@Test
	public void discover() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		List<ScoredPoint> points = client.discoverAsync(DiscoverPoints.newBuilder()
			.setCollectionName(testName)
			.setTarget(targetVector(vector(ImmutableList.of(10.4f, 11.4f))))
			.setLimit(1)
			.build()).get();

		assertEquals(1, points.size());
		assertEquals(id(9), points.get(0).getId());
	}

	@Test
	public void discoverBatch() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		List<BatchResult> batchResults = client.discoverBatchAsync(
			testName,
			ImmutableList.of(
				DiscoverPoints.newBuilder()
					.setCollectionName(testName)
					.setTarget(targetVector(vector(ImmutableList.of(10.4f, 11.4f))))
					.setLimit(1)
					.build(),
				DiscoverPoints.newBuilder()
					.setCollectionName(testName)
					.setTarget(targetVector(vector(ImmutableList.of(3.5f, 4.5f))))
					.setLimit(1)
					.build()),
			null).get();

		assertEquals(2, batchResults.size());
		BatchResult result = batchResults.get(0);
		assertEquals(1, result.getResultCount());
		assertEquals(id(9), result.getResult(0).getId());
		result = batchResults.get(1);
		assertEquals(1, result.getResultCount());
		assertEquals(id(8), result.getResult(0).getId());
	}

	@Test
	public void count() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);
		Long count = client.countAsync(testName).get();
		assertEquals(2, count);
	}

	@Test
	public void count_with_filter() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);
		Long count = client.countAsync(
			testName,
			Filter.newBuilder()
				.addMust(hasId(id(9)))
				.addMust(matchKeyword("foo", "goodbye"))
				.build(),
			null
		).get();
		assertEquals(1, count);
	}

	@Test
	public void delete_by_id() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		List<RetrievedPoint> points = client.retrieveAsync(
			testName,
			id(8),
			false,
			false,
			null).get();

		assertEquals(1, points.size());

		client.deleteAsync(testName, ImmutableList.of(id(8))).get();

		points = client.retrieveAsync(
			testName,
			id(8),
			false,
			false,
			null).get();

		assertEquals(0, points.size());
	}

	@Test
	public void delete_by_filter() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		List<RetrievedPoint> points = client.retrieveAsync(
			testName,
			id(8),
			false,
			false,
			null).get();

		assertEquals(1, points.size());

		client.deleteAsync(
			testName,
			Filter.newBuilder().addMust(matchKeyword("foo", "hello")).build()).get();

		points = client.retrieveAsync(
			testName,
			id(8),
			false,
			false,
			null).get();

		assertEquals(0, points.size());
	}

	@Test
	public void batchPointUpdate() throws ExecutionException, InterruptedException {
		createAndSeedCollection(testName);

		List<PointsUpdateOperation> operations = List.of(
			PointsUpdateOperation.newBuilder()
				.setClearPayload(ClearPayload.newBuilder().setPoints(
						PointsSelector.newBuilder().setPoints(PointsIdsList.newBuilder().addIds(id(9))))
					.build())
				.build(),
			PointsUpdateOperation.newBuilder()
				.setUpdateVectors(UpdateVectors.newBuilder()
					.addPoints(PointVectors.newBuilder()
						.setId(id(9))
						.setVectors(vectors(0.6f, 0.7f))))
				.build());

		List<UpdateResult> response = client.batchUpdateAsync(testName, operations).get();

		response.forEach(result -> assertEquals(UpdateStatus.Completed, result.getStatus()));
	}

	private void createAndSeedCollection(String collectionName) throws ExecutionException, InterruptedException {
		CreateCollection request = CreateCollection.newBuilder()
			.setCollectionName(collectionName)
			.setVectorsConfig(VectorsConfig.newBuilder()
				.setParams(VectorParams.newBuilder()
					.setDistance(Distance.Cosine)
					.setSize(2)
					.build())
				.build())
			.build();

		client.createCollectionAsync(request).get();

		// ToDo: create params for integer index, so that only Range is enabled
		//
		// Collections.PayloadIndexParams params = Collections.PayloadIndexParamsOrBuilder()

		UpdateResult resultIndex = client.createPayloadIndexAsync(
			testName,
			"bar",
			PayloadSchemaType.Integer,
			null,
			true,
			null,
			null).get();

		assertEquals(UpdateStatus.Completed, resultIndex.getStatus());

		UpdateResult result = client.upsertAsync(collectionName, ImmutableList.of(
			PointStruct.newBuilder()
				.setId(id(8))
				.setVectors(VectorsFactory.vectors(ImmutableList.of(3.5f, 4.5f)))
				.putAllPayload(ImmutableMap.of(
					"foo", value("hello"),
					"bar", value(1),
					"date", value("2021-01-01T00:00:00Z")
				))
				.build(),
			PointStruct.newBuilder()
				.setId(id(9))
				.setVectors(VectorsFactory.vectors(ImmutableList.of(10.5f, 11.5f)))
				.putAllPayload(ImmutableMap.of(
					"foo", value("goodbye"),
					"bar", value(2),
					"date", value("2024-01-02T00:00:00Z")
				))
				.build()
		)).get();
		assertEquals(UpdateStatus.Completed, result.getStatus());
	}
}
