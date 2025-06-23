package io.qdrant.client;

import static io.qdrant.client.VectorFactory.vector;

import io.qdrant.client.grpc.Points.Document;
import io.qdrant.client.grpc.Points.Image;
import io.qdrant.client.grpc.Points.InferenceObject;
import io.qdrant.client.grpc.Points.NamedVectors;
import io.qdrant.client.grpc.Points.Vector;
import io.qdrant.client.grpc.Points.Vectors;
import java.util.List;
import java.util.Map;

/** Convenience methods for constructing {@link Vectors} */
public final class VectorsFactory {
  private VectorsFactory() {}

  /**
   * Creates named vectors
   *
   * @param values A map of vector names to {@link Vector}
   * @return a new instance of {@link Vectors}
   */
  public static Vectors namedVectors(Map<String, Vector> values) {
    return Vectors.newBuilder().setVectors(NamedVectors.newBuilder().putAllVectors(values)).build();
  }

  /**
   * Creates a vector
   *
   * @param values A list of values
   * @return a new instance of {@link Vectors}
   */
  public static Vectors vectors(List<Float> values) {
    return Vectors.newBuilder().setVector(vector(values)).build();
  }

  /**
   * Creates a vector
   *
   * @param values A list of values
   * @return a new instance of {@link Vectors}
   */
  public static Vectors vectors(float... values) {
    return Vectors.newBuilder().setVector(vector(values)).build();
  }

  /**
   * Creates a vector
   *
   * @param vector An instance of {@link Vector}
   * @return a new instance of {@link Vectors}
   */
  public static Vectors vectors(Vector vector) {
    return Vectors.newBuilder().setVector(vector).build();
  }

  /**
   * Creates an instance of {@link Vectors} from a document for cloud inference.
   *
   * @param document An instance of {@link Document}
   * @return a new instance of {@link Vectors}
   */
  public static Vectors vectors(Document document) {
    return Vectors.newBuilder().setVector(vector(document)).build();
  }

  /**
   * Creates an instance of {@link Vectors} from an image for cloud inference.
   *
   * @param image An instance of {@link Image}
   * @return a new instance of {@link Vectors}
   */
  public static Vectors vectors(Image image) {
    return Vectors.newBuilder().setVector(vector(image)).build();
  }

  /**
   * Creates an instance of {@link Vectors} from an inference object.
   *
   * @param object The inference object to vectorize.
   * @return A new instance of {@link Vectors}
   */
  public static Vectors vectors(InferenceObject object) {
    return Vectors.newBuilder().setVector(vector(object)).build();
  }
}
