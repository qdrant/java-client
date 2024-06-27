package io.qdrant.client;

import com.google.common.primitives.Floats;
import io.qdrant.client.grpc.Points.SparseIndices;
import io.qdrant.client.grpc.Points.Vector;
import java.util.List;

/** Convenience methods for constructing {@link Vector} */
public final class VectorFactory {
  private VectorFactory() {}

  /**
   * Creates a vector from a list of floats
   *
   * @param values A map of vector names to values
   * @return A new instance of {@link Vector}
   */
  public static Vector vector(List<Float> values) {
    return Vector.newBuilder().addAllData(values).build();
  }

  /**
   * Creates a vector from a list of floats
   *
   * @param values A list of values
   * @return A new instance of {@link Vector}
   */
  public static Vector vector(float... values) {
    return Vector.newBuilder().addAllData(Floats.asList(values)).build();
  }

  /**
   * Creates a sparse vector from a list of floats and integers as indices
   *
   * @param vector The list of floats representing the vector.
   * @param indices The list of integers representing the indices.
   * @return A new instance of {@link Vector}
   */
  public static Vector vector(List<Float> vector, List<Integer> indices) {
    return Vector.newBuilder()
        .addAllData(vector)
        .setIndices(SparseIndices.newBuilder().addAllData(indices).build())
        .build();
  }
}
