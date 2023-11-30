package io.qdrant.client.utils;

import io.qdrant.client.grpc.Points.NamedVectors;
import io.qdrant.client.grpc.Points.PointVectors;
import io.qdrant.client.grpc.Points.Vector;
import io.qdrant.client.grpc.Points.Vectors;
import java.util.List;
import java.util.Map;

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
}
