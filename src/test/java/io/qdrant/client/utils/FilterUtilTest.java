package io.qdrant.client.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.qdrant.client.grpc.Points.Condition;
import io.qdrant.client.grpc.Points.Filter;
import io.qdrant.client.grpc.Points.GeoBoundingBox;
import io.qdrant.client.grpc.Points.GeoLineString;
import io.qdrant.client.grpc.Points.GeoPoint;
import io.qdrant.client.grpc.Points.GeoPolygon;
import io.qdrant.client.grpc.Points.GeoRadius;
import io.qdrant.client.grpc.Points.Match;
import io.qdrant.client.grpc.Points.PointId;
import io.qdrant.client.grpc.Points.Range;
import io.qdrant.client.grpc.Points.ValuesCount;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class FilterUtilTest {

  @Test
  void testFieldConditionForTextMatching() {
    String key = "name";
    Match match = FilterUtil.match("Elon");
    Condition condition = FilterUtil.fieldCondition(key, match);

    assertTrue(condition.hasField());
    assertEquals(key, condition.getField().getKey());
    assertTrue(condition.getField().hasMatch());
    assertEquals(match, condition.getField().getMatch());
  }

  @Test
  void testFieldConditionForRangeMatching() {
    String key = "age";
    Range range = FilterUtil.range(20, 50, 20, 50);
    Condition condition = FilterUtil.fieldCondition(key, range);

    assertTrue(condition.hasField());
    assertEquals(key, condition.getField().getKey());
    assertTrue(condition.getField().hasRange());
    assertEquals(range, condition.getField().getRange());
  }

  @Test
  void testFieldConditionForGeoBoundingBoxMatching() {
    String key = "location";
    GeoBoundingBox geoBoundingBox =
        FilterUtil.geoBoundingBox(FilterUtil.geoPoint(10.0, 20.0), FilterUtil.geoPoint(30.0, 40.0));
    Condition condition = FilterUtil.fieldCondition(key, geoBoundingBox);

    assertTrue(condition.hasField());
    assertEquals(key, condition.getField().getKey());
    assertTrue(condition.getField().hasGeoBoundingBox());
    assertEquals(geoBoundingBox, condition.getField().getGeoBoundingBox());
  }

  @Test
  void testFieldConditionForGeoRadiusMatching() {
    String key = "location";
    GeoRadius geoRadius = FilterUtil.geoRadius(FilterUtil.geoPoint(10.0, 20.0), 100.0f);
    Condition condition = FilterUtil.fieldCondition(key, geoRadius);

    assertTrue(condition.hasField());
    assertEquals(key, condition.getField().getKey());
    assertTrue(condition.getField().hasGeoRadius());
    assertEquals(geoRadius, condition.getField().getGeoRadius());
  }

  @Test
  void testFieldConditionForValuesCountMatching() {
    String key = "count";
    ValuesCount valuesCount = FilterUtil.valuesCount(0, 10, 0, 10);
    Condition condition = FilterUtil.fieldCondition(key, valuesCount);

    assertTrue(condition.hasField());
    assertEquals(key, condition.getField().getKey());
    assertTrue(condition.getField().hasValuesCount());
    assertEquals(valuesCount, condition.getField().getValuesCount());
  }

  @Test
  void testFieldConditionForGeoPolygonMatching() {
    String key = "area";
    GeoPolygon geoPolygon =
        FilterUtil.geoPolygon(
            GeoLineString.newBuilder()
                .addPoints(FilterUtil.geoPoint(10.0, 20.0))
                .addPoints(FilterUtil.geoPoint(30.0, 40.0))
                .build(),
            Collections.singletonList(
                GeoLineString.newBuilder()
                    .addPoints(FilterUtil.geoPoint(15.0, 25.0))
                    .addPoints(FilterUtil.geoPoint(35.0, 45.0))
                    .build()));
    Condition condition = FilterUtil.fieldCondition(key, geoPolygon);

    assertTrue(condition.hasField());
    assertEquals(key, condition.getField().getKey());
    assertTrue(condition.getField().hasGeoPolygon());
    assertEquals(geoPolygon, condition.getField().getGeoPolygon());
  }

  @Test
  void testMatchForStringMatching() {
    String text = "Elon";
    Match match = FilterUtil.match(text);

    assertTrue(match.hasKeyword());
    assertEquals(text, match.getKeyword());
    assertFalse(match.hasText());
  }

  @Test
  void testMatchForStringMatchingWithSpace() {
    String text = "Elon Musk";
    Match match = FilterUtil.match(text);

    assertTrue(match.hasText());
    assertEquals(text, match.getText());
    assertFalse(match.hasKeyword());
  }

  @Test
  void testMatchForIntegerMatching() {
    long value = 42;
    Match match = FilterUtil.match(value);

    assertTrue(match.hasInteger());
    assertEquals(value, match.getInteger());
  }

  @Test
  void testMatchForBooleanMatching() {
    boolean value = true;
    Match match = FilterUtil.match(value);

    assertTrue(match.hasBoolean());
    assertEquals(value, match.getBoolean());
  }

  @Test
  void testMatchWithKeywords() {
    List<String> keywords = Arrays.asList("apple", "banana", "cherry");
    Match match = FilterUtil.matchWithKeywords(keywords);

    assertTrue(match.hasKeywords());
    assertEquals(keywords, match.getKeywords().getStringsList());
  }

  @Test
  void testMatchWithIntegers() {
    List<Long> integers = Arrays.asList(1L, 2L, 3L);
    Match match = FilterUtil.matchWithIntegers(integers);

    assertTrue(match.hasIntegers());
    assertEquals(integers, match.getIntegers().getIntegersList());
  }

  @Test
  void testGeoBoundingBox() {
    GeoPoint topLeft = FilterUtil.geoPoint(10.0, 20.0);
    GeoPoint bottomRight = FilterUtil.geoPoint(30.0, 40.0);
    GeoBoundingBox geoBoundingBox = FilterUtil.geoBoundingBox(topLeft, bottomRight);

    assertEquals(topLeft, geoBoundingBox.getTopLeft());
    assertEquals(bottomRight, geoBoundingBox.getBottomRight());
  }

  @Test
  void testGeoRadius() {
    GeoPoint center = FilterUtil.geoPoint(10.0, 20.0);
    float radius = 100.0f;
    GeoRadius geoRadius = FilterUtil.geoRadius(center, radius);

    assertEquals(center, geoRadius.getCenter());
    assertEquals(radius, geoRadius.getRadius());
  }

  @Test
  void testGeoPolygon() {
    GeoLineString exterior =
        GeoLineString.newBuilder()
            .addPoints(FilterUtil.geoPoint(10.0, 20.0))
            .addPoints(FilterUtil.geoPoint(30.0, 40.0))
            .build();
    List<GeoLineString> interiors =
        Collections.singletonList(
            GeoLineString.newBuilder()
                .addPoints(FilterUtil.geoPoint(15.0, 25.0))
                .addPoints(FilterUtil.geoPoint(35.0, 45.0))
                .build());
    GeoPolygon geoPolygon = FilterUtil.geoPolygon(exterior, interiors);

    assertEquals(exterior, geoPolygon.getExterior());
    assertEquals(interiors, geoPolygon.getInteriorsList());
  }

  @Test
  void testRange() {
    double lt = 10.0;
    double gt = 20.0;
    double gte = 15.0;
    double lte = 25.0;
    Range range = FilterUtil.range(lt, gt, gte, lte);

    assertEquals(lt, range.getLt());
    assertEquals(gt, range.getGt());
    assertEquals(gte, range.getGte());
    assertEquals(lte, range.getLte());
  }

  @Test
  void testValuesCount() {
    long lt = 0;
    long gt = 10;
    long gte = 0;
    long lte = 5;
    ValuesCount valuesCount = FilterUtil.valuesCount(lt, gt, gte, lte);

    assertEquals(lt, valuesCount.getLt());
    assertEquals(gt, valuesCount.getGt());
    assertEquals(gte, valuesCount.getGte());
    assertEquals(lte, valuesCount.getLte());
  }

  @Test
  void testFilterCondition() {
    Filter filter = Filter.newBuilder().build();
    Condition condition = FilterUtil.filterCondition(filter);

    assertTrue(condition.hasFilter());
    assertEquals(filter, condition.getFilter());
  }

  @Test
  void testNestedCondition() {
    String key = "nested";
    Filter filter = Filter.newBuilder().build();
    Condition condition = FilterUtil.nestedCondition(key, filter);

    assertTrue(condition.hasNested());
    assertEquals(key, condition.getNested().getKey());
    assertEquals(filter, condition.getNested().getFilter());
  }

  @Test
  void testIsEmptyCondition() {
    String key = "field";
    Condition condition = FilterUtil.isEmptyCondition(key);

    assertTrue(condition.hasIsEmpty());
    assertEquals(key, condition.getIsEmpty().getKey());
  }

  @Test
  void testIsNullCondition() {
    String key = "field";
    Condition condition = FilterUtil.isNullCondition(key);

    assertTrue(condition.hasIsNull());
    assertEquals(key, condition.getIsNull().getKey());
  }

  @Test
  void testHasIdCondition() {
    List<PointId> pointIds = Arrays.asList(PointUtil.pointId(1), PointUtil.pointId(2));
    Condition condition = FilterUtil.hasIdCondition(pointIds);

    assertTrue(condition.hasHasId());
    assertEquals(pointIds, condition.getHasId().getHasIdList());
  }

  @Test
  void testGeoPoint() {
    double latitude = 10.0;
    double longitude = 20.0;
    GeoPoint geoPoint = FilterUtil.geoPoint(latitude, longitude);

    assertEquals(latitude, geoPoint.getLat());
    assertEquals(longitude, geoPoint.getLon());
  }

  @Test
  void testMust() {
    Condition condition1 = Condition.newBuilder().build();
    Condition condition2 = Condition.newBuilder().build();
    Filter filter = FilterUtil.must(condition1, condition2);

    assertEquals(Arrays.asList(condition1, condition2), filter.getMustList());
  }

  @Test
  void testMustNot() {
    Condition condition1 = Condition.newBuilder().build();
    Condition condition2 = Condition.newBuilder().build();
    Filter filter = FilterUtil.mustNot(condition1, condition2);

    assertEquals(Arrays.asList(condition1, condition2), filter.getMustNotList());
  }

  @Test
  void testShould() {
    Condition condition1 = Condition.newBuilder().build();
    Condition condition2 = Condition.newBuilder().build();
    Filter filter = FilterUtil.should(condition1, condition2);

    assertEquals(Arrays.asList(condition1, condition2), filter.getShouldList());
  }
}
