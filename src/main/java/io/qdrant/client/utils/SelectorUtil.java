package io.qdrant.client.utils;

import io.qdrant.client.grpc.Points.Filter;
import io.qdrant.client.grpc.Points.PayloadIncludeSelector;
import io.qdrant.client.grpc.Points.PointId;
import io.qdrant.client.grpc.Points.PointsIdsList;
import io.qdrant.client.grpc.Points.PointsSelector;
import io.qdrant.client.grpc.Points.VectorsSelector;
import io.qdrant.client.grpc.Points.WithPayloadSelector;
import io.qdrant.client.grpc.Points.WithVectorsSelector;

import java.util.Arrays;
import java.util.List;

/** Utility class for working with Selectors. */
public class SelectorUtil {

  /**
   * Creates a {@link WithPayloadSelector} with the enable flag set to true.
   *
   * @return The created {@link WithPayloadSelector} object.
   */
  public static WithPayloadSelector withPayload() {
    return WithPayloadSelector.newBuilder().setEnable(true).build();
  }

  /**
   * Creates a {@link WithVectorsSelector} with the enable flag set to true.
   *
   * @return The created {@link WithVectorsSelector} object.
   */
  public static WithVectorsSelector withVectors() {
    return WithVectorsSelector.newBuilder().setEnable(true).build();
  }

  /**
   * Creates a {@link WithPayloadSelector} with the specified fields included in the payload.
   *
   * @param fields The fields to include in the payload.
   * @return The created {@link WithPayloadSelector} object.
   */
  public static WithPayloadSelector withPayload(String... fields) {
    PayloadIncludeSelector include =
        PayloadIncludeSelector.newBuilder().addAllFields(Arrays.asList(fields)).build();
    return WithPayloadSelector.newBuilder().setInclude(include).build();
  }

  /**
   * Creates a {@link WithVectorsSelector} with the specified vector fields included.
   *
   * @param vectors The names of the vectors to include.
   * @return The created {@link WithVectorsSelector} object.
   */
  public static WithVectorsSelector withVectors(String... vectors) {
    VectorsSelector include = VectorsSelector.newBuilder().addAllNames(Arrays.asList(vectors)).build();
    return WithVectorsSelector.newBuilder().setInclude(include).build();
  }

  /**
   * Creates a {@link PointsSelector} with the specified list of point IDs.
   *
   * @param ids The list of point IDs.
   * @return The created {@link PointsSelector} object.
   */
  public static PointsSelector idsSelector(List<PointId> ids) {
    return PointsSelector.newBuilder()
        .setPoints(PointsIdsList.newBuilder().addAllIds(ids).build())
        .build();
  }

  /**
   * Creates a {@link PointsSelector} with the specified array of point IDs.
   *
   * @param ids The array of point IDs.
   * @return The created {@link PointsSelector} object.
   */
  public static PointsSelector idsSelector(PointId... ids) {
    return PointsSelector.newBuilder()
        .setPoints(PointsIdsList.newBuilder().addAllIds(Arrays.asList(ids)).build())
        .build();
  }

  /**
   * Creates a {@link PointsSelector} with the specified Filter.
   *
   * @param filter The Filter for the selector.
   * @return The created {@link PointsSelector} object.
   */
  public static PointsSelector filterSelector(Filter filter) {
    return PointsSelector.newBuilder().setFilter(filter).build();
  }
}
