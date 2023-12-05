package io.qdrant.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points;
import io.qdrant.client.grpc.Points.Filter;
import io.qdrant.client.grpc.Points.PointId;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.Points.SearchResponse;
import io.qdrant.client.utils.FilterUtil;
import io.qdrant.client.utils.PayloadUtil;
import io.qdrant.client.utils.PointUtil;
import io.qdrant.client.utils.SelectorUtil;
import io.qdrant.client.utils.VectorUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class QdrantClientPointsTest {

  private static final int EMBEDDINGS_SIZE = 768;
  private static QdrantClient qdrantClient;

  @BeforeAll
  static void setUp() throws Exception {
    String qdrantUrl = System.getenv("QDRANT_URL");
    String apiKey = System.getenv("QDRANT_API_KEY");

    if (qdrantUrl == null) {
      qdrantUrl = "http://localhost:6334";
    }

    if (apiKey == null) {
      qdrantClient = new QdrantClient(qdrantUrl);
    } else {
      qdrantClient = new QdrantClient(qdrantUrl, apiKey);
    }
  }

  @Test
  void testPointsWithPayloadFilters() {
    String collectionName = UUID.randomUUID().toString();

    UUID pointID = UUID.randomUUID();

    qdrantClient.createCollection(collectionName, EMBEDDINGS_SIZE, Distance.Cosine);

    PointId[] pointIds = new PointId[] {PointUtil.pointId(pointID)};

    Points.GetResponse response =
        qdrantClient.getPoints(
            collectionName,
            Arrays.asList(pointIds),
            SelectorUtil.withVectors(),
            SelectorUtil.withPayload(),
            null);

    assertEquals(0, response.getResultCount());

    Map<String, Object> data = new HashMap<>();
    data.put("name", "Anush");
    data.put("age", 32);

    Map<String, Object> nestedData = new HashMap<>();
    nestedData.put("color", "Blue");
    nestedData.put("movie", "Man Of Steel");

    data.put("favourites", nestedData);

    PointStruct point =
        PointUtil.point(
            pointID, VectorUtil.dummyVector(EMBEDDINGS_SIZE), PayloadUtil.toPayload(data));

    List<PointStruct> points = Arrays.asList(point);
    qdrantClient.upsertPointsBlocking(collectionName, points, null);
    response =
        qdrantClient.getPoints(
            collectionName,
            Arrays.asList(pointIds),
            SelectorUtil.withVectors(),
            SelectorUtil.withPayload(),
            null);
    assertEquals(1, response.getResultCount());

    Filter filter =
        FilterUtil.must(
            FilterUtil.fieldCondition("age", FilterUtil.match(32)),
            FilterUtil.fieldCondition("name", FilterUtil.match("Anush")),
            FilterUtil.fieldCondition("favourites.color", FilterUtil.match("Blue")),
            FilterUtil.fieldCondition("favourites.movie", FilterUtil.match("Man Of Steel")));
    qdrantClient.deletePointsBlocking(collectionName, SelectorUtil.filterSelector(filter), null);

    response =
        qdrantClient.getPoints(
            collectionName,
            Arrays.asList(pointIds),
            SelectorUtil.withVectors(),
            SelectorUtil.withPayload(),
            null);

    assertEquals(0, response.getResultCount());
  }

  @Test
  void testUpsertPoints() {
    String collectionName = UUID.randomUUID().toString();

    UUID pointID = UUID.randomUUID();

    qdrantClient.createCollection(collectionName, EMBEDDINGS_SIZE, Distance.Cosine);

    PointId[] pointIds = new PointId[] {PointUtil.pointId(pointID)};

    Points.GetResponse response =
        qdrantClient.getPoints(
            collectionName,
            Arrays.asList(pointIds),
            SelectorUtil.withVectors(),
            SelectorUtil.withPayload(),
            null);

    assertEquals(0, response.getResultCount());

    PointStruct point = PointUtil.point(pointID, VectorUtil.dummyVector(EMBEDDINGS_SIZE), null);
    List<PointStruct> points = Arrays.asList(point);
    qdrantClient.upsertPointsBlocking(collectionName, points, null);
    response =
        qdrantClient.getPoints(
            collectionName,
            Arrays.asList(pointIds),
            SelectorUtil.withVectors(),
            SelectorUtil.withPayload(),
            null);
    assertEquals(1, response.getResultCount());

    qdrantClient.deletePointsBlocking(collectionName, SelectorUtil.idsSelector(pointIds), null);

    response =
        qdrantClient.getPoints(
            collectionName,
            Arrays.asList(pointIds),
            SelectorUtil.withVectors(),
            SelectorUtil.withPayload(),
            null);

    assertEquals(0, response.getResultCount());
  }

  @Test
  void testUpsertPointsBatch() {
    String collectionName = UUID.randomUUID().toString();

    qdrantClient.createCollection(collectionName, EMBEDDINGS_SIZE, Distance.Cosine);

    List<PointStruct> points = new ArrayList<>();

    // Upsert 1000 points with batching
    for (int i = 0; i < 1000; i++) {
      UUID pointID = UUID.randomUUID();
      PointStruct point = PointUtil.point(pointID, VectorUtil.dummyVector(EMBEDDINGS_SIZE), null);
      points.add(point);
    }

    assertDoesNotThrow(
        () -> {
          qdrantClient.upsertPointsBatchBlocking(collectionName, points, null, 100);
        });
  }

  @Test
  void testSearchPoints() {
    String collectionName = UUID.randomUUID().toString();

    qdrantClient.createCollection(collectionName, EMBEDDINGS_SIZE, Distance.Cosine);

    List<PointStruct> points = new ArrayList<>();

    // Upsert 100 points
    for (int i = 0; i < 100; i++) {
      UUID pointID = UUID.randomUUID();
      PointStruct point = PointUtil.point(pointID, VectorUtil.dummyVector(EMBEDDINGS_SIZE), null);
      points.add(point);
    }

    assertDoesNotThrow(
        () -> {
          qdrantClient.upsertPointsBlocking(collectionName, points, null);
        });

    SearchPoints request =
        SearchPoints.newBuilder()
            .setCollectionName(collectionName)
            .addAllVector(VectorUtil.dummyEmbeddings(EMBEDDINGS_SIZE))
            .setWithPayload(SelectorUtil.withPayload())
            .setLimit(100)
            .build();

    SearchResponse result = qdrantClient.searchPoints(request);

    assertEquals(result.getResultList().size(), 100);
  }

  @Test
  void testSetPayloadWithScroll() {
    String collectionName = UUID.randomUUID().toString();

    qdrantClient.createCollection(collectionName, EMBEDDINGS_SIZE, Distance.Cosine);

    List<PointStruct> points = new ArrayList<>();

    // Upsert 100 points
    for (int i = 0; i < 100; i++) {
      UUID pointID = UUID.randomUUID();
      PointStruct point = PointUtil.point(pointID, VectorUtil.dummyVector(EMBEDDINGS_SIZE), null);
      points.add(point);
    }

    assertDoesNotThrow(
        () -> {
          qdrantClient.upsertPointsBlocking(collectionName, points, null);
        });

    Map<String, Object> data = new HashMap<>();
    data.put("name", "Anush");
    data.put("age", 32);

    Map<String, Object> nestedData = new HashMap<>();
    nestedData.put("color", "Blue");
    nestedData.put("movie", "Man of Steel");

    data.put("favourites", nestedData);

    Map<String, Value> payload = PayloadUtil.toPayload(data);

    qdrantClient.setPayloadBlocking(
        collectionName, SelectorUtil.filterSelector(FilterUtil.must()), payload, null);

    Points.ScrollPoints request =
        Points.ScrollPoints.newBuilder()
            .setCollectionName(collectionName)
            .setWithPayload(SelectorUtil.withPayload())
            .setLimit(100)
            .build();

    Points.ScrollResponse response = qdrantClient.scroll(request);

    response
        .getResultList()
        .forEach(
            (point) -> {
              assertEquals(PayloadUtil.toMap(point.getPayloadMap()), data);
              assertEquals(point.getPayloadMap(), PayloadUtil.toPayload(data));
            });
  }
}
