package io.qdrant.client.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

  @Test
  void testToSparseVector() {
    List<Float> vectorData = Arrays.asList(1.0f, 2.0f, 3.0f);
    List<Integer> indices = Arrays.asList(0, 2, 4);

    Vector result = VectorUtil.toVector(vectorData, indices);

    assertNotNull(result);
    assertEquals(vectorData.size(), result.getDataCount());
    assertNotNull(result.getIndices());
    assertEquals(indices.size(), result.getIndices().getDataCount());
  }

  @Test
  void testToSparseVectorEmptyInput() {
    List<Float> emptyVectorData = Arrays.asList();
    List<Integer> emptyIndices = Arrays.asList();

    Vector result = VectorUtil.toVector(emptyVectorData, emptyIndices);

    assertNotNull(result);
    assertEquals(emptyVectorData.size(), result.getDataCount());
    assertNotNull(result.getIndices());
    assertEquals(emptyIndices.size(), result.getIndices().getDataCount());
  }

  @Test
  void testNamedSparseVector() {
    String name = "testVector";
    List<Float> vector = Arrays.asList(1.0f, 2.0f, 3.0f);
    List<Integer> indices = Arrays.asList(0, 1, 2);

    NamedVectors result = VectorUtil.namedVector(name, vector, indices);

    assertEquals(1, result.getVectorsCount());
    assertTrue(result.getVectorsMap().containsKey(name));

    Vector generatedVector = result.getVectorsMap().get(name);

    assertEquals(vector.size(), generatedVector.getDataCount());
    assertEquals(indices.size(), generatedVector.getIndices().getDataCount());
  }
}
