package io.qdrant.client.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.qdrant.client.grpc.Points.Filter;
import io.qdrant.client.grpc.Points.PointId;
import io.qdrant.client.grpc.Points.PointsSelector;
import io.qdrant.client.grpc.Points.WithPayloadSelector;
import io.qdrant.client.grpc.Points.WithVectorsSelector;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class SelectorUtilTest {

  @Test
  void testWithPayload() {
    WithPayloadSelector selector = SelectorUtil.withPayload();
    assertTrue(selector.getEnable());
  }

  @Test
  void testWithVectors() {
    WithVectorsSelector selector = SelectorUtil.withVectors();
    assertTrue(selector.getEnable());
  }

  @Test
  void testWithPayloadWithFields() {
    String[] fields = {"field1", "field2"};
    WithPayloadSelector selector = SelectorUtil.withPayload(fields);
    List<String> expectedFields = Arrays.asList(fields);
    assertEquals(expectedFields, selector.getInclude().getFieldsList());
  }

  @Test
  void testWithVectorsWithNames() {
    String[] vectors = {"vector1", "vector2"};
    WithVectorsSelector selector = SelectorUtil.withVectors(vectors);
    List<String> expectedVectors = Arrays.asList(vectors);
    assertEquals(expectedVectors, selector.getInclude().getNamesList());
  }

  @Test
  void testIdsSelectorWithList() {
    List<PointId> ids = Arrays.asList(PointUtil.pointId(1), PointUtil.pointId(2));
    PointsSelector selector = SelectorUtil.idsSelector(ids);
    assertEquals(ids, selector.getPoints().getIdsList());
  }

  @Test
  void testIdsSelectorWithArray() {
    PointId[] ids = {PointUtil.pointId(1), PointUtil.pointId(2)};
    PointsSelector selector = SelectorUtil.idsSelector(ids);
    List<PointId> expectedIds = Arrays.asList(ids);
    assertEquals(expectedIds, selector.getPoints().getIdsList());
  }

  @Test
  void testFilterSelector() {
    Filter filter = Filter.newBuilder().build();
    PointsSelector selector = SelectorUtil.filterSelector(filter);
    assertEquals(filter, selector.getFilter());
  }
}
