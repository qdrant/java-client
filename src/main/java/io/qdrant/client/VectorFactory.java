package io.qdrant.client;

import com.google.common.primitives.Floats;
import io.qdrant.client.grpc.Points.DenseVector;
import io.qdrant.client.grpc.Points.Document;
import io.qdrant.client.grpc.Points.Image;
import io.qdrant.client.grpc.Points.InferenceObject;
import io.qdrant.client.grpc.Points.MultiDenseVector;
import io.qdrant.client.grpc.Points.SparseVector;
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
    return Vector.newBuilder()
        .setDense(DenseVector.newBuilder().addAllData(values).build())
        .build();
  }

  /**
   * Creates a vector from a list of floats
   *
   * @param values A list of values
   * @return A new instance of {@link Vector}
   */
  public static Vector vector(float... values) {
    return Vector.newBuilder()
        .setDense(DenseVector.newBuilder().addAllData(Floats.asList(values)).build())
        .build();
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
        .setSparse(SparseVector.newBuilder().addAllValues(vector).addAllIndices(indices).build())
        .build();
  }

  /**
   * Creates a vector from a document for cloud inference.
   *
   * @param document The document to vectorize.
   * @return A new instance of {@link Vector}
   */
  public static Vector vector(Document document) {
    return Vector.newBuilder().setDocument(document).build();
  }

  /**
   * Creates a vector from an image for cloud inference.
   *
   * @param image The image to vectorize.
   * @return A new instance of {@link Vector}
   */
  public static Vector vector(Image image) {
    return Vector.newBuilder().setImage(image).build();
  }

  /**
   * Creates a vector from an inference object.
   *
   * @param object The inference object to vectorize.
   * @return A new instance of {@link Vector}
   */
  public static Vector vector(InferenceObject object) {
    return Vector.newBuilder().setObject(object).build();
  }

  /**
   * Creates a multi vector from a nested list of floats
   *
   * @param vectors The nested list of floats representing the multi vector.
   * @return A new instance of {@link Vector}
   */
  public static Vector multiVector(List<List<Float>> vectors) {
    List<DenseVector> denseVectors =
        vectors.stream()
            .map(v -> DenseVector.newBuilder().addAllData(v).build())
            .collect(Collectors.toList());
    return Vector.newBuilder()
        .setMultiDense(MultiDenseVector.newBuilder().addAllVectors(denseVectors).build())
        .build();
  }

  /**
   * Creates a multi vector from a nested array of floats
   *
   * @param vectors The nested array of floats representing the multi vector.
   * @return A new instance of {@link Vector}
   */
  public static Vector multiVector(float[][] vectors) {
    List<DenseVector> denseVectors = new ArrayList<>();
    for (float[] vector : vectors) {
      denseVectors.add(DenseVector.newBuilder().addAllData(Floats.asList(vector)).build());
    }
    return Vector.newBuilder()
        .setMultiDense(MultiDenseVector.newBuilder().addAllVectors(denseVectors).build())
        .build();
  }
}
