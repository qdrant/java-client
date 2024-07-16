package io.qdrant.client;

import com.google.common.primitives.Floats;
import io.qdrant.client.grpc.Points.SparseIndices;
import io.qdrant.client.grpc.Points.Vector;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

  /**
   * Creates a multi vector from a nested list of floats
   *
   * @param vectors The nested list of floats representing the multi vector.
   * @return A new instance of {@link Vector}
   */
  public static Vector multiVector(List<List<Float>> vectors) {
    int vectorSize = vectors.size();
    List<Float> flatVector = vectors.stream().flatMap(List::stream).collect(Collectors.toList());

    return Vector.newBuilder().addAllData(flatVector).setVectorsCount(vectorSize).build();
  }

  /**
   * Creates a multi vector from a nested array of floats
   *
   * @param vectors The nested array of floats representing the multi vector.
   * @return A new instance of {@link Vector}
   */
  public static Vector multiVector(float[][] vectors) {
    int vectorSize = vectors.length;

    List<Float> flatVector = new ArrayList<>();
    for (float[] vector : vectors) {
      for (float value : vector) {
        flatVector.add(value);
      }
    }

    return Vector.newBuilder().addAllData(flatVector).setVectorsCount(vectorSize).build();
  }
}
