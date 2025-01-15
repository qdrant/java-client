package io.qdrant.client;

import static io.qdrant.client.PointIdFactory.id;

import com.google.common.primitives.Floats;
import io.qdrant.client.grpc.Points.DenseVector;
import io.qdrant.client.grpc.Points.Document;
import io.qdrant.client.grpc.Points.Image;
import io.qdrant.client.grpc.Points.InferenceObject;
import io.qdrant.client.grpc.Points.MultiDenseVector;
import io.qdrant.client.grpc.Points.PointId;
import io.qdrant.client.grpc.Points.SparseVector;
import io.qdrant.client.grpc.Points.VectorInput;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** Convenience methods for constructing {@link VectorInput} */
public final class VectorInputFactory {
  private VectorInputFactory() {}

  /**
   * Creates a {@link VectorInput} from a list of floats
   *
   * @param values A map of vector names to values
   * @return A new instance of {@link VectorInput}
   */
  public static VectorInput vectorInput(List<Float> values) {
    return VectorInput.newBuilder().setDense(DenseVector.newBuilder().addAllData(values)).build();
  }

  /**
   * Creates a {@link VectorInput} from a list of floats
   *
   * @param values A list of values
   * @return A new instance of {@link VectorInput}
   */
  public static VectorInput vectorInput(float... values) {
    return VectorInput.newBuilder()
        .setDense(DenseVector.newBuilder().addAllData(Floats.asList(values)))
        .build();
  }

  /**
   * Creates a {@link VectorInput} from a list of floats and integers as indices
   *
   * @param vector The list of floats representing the vector.
   * @param indices The list of integers representing the indices.
   * @return A new instance of {@link VectorInput}
   */
  public static VectorInput vectorInput(List<Float> vector, List<Integer> indices) {
    return VectorInput.newBuilder()
        .setSparse(SparseVector.newBuilder().addAllValues(vector).addAllIndices(indices).build())
        .build();
  }

  /**
   * Creates a {@link VectorInput} from a nested list of floats representing a multi vector
   *
   * @param vectors The nested list of floats.
   * @return A new instance of {@link VectorInput}
   */
  public static VectorInput multiVectorInput(List<List<Float>> vectors) {
    List<DenseVector> denseVectors =
        vectors.stream()
            .map(v -> DenseVector.newBuilder().addAllData(v).build())
            .collect(Collectors.toList());
    return VectorInput.newBuilder()
        .setMultiDense(MultiDenseVector.newBuilder().addAllVectors(denseVectors).build())
        .build();
  }

  /**
   * Creates a {@link VectorInput} from a nested array of floats representing a multi vector
   *
   * @param vectors The nested array of floats.
   * @return A new instance of {@link VectorInput}
   */
  public static VectorInput multiVectorInput(float[][] vectors) {
    List<DenseVector> denseVectors = new ArrayList<>();
    for (float[] vector : vectors) {
      denseVectors.add(DenseVector.newBuilder().addAllData(Floats.asList(vector)).build());
    }
    return VectorInput.newBuilder()
        .setMultiDense(MultiDenseVector.newBuilder().addAllVectors(denseVectors).build())
        .build();
  }

  /**
   * Creates a {@link VectorInput} from a {@link long}
   *
   * @param id The point id
   * @return a new instance of {@link VectorInput}
   */
  public static VectorInput vectorInput(long id) {
    return VectorInput.newBuilder().setId(id(id)).build();
  }

  /**
   * Creates a {@link VectorInput} from a {@link UUID}
   *
   * @param id The point id
   * @return a new instance of {@link VectorInput}
   */
  public static VectorInput vectorInput(UUID id) {
    return VectorInput.newBuilder().setId(id(id)).build();
  }

  /**
   * Creates a {@link VectorInput} from a {@link PointId}
   *
   * @param id The point id
   * @return a new instance of {@link VectorInput}
   */
  public static VectorInput vectorInput(PointId id) {
    return VectorInput.newBuilder().setId(id).build();
  }

  /**
   * Creates a {@link VectorInput} from a {@link Document}
   *
   * @param document An instance of {@link Document}
   * @return a new instance of {@link VectorInput}
   */
  public static VectorInput vectorInput(Document document) {
    return VectorInput.newBuilder().setDocument(document).build();
  }

  /**
   * Creates a {@link VectorInput} from a an {@link Image}
   *
   * @param image An instance of {@link Image}
   * @return a new instance of {@link VectorInput}
   */
  public static VectorInput vectorInput(Image image) {
    return VectorInput.newBuilder().setImage(image).build();
  }

  /**
   * Creates a {@link VectorInput} from a {@link InferenceObject}
   *
   * @param object An instance of {@link InferenceObject}
   * @return a new instance of {@link VectorInput}
   */
  public static VectorInput vectorInput(InferenceObject object) {
    return VectorInput.newBuilder().setObject(object).build();
  }
}
