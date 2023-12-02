package io.qdrant.client.utils;

import io.qdrant.client.grpc.Points.NamedVectors;
import io.qdrant.client.grpc.Points.PointVectors;
import io.qdrant.client.grpc.Points.Vector;
import io.qdrant.client.grpc.Points.Vectors;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Utility class for working with vector data. */
public class VectorUtil {

  /**
   * Creates a vector from a list of floats.
   *
   * @param vector The list of floats representing the vector.
   * @return The created vector.
   */
  public static Vector toVector(List<Float> vector) {
    Vector.Builder vectorBuilder = Vector.newBuilder();
    return vectorBuilder.addAllData(vector).build();
  }

  /**
   * Creates a vector from an array of floats.
   *
   * @param vector The array of floats representing the vector.
   * @return The created vector.
   */
  public static Vector toVector(float... vector) {
    Vector.Builder vectorBuilder = Vector.newBuilder();
    for (Float f : vector) {
      vectorBuilder.addData(f);
    }
    return vectorBuilder.build();
  }

  /**
   * Creates a named vector from a list of floats.
   *
   * @param name The name of the vector.
   * @param vector The list of floats representing the vector.
   * @return The created named vector.
   */
  public static NamedVectors namedVector(String name, List<Float> vector) {
    NamedVectors.Builder namedVectorBuilder = NamedVectors.newBuilder();
    return namedVectorBuilder.putVectors(name, toVector(vector)).build();
  }

  /**
   * Creates a named vector from an array of floats.
   *
   * @param name The name of the vector.
   * @param vector The array of floats representing the vector.
   * @return The created named vector.
   */
  public static NamedVectors namedVector(String name, float... vector) {
    NamedVectors.Builder namedVectorBuilder = NamedVectors.newBuilder();
    return namedVectorBuilder.putVectors(name, toVector(vector)).build();
  }

  /**
   * Creates named vectors from a map of vector names to vectors.
   *
   * @param vectors The map of vector names to vectors.
   * @return The created named vectors.
   */
  public static NamedVectors namedVectors(Map<String, Vector> vectors) {
    NamedVectors.Builder namedVectorBuilder = NamedVectors.newBuilder();
    return namedVectorBuilder.putAllVectors(vectors).build();
  }

  /**
   * Creates point vectors from an ID, name, and an array of floats.
   *
   * @param id The ID of the point vectors.
   * @param name The name of the point vectors.
   * @param vector The array of floats representing the point vectors.
   * @return The created point vectors.
   */
  public static PointVectors pointVectors(String id, String name, float... vector) {
    PointVectors.Builder pointVectorsBuilder = PointVectors.newBuilder();
    Vectors vectors = Vectors.newBuilder().setVectors(namedVector(name, vector)).build();
    return pointVectorsBuilder.setId(PointUtil.pointId(id)).setVectors(vectors).build();
  }

  /**
   * Generates dummy embeddings of the specified size.
   *
   * @param size The size of the embeddings to generate.
   * @return An array of floats representing the generated embeddings.
   * @throws IllegalArgumentException If the size is less than or equal to zero.
   */
  public static List<Float> dummyEmbeddings(int size) {
    if (size <= 0) {
      throw new IllegalArgumentException("Size must be greater than zero");
    }

    List<Float> embeddings = new ArrayList<>();
    Random random = new Random();

    for (int i = 0; i < size; i++) {
      embeddings.add(random.nextFloat());
    }

    return embeddings;
  }

  /**
   * Generates a dummy vector of the specified size.
   *
   * @param size The size of the vector.
   * @return The generated dummy vector.
   */
  public static Vector dummyVector(int size) {
    return toVector(dummyEmbeddings(size));
  }

  /**
   * Generates a dummy named vector of the specified size.
   *
   * @param name The name of the vector.
   * @param size The size of the vector.
   * @return The generated dummy vector.
   */
  public static NamedVectors dummyNamedVector(String name, int size) {
    return namedVector(name, dummyEmbeddings(size));
  }
}
