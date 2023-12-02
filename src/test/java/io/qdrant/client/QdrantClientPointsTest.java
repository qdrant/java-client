package io.qdrant.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Points;
import io.qdrant.client.grpc.Points.Filter;
import io.qdrant.client.grpc.Points.PointId;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.utils.FilterUtil;
import io.qdrant.client.utils.PayloadUtil;
import io.qdrant.client.utils.PointUtil;
import io.qdrant.client.utils.SelectorUtil;
import io.qdrant.client.utils.VectorUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class QdrantClientPointsTest {

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

    qdrantClient.createCollection(collectionName, 768, Distance.Cosine);

    PointId[] pointIds = new PointId[] {PointUtil.pointId(pointID)};

    Points.GetResponse response =
        qdrantClient.getPoints(
            collectionName,
            List.of(pointIds),
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
        PointUtil.point(pointID, VectorUtil.dummyVector(768), PayloadUtil.toPayload(data));

    List<PointStruct> points = List.of(point);
    qdrantClient.upsertPointsBlocking(collectionName, points, null);
    response =
        qdrantClient.getPoints(
            collectionName,
            List.of(pointIds),
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
            List.of(pointIds),
            SelectorUtil.withVectors(),
            SelectorUtil.withPayload(),
            null);

    assertEquals(0, response.getResultCount());
  }

  @Test
  void testUpsertPoints() {
    String collectionName = UUID.randomUUID().toString();

    UUID pointID = UUID.randomUUID();

    qdrantClient.createCollection(collectionName, 768, Distance.Cosine);

    PointId[] pointIds = new PointId[] {PointUtil.pointId(pointID)};

    Points.GetResponse response =
        qdrantClient.getPoints(
            collectionName,
            List.of(pointIds),
            SelectorUtil.withVectors(),
            SelectorUtil.withPayload(),
            null);

    assertEquals(0, response.getResultCount());

    PointStruct point = PointUtil.point(pointID, VectorUtil.dummyVector(768), null);
    List<PointStruct> points = List.of(point);
    qdrantClient.upsertPointsBlocking(collectionName, points, null);
    response =
        qdrantClient.getPoints(
            collectionName,
            List.of(pointIds),
            SelectorUtil.withVectors(),
            SelectorUtil.withPayload(),
            null);
    assertEquals(1, response.getResultCount());

    qdrantClient.deletePointsBlocking(collectionName, SelectorUtil.idsSelector(pointIds), null);

    response =
        qdrantClient.getPoints(
            collectionName,
            List.of(pointIds),
            SelectorUtil.withVectors(),
            SelectorUtil.withPayload(),
            null);

    assertEquals(0, response.getResultCount());
  }

  @Test
  void testUpsertPointsBatch() {
    String collectionName = UUID.randomUUID().toString();

    qdrantClient.createCollection(collectionName, 768, Distance.Cosine);

    List<PointStruct> points = new ArrayList<>();

    for (int i = 0; i < 1000; i++) {
      UUID pointID = UUID.randomUUID();
      PointStruct point = PointUtil.point(pointID, VectorUtil.dummyVector(768), null);
      points.add(point);
    }

    assertDoesNotThrow(
        () -> {
          qdrantClient.upsertPointsBatchBlocking(collectionName, points, null, 100);
        });
  }
}
