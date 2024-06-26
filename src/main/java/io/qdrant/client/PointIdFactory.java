package io.qdrant.client;

import io.qdrant.client.grpc.Points.PointId;
import java.util.UUID;

/** Convenience methods for constructing {@link PointId} */
public final class PointIdFactory {
  private PointIdFactory() {}

  /**
   * Creates a point id from a {@link long}
   *
   * @param id The id
   * @return a new instance of {@link PointId}
   */
  public static PointId id(long id) {
    return PointId.newBuilder().setNum(id).build();
  }

  /**
   * Creates a point id from a {@link UUID}
   *
   * @param id The id
   * @return a new instance of {@link PointId}
   */
  public static PointId id(UUID id) {
    return PointId.newBuilder().setUuid(id.toString()).build();
  }
}
