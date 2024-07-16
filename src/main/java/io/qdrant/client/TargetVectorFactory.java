package io.qdrant.client;

import io.qdrant.client.grpc.Points.PointId;
import io.qdrant.client.grpc.Points.TargetVector;
import io.qdrant.client.grpc.Points.Vector;
import io.qdrant.client.grpc.Points.VectorExample;

/** Convenience methods for constructing {@link TargetVector} */
public class TargetVectorFactory {
  private TargetVectorFactory() {}

  /**
   * Creates a TargetVector from a point ID
   *
   * @param id The point ID to use
   * @return A new instance of {@link TargetVector}
   */
  public static TargetVector targetVector(PointId id) {
    return TargetVector.newBuilder().setSingle(VectorExample.newBuilder().setId(id)).build();
  }

  /**
   * Creates a TargetVector from a Vector
   *
   * @param vector The Vector value to use
   * @return A new instance of {@link TargetVector}
   */
  public static TargetVector targetVector(Vector vector) {
    return TargetVector.newBuilder()
        .setSingle(VectorExample.newBuilder().setVector(vector))
        .build();
  }
}
