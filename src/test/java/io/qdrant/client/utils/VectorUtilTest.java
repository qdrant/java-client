package io.qdrant.client.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.qdrant.client.grpc.Points.NamedVectors;
import io.qdrant.client.grpc.Points.PointVectors;
import io.qdrant.client.grpc.Points.Vector;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class VectorUtilTest {

  @Test
  void testVectorFromList() {
    List<Float> vectorData = Arrays.asList(1.0f, 2.0f, 3.0f);
    Vector vector = VectorUtil.toVector(vectorData);

    assertEquals(vectorData, vector.getDataList());
  }

  @Test
  void testVectorFromArray() {
    float[] vectorData = {1.0f, 2.0f, 3.0f};
    Vector vector = VectorUtil.toVector(vectorData);

    assertEquals(Arrays.asList(1.0f, 2.0f, 3.0f), vector.getDataList());
  }

  @Test
  void testNamedVectorFromList() {
    String name = "vector1";
    List<Float> vectorData = Arrays.asList(1.0f, 2.0f, 3.0f);
    NamedVectors namedVector = VectorUtil.namedVector(name, vectorData);

    assertTrue(namedVector.getVectorsMap().containsKey(name));
    assertEquals(vectorData, namedVector.getVectorsMap().get(name).getDataList());
  }

  @Test
  void testNamedVectorFromArray() {
    String name = "vector1";
    float[] vectorData = {1.0f, 2.0f, 3.0f};
    NamedVectors namedVector = VectorUtil.namedVector(name, vectorData);

    assertTrue(namedVector.getVectorsMap().containsKey(name));
    assertEquals(
        Arrays.asList(1.0f, 2.0f, 3.0f), namedVector.getVectorsMap().get(name).getDataList());
  }

  @Test
  void testNamedVectorsFromMap() {
    String name1 = "vector1";
    String name2 = "vector2";
    Map<String, Vector> vectorsMap = new HashMap<>();
    vectorsMap.put(name1, VectorUtil.toVector(1.0f, 2.0f, 3.0f));
    vectorsMap.put(name2, VectorUtil.toVector(4.0f, 5.0f, 6.0f));
    NamedVectors namedVectors = VectorUtil.namedVectors(vectorsMap);

    assertEquals(vectorsMap, namedVectors.getVectorsMap());
  }

  @Test
  void testPointVectors() {
    String id = "123e4567-e89b-12d3-a456-426614174000";
    String name = "vector1";
    float[] vectorData = {1.0f, 2.0f, 3.0f};
    PointVectors pointVectors = VectorUtil.pointVectors(id, name, vectorData);

    assertEquals(PointUtil.pointId(id), pointVectors.getId());
    assertTrue(pointVectors.getVectors().getVectors().getVectorsMap().containsKey(name));
    assertEquals(
        Arrays.asList(1.0f, 2.0f, 3.0f),
        pointVectors.getVectors().getVectors().getVectorsMap().get(name).getDataList());
  }
}
