package io.qdrant.client.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points.Filter;
import io.qdrant.client.grpc.Points.PointId;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.PointsIdsList;
import io.qdrant.client.grpc.Points.PointsSelector;
import io.qdrant.client.grpc.Points.ReadConsistency;
import io.qdrant.client.grpc.Points.ReadConsistencyType;
import io.qdrant.client.grpc.Points.Vector;
import io.qdrant.client.grpc.Points.WriteOrdering;
import io.qdrant.client.grpc.Points.WriteOrderingType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PointUtilTest {

  @Test
  void testCreatePointsSelectorWithLongIds() {
    long[] pointIds = {1L, 2L, 3L};
    PointsSelector pointsSelector = PointUtil.createPointsSelector(pointIds);

    PointsIdsList expectedIdsList =
        PointsIdsList.newBuilder()
            .addAllIds(
                Arrays.asList(PointUtil.pointId(1L), PointUtil.pointId(2L), PointUtil.pointId(3L)))
            .build();

    assertEquals(expectedIdsList, pointsSelector.getPoints());
  }

  @Test
  void testCreatePointsSelectorWithStringIds() {
    String[] pointIds = {
      "123e4567-e89b-12d3-a456-426614174000",
      "123e4567-e89b-12d3-a456-426614174001",
      "123e4567-e89b-12d3-a456-426614174002"
    };
    PointsSelector pointsSelector = PointUtil.createPointsSelector(pointIds);

    PointsIdsList expectedIdsList =
        PointsIdsList.newBuilder()
            .addAllIds(
                Arrays.asList(
                    PointUtil.pointId("123e4567-e89b-12d3-a456-426614174000"),
                    PointUtil.pointId("123e4567-e89b-12d3-a456-426614174001"),
                    PointUtil.pointId("123e4567-e89b-12d3-a456-426614174002")))
            .build();

    assertEquals(expectedIdsList, pointsSelector.getPoints());
  }

  @Test
  void testCreatePointsSelectorWithIterable() {
    List<PointId> pointIds =
        Arrays.asList(PointUtil.pointId(1L), PointUtil.pointId(2L), PointUtil.pointId(3L));
    PointsSelector pointsSelector = PointUtil.createPointsSelector(pointIds);

    PointsIdsList expectedIdsList =
        PointsIdsList.newBuilder()
            .addAllIds(
                Arrays.asList(PointUtil.pointId(1L), PointUtil.pointId(2L), PointUtil.pointId(3L)))
            .build();

    assertEquals(expectedIdsList, pointsSelector.getPoints());
  }

  @Test
  void testPointsSelector() {
    Filter filter = Filter.newBuilder().build();
    PointsSelector pointsSelector = PointUtil.pointsSelector(filter);

    assertEquals(filter, pointsSelector.getFilter());
  }

  @Test
  void testPointIdWithLong() {
    long num = 42L;
    PointId pointId = PointUtil.pointId(num);

    assertEquals(num, pointId.getNum());
  }

  @Test
  void testPointIdWithString() {
    String uuid = "123e4567-e89b-12d3-a456-426614174000";
    PointId pointId = PointUtil.pointId(uuid);

    assertEquals(uuid, pointId.getUuid());
  }

  @Test
  void testPointIdWithUUID() {
    UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    PointId pointId = PointUtil.pointId(uuid);

    assertEquals(uuid.toString(), pointId.getUuid());
  }

  @Test
  void testPointWithUUID() {
    UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    Vector vector = Vector.newBuilder().build();
    Map<String, Value> payload = Collections.emptyMap();
    PointStruct pointStruct = PointUtil.point(uuid, vector, payload);

    assertEquals(uuid.toString(), pointStruct.getId().getUuid());
    assertEquals(vector, pointStruct.getVectors().getVector());
    assertEquals(payload, pointStruct.getPayloadMap());
  }

  @Test
  void testPointWithLongId() {
    long id = 42L;
    Vector vector = Vector.newBuilder().build();
    Map<String, Value> payload = Collections.emptyMap();
    PointStruct pointStruct = PointUtil.point(id, vector, payload);

    assertEquals(id, pointStruct.getId().getNum());
    assertEquals(vector, pointStruct.getVectors().getVector());
    assertEquals(payload, pointStruct.getPayloadMap());
  }

  @Test
  void testPointWithPointId() {
    PointId id = PointUtil.pointId(42L);
    Vector vector = Vector.newBuilder().build();
    Map<String, Value> payload = Collections.emptyMap();
    PointStruct pointStruct = PointUtil.point(id, vector, payload);

    assertEquals(id, pointStruct.getId());
    assertEquals(vector, pointStruct.getVectors().getVector());
    assertEquals(payload, pointStruct.getPayloadMap());
  }

  @Test
  void testNamedPointWithLongId() {
    Long id = 42L;
    String vectorName = "vector";
    float[] vectorData = {1.0f, 2.0f, 3.0f};
    Map<String, Value> payload = Collections.emptyMap();
    PointStruct pointStruct = PointUtil.namedPoint(id, vectorName, vectorData, payload);

    assertEquals(PointUtil.pointId(id), pointStruct.getId());
    assertEquals(
        VectorUtil.namedVector(vectorName, vectorData), pointStruct.getVectors().getVectors());
    assertEquals(payload, pointStruct.getPayloadMap());
  }

  @Test
  void testNamedPointWithPointId() {
    PointId id = PointUtil.pointId(42L);
    String vectorName = "vector";
    float[] vectorData = {1.0f, 2.0f, 3.0f};
    Map<String, Value> payload = Collections.emptyMap();
    PointStruct pointStruct = PointUtil.namedPoint(id, vectorName, vectorData, payload);

    assertEquals(id, pointStruct.getId());
    assertEquals(
        VectorUtil.namedVector(vectorName, vectorData), pointStruct.getVectors().getVectors());
    assertEquals(payload, pointStruct.getPayloadMap());
  }

  @Test
  void testOrdering() {
    WriteOrderingType orderingType = WriteOrderingType.Medium;
    WriteOrdering ordering = PointUtil.ordering(orderingType);

    assertEquals(orderingType, ordering.getType());
  }

  @Test
  void testConsistency() {
    ReadConsistencyType consistencyType = ReadConsistencyType.Quorum;
    ReadConsistency consistency = PointUtil.consistency(consistencyType);

    assertEquals(consistencyType, consistency.getType());
  }
}
