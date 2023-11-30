package io.qdrant.client.utils;

import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points.Filter;
import io.qdrant.client.grpc.Points.NamedVectors;
import io.qdrant.client.grpc.Points.PointId;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.PointsIdsList;
import io.qdrant.client.grpc.Points.PointsSelector;
import io.qdrant.client.grpc.Points.ReadConsistency;
import io.qdrant.client.grpc.Points.ReadConsistencyType;
import io.qdrant.client.grpc.Points.Vector;
import io.qdrant.client.grpc.Points.Vectors;
import io.qdrant.client.grpc.Points.WriteOrdering;
import io.qdrant.client.grpc.Points.WriteOrderingType;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

/** Utility class for working with Points. */
public class PointUtil {

  /**
   * Creates a {@link PointsSelector} with point IDs specified as long values.
   *
   * @param pointIds The array of point IDs.
   * @return The created PointsSelector.
   */
  public static PointsSelector createPointsSelector(long... pointIds) {

    PointsIdsList pointsIdsList =
        PointsIdsList.newBuilder()
            .addAllIds(Arrays.stream(pointIds).mapToObj(PointUtil::pointId).toList())
            .build();

    return PointsSelector.newBuilder().setPoints(pointsIdsList).build();
  }

  /**
   * Creates a {@link PointsSelector} with point IDs specified as strings.
   *
   * @param pointIds The array of point IDs.
   * @return The created PointsSelector.
   */
  public static PointsSelector createPointsSelector(String... pointIds) {

    // Validating the UUIDs
    Arrays.stream(pointIds).forEach((String id) -> UUID.fromString(id));

    // mapToObj() couldn't resolve the method overloads of createPointId()
    // Using map() instead
    PointsIdsList pointsIdsList =
        PointsIdsList.newBuilder()
            .addAllIds(Arrays.stream(pointIds).map((String id) -> PointUtil.pointId(id)).toList())
            .build();

    return PointsSelector.newBuilder().setPoints(pointsIdsList).build();
  }

  /**
   * Creates a {@link PointsSelector} with point IDs specified as an iterable of {@link PointId}.
   *
   * @param pointIds The iterable of point IDs.
   * @return The created PointsSelector.
   */
  public static PointsSelector createPointsSelector(Iterable<? extends PointId> pointIds) {
    PointsIdsList pointsIdsList = PointsIdsList.newBuilder().addAllIds(pointIds).build();

    return PointsSelector.newBuilder().setPoints(pointsIdsList).build();
  }

  /**
   * Creates a {@link PointsSelector} with a filter condition.
   *
   * @param filter The filter condition.
   * @return The created PointsSelector.
   */
  public static PointsSelector pointsSelector(Filter filter) {
    return PointsSelector.newBuilder().setFilter(filter).build();
  }

  /**
   * Creates a {@link PointId} with a long value.
   *
   * @param num The long value for the point ID.
   * @return The created PointId.
   * @throws IllegalArgumentException if the provided long value is negative.
   */
  public static PointId pointId(long num) {
    if (num < 0) {
      throw new IllegalArgumentException("Point ID must be an unsigned integer or UUID");
    }
    return PointId.newBuilder().setNum(num).build();
  }

  /**
   * Creates a {@link PointId} with a string representing a UUID.
   *
   * @param uuid The string representation of the UUID.
   * @return The created PointId.
   * @throws IllegalArgumentException if the provided string is not a valid UUID.
   */
  public static PointId pointId(String uuid) {
    UUID.fromString(uuid); // Throws IllegalArgumentException if the string is not a valid UUID
    return PointId.newBuilder().setUuid(uuid).build();
  }

  /**
   * Creates a {@link PointId} with a {@link UUID}.
   *
   * @param uuid The UUID.
   * @return The created PointId.
   */
  public static PointId pointId(UUID uuid) {
    return PointId.newBuilder().setUuid(uuid.toString()).build();
  }

  /**
   * Creates a {@link PointStruct} with a UUID, a {@link Vector}, and payload data.
   *
   * @param uuid The UUID.
   * @param vector The vector data.
   * @param payload The payload data.
   * @return The created PointStruct.
   */
  public static PointStruct point(UUID uuid, Vector vector, Map<String, Value> payload) {

    return point(pointId(uuid), vector, payload);
  }

  /**
   * Creates a {@link PointStruct} with a long ID, a {@link Vector}, and payload data.
   *
   * @param id The long ID.
   * @param vector The vector data.
   * @param payload The payload data.
   * @return The created PointStruct.
   */
  public static PointStruct point(long id, Vector vector, Map<String, Value> payload) {

    return point(pointId(id), vector, payload);
  }

  /**
   * Creates a {@link PointStruct} with a UUID string, a {@link Vector}, and payload data.
   *
   * @param uuid The UUID string.
   * @param vector The vector data.
   * @param payload The payload data.
   * @return The created PointStruct.
   */
  public static PointStruct point(String uuid, Vector vector, Map<String, Value> payload) {

    return point(pointId(uuid), vector, payload);
  }

  /**
   * Creates a {@link PointStruct} with a {@link PointId}, a {@link Vector}, and payload data.
   *
   * @param id The PointId.
   * @param vector The vector data.
   * @param payload The payload data.
   * @return The created PointStruct.
   */
  public static PointStruct point(PointId id, Vector vector, Map<String, Value> payload) {
    PointStruct.Builder builder =
        PointStruct.newBuilder().setId(id).setVectors(Vectors.newBuilder().setVector(vector));
    if (payload != null) {
      builder.putAllPayload(payload);
    }
    return builder.build();
  }

  /**
   * Creates a named {@link PointStruct} with a long ID, vector name, vector data, and payload data.
   *
   * @param id The long ID.
   * @param vectorName The name of the vector.
   * @param vectorData The vector data.
   * @param payload The payload data.
   * @return The created PointStruct.
   */
  public static PointStruct namedPoint(
      Long id, String vectorName, float[] vectorData, Map<String, Value> payload) {
    return namedPoint(pointId(id), vectorName, vectorData, payload);
  }

  /**
   * Creates a named {@link PointStruct} with a {@link PointId}, vector name, vector data, and
   * payload data.
   *
   * @param id The PointId.
   * @param vectorName The name of the vector.
   * @param vectorData The vector data.
   * @param payload The payload data.
   * @return The created PointStruct.
   */
  public static PointStruct namedPoint(
      PointId id, String vectorName, float[] vectorData, Map<String, Value> payload) {
    NamedVectors vectors = VectorUtil.namedVector(vectorName, vectorData);
    PointStruct.Builder builder =
        PointStruct.newBuilder().setId(id).setVectors(Vectors.newBuilder().setVectors(vectors));
    if (payload != null) {
      builder.putAllPayload(payload);
    }
    return builder.build();
  }

  /**
   * Creates a {@link WriteOrdering} with the specified ordering type.
   *
   * @param orderingType The ordering type.
   * @return The created WriteOrdering.
   */
  public static WriteOrdering ordering(WriteOrderingType orderingType) {
    return WriteOrdering.newBuilder().setType(orderingType).build();
  }

  /**
   * Creates a {@link ReadConsistency} with the specified consistency type.
   *
   * @param consistencyType The consistency type.
   * @return The created ReadConsistency.
   */
  public static ReadConsistency consistency(ReadConsistencyType consistencyType) {
    return ReadConsistency.newBuilder().setType(consistencyType).build();
  }
}
