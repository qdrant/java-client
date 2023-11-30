package io.qdrant.client.utils;

import io.qdrant.client.grpc.Points.Condition;
import io.qdrant.client.grpc.Points.FieldCondition;
import io.qdrant.client.grpc.Points.Filter;
import io.qdrant.client.grpc.Points.GeoBoundingBox;
import io.qdrant.client.grpc.Points.GeoLineString;
import io.qdrant.client.grpc.Points.GeoPoint;
import io.qdrant.client.grpc.Points.GeoPolygon;
import io.qdrant.client.grpc.Points.GeoRadius;
import io.qdrant.client.grpc.Points.HasIdCondition;
import io.qdrant.client.grpc.Points.IsEmptyCondition;
import io.qdrant.client.grpc.Points.IsNullCondition;
import io.qdrant.client.grpc.Points.Match;
import io.qdrant.client.grpc.Points.NestedCondition;
import io.qdrant.client.grpc.Points.PointId;
import io.qdrant.client.grpc.Points.Range;
import io.qdrant.client.grpc.Points.RepeatedIntegers;
import io.qdrant.client.grpc.Points.RepeatedStrings;
import io.qdrant.client.grpc.Points.ValuesCount;
import java.util.Arrays;
import java.util.List;

/** Utility class for creating filter conditions and filters. */
public class FilterUtil {

  /**
   * Creates a {@link Condition} with a {@link FieldCondition} for text matching.
   *
   * @param key The key of the field.
   * @param match The text match criteria.
   * @return The created condition.
   */
  public static Condition fieldCondition(String key, Match match) {
    FieldCondition fieldCondition = FieldCondition.newBuilder().setKey(key).setMatch(match).build();

    return Condition.newBuilder().setField(fieldCondition).build();
  }

  /**
   * Creates a {@link Condition} with a {@link FieldCondition} for range matching.
   *
   * @param key The key of the field.
   * @param range The range criteria.
   * @return The created condition.
   */
  public static Condition fieldCondition(String key, Range range) {
    FieldCondition fieldCondition = FieldCondition.newBuilder().setKey(key).setRange(range).build();

    return Condition.newBuilder().setField(fieldCondition).build();
  }

  /**
   * Creates a {@link Condition} with a {@link FieldCondition} for geo bounding box matching.
   *
   * @param key The key of the field.
   * @param geoBoundingBox The geo bounding box criteria.
   * @return The created condition.
   */
  public static Condition fieldCondition(String key, GeoBoundingBox geoBoundingBox) {
    FieldCondition fieldCondition =
        FieldCondition.newBuilder().setKey(key).setGeoBoundingBox(geoBoundingBox).build();

    return Condition.newBuilder().setField(fieldCondition).build();
  }

  /**
   * Creates a {@link Condition} with a {@link FieldCondition} for geo radius matching.
   *
   * @param key The key of the field.
   * @param geoRadius The geo radius criteria.
   * @return The created condition.
   */
  public static Condition fieldCondition(String key, GeoRadius geoRadius) {
    FieldCondition fieldCondition =
        FieldCondition.newBuilder().setKey(key).setGeoRadius(geoRadius).build();

    return Condition.newBuilder().setField(fieldCondition).build();
  }

  /**
   * Creates a {@link Condition} with a {@link FieldCondition} for values count matching.
   *
   * @param key The key of the field.
   * @param valuesCount The values count criteria.
   * @return The created condition.
   */
  public static Condition fieldCondition(String key, ValuesCount valuesCount) {
    FieldCondition fieldCondition =
        FieldCondition.newBuilder().setKey(key).setValuesCount(valuesCount).build();

    return Condition.newBuilder().setField(fieldCondition).build();
  }

  /**
   * Creates a {@link Condition} with a {@link FieldCondition} for geo polygon matching.
   *
   * @param key The key of the field.
   * @param geoPolygon The geo polygon criteria.
   * @return The created condition.
   */
  public static Condition fieldCondition(String key, GeoPolygon geoPolygon) {
    FieldCondition fieldCondition =
        FieldCondition.newBuilder().setKey(key).setGeoPolygon(geoPolygon).build();

    return Condition.newBuilder().setField(fieldCondition).build();
  }

  /**
   * Creates a {@link Match} condition for string matching. If the text contains a space, it is
   * considered a full-text match. Else it is considered a keyword match.
   *
   * @param text The text to match.
   * @return The created match condition.
   */
  public static Match match(String text) {
    return text.contains(" ")
        ? Match.newBuilder().setText(text).build()
        : Match.newBuilder().setKeyword(text).build();
  }

  /**
   * Creates a {@link Match} condition for integer matching.
   *
   * @param value The integer value to match.
   * @return The created match condition.
   */
  public static Match match(long value) {
    return Match.newBuilder().setInteger(value).build();
  }

  /**
   * Creates a {@link Match} condition for boolean matching.
   *
   * @param value The boolean value to match.
   * @return The created match condition.
   */
  public static Match match(boolean value) {
    return Match.newBuilder().setBoolean(value).build();
  }

  /**
   * Creates a {@link Match} condition with a list of keywords for matching.
   *
   * @param keywords The list of keywords to match.
   * @return The created match condition.
   */
  public static Match matchWithKeywords(List<String> keywords) {
    return Match.newBuilder()
        .setKeywords(RepeatedStrings.newBuilder().addAllStrings(keywords).build())
        .build();
  }

  /**
   * Creates a {@link Match} condition with a list of integers for matching.
   *
   * @param integers The list of integers to match.
   * @return The created match condition.
   */
  public static Match matchWithIntegers(List<Long> integers) {
    return Match.newBuilder()
        .setIntegers(RepeatedIntegers.newBuilder().addAllIntegers(integers).build())
        .build();
  }

  /**
   * Creates a {@link GeoBoundingBox} based on the top-left and bottom-right {@link GeoPoint}s.
   *
   * @param topLeft The top-left point of the bounding box.
   * @param bottomRight The bottom-right point of the bounding box.
   * @return The created geo bounding box.
   */
  public static GeoBoundingBox geoBoundingBox(GeoPoint topLeft, GeoPoint bottomRight) {
    return GeoBoundingBox.newBuilder().setTopLeft(topLeft).setBottomRight(bottomRight).build();
  }

  /**
   * Creates a {@link GeoRadius} based on the center {@link GeoPoint} and radius.
   *
   * @param center The center point of the radius.
   * @param radius The radius value.
   * @return The created geo radius.
   */
  public static GeoRadius geoRadius(GeoPoint center, float radius) {
    return GeoRadius.newBuilder().setCenter(center).setRadius(radius).build();
  }

  /**
   * Creates a {@link GeoPolygon} based on the exterior {@link GeoLineString} and a list of interior
   * {@link GeoLineString}s.
   *
   * @param exterior The exterior line string.
   * @param interiors The list of interior line strings.
   * @return The created geo polygon.
   */
  public static GeoPolygon geoPolygon(GeoLineString exterior, List<GeoLineString> interiors) {
    return GeoPolygon.newBuilder().setExterior(exterior).addAllInteriors(interiors).build();
  }

  /**
   * Creates a {@link Range} for numeric range matching.
   *
   * @param lt The less than value.
   * @param gt The greater than value.
   * @param gte The greater than or equal to value.
   * @param lte The less than or equal to value.
   * @return The created range.
   */
  public static Range range(double lt, double gt, double gte, double lte) {
    return Range.newBuilder().setLt(lt).setGt(gt).setGte(gte).setLte(lte).build();
  }

  /**
   * Creates a {@link ValuesCount} for values count matching.
   *
   * @param lt The less than value.
   * @param gt The greater than value.
   * @param gte The greater than or equal to value.
   * @param lte The less than or equal to value.
   * @return The created values count.
   */
  public static ValuesCount valuesCount(long lt, long gt, long gte, long lte) {
    return ValuesCount.newBuilder().setLt(lt).setGt(gt).setGte(gte).setLte(lte).build();
  }

  /**
   * Creates a {@link Condition} with a {@link Filter} as a subcondition.
   *
   * @param filter The filter criteria.
   * @return The created condition.
   */
  public static Condition filterCondition(Filter filter) {
    return Condition.newBuilder().setFilter(filter).build();
  }

  /**
   * Creates a {@link Condition} with a {@link NestedCondition} based on a key and a nested filter.
   *
   * @param key The key of the nested condition.
   * @param filter The nested filter criteria.
   * @return The created condition.
   */
  public static Condition nestedCondition(String key, Filter filter) {
    validateNestedFilter(filter);

    NestedCondition nestedCondition =
        NestedCondition.newBuilder().setKey(key).setFilter(filter).build();

    return Condition.newBuilder().setNested(nestedCondition).build();
  }

  private static void validateNestedFilter(Filter filter) {
    validateNoHasId(filter.getMustList());
    validateNoHasId(filter.getMustNotList());
    validateNoHasId(filter.getShouldList());
  }

  private static void validateNoHasId(List<Condition> conditions) {
    for (Condition condition : conditions) {
      if (condition.hasHasId()) {
        throw new IllegalArgumentException("Nested filter cannot have a HasIdCondition");
      }
    }
  }

  /**
   * Creates a {@link Condition} with an {@link IsEmptyCondition} based on a key for empty condition
   * matching.
   *
   * @param key The key of the field.
   * @return The created condition.
   */
  public static Condition isEmptyCondition(String key) {
    IsEmptyCondition isEmptyCondition = IsEmptyCondition.newBuilder().setKey(key).build();

    return Condition.newBuilder().setIsEmpty(isEmptyCondition).build();
  }

  /**
   * Creates a {@link Condition} with an {@link IsNullCondition} based on a key for null condition
   * matching.
   *
   * @param key The key of the field.
   * @return The created condition.
   */
  public static Condition isNullCondition(String key) {
    IsNullCondition isNullCondition = IsNullCondition.newBuilder().setKey(key).build();

    return Condition.newBuilder().setIsNull(isNullCondition).build();
  }

  /**
   * Creates a {@link Condition} with a {@link HasIdCondition} based on a list of point IDs for ID
   * condition matching.
   *
   * @param pointIds The list of point IDs.
   * @return The created condition.
   */
  public static Condition hasIdCondition(List<PointId> pointIds) {
    HasIdCondition hasIdCondition = HasIdCondition.newBuilder().addAllHasId(pointIds).build();

    return Condition.newBuilder().setHasId(hasIdCondition).build();
  }

  /**
   * Creates a {@link GeoPoint} based on latitude and longitude values.
   *
   * @param latitude The latitude value.
   * @param longitude The longitude value.
   * @return The created geo point.
   */
  public static GeoPoint geoPoint(double latitude, double longitude) {
    return GeoPoint.newBuilder().setLat(latitude).setLon(longitude).build();
  }

  /**
   * Creates a {@link Filter} with "must" conditions.
   *
   * @param mustConditions The list of "must" conditions.
   * @return The created filter.
   */
  public static Filter must(Condition... mustConditions) {
    return Filter.newBuilder().addAllMust(Arrays.asList(mustConditions)).build();
  }

  /**
   * Creates a {@link Filter} with "must not" conditions.
   *
   * @param mustNotConditions The list of "must not" conditions.
   * @return The created filter.
   */
  public static Filter mustNot(Condition... mustNotConditions) {
    return Filter.newBuilder().addAllMustNot(Arrays.asList(mustNotConditions)).build();
  }

  /**
   * Creates a {@link Filter} with "should" conditions.
   *
   * @param shouldConditions The list of "should" conditions.
   * @return The created filter.
   */
  public static Filter should(Condition... shouldConditions) {
    return Filter.newBuilder().addAllShould(Arrays.asList(shouldConditions)).build();
  }

  /**
   * Creates a {@link Filter} with "must" conditions.
   *
   * @param mustConditions The list of "must" conditions.
   * @return The created filter.
   */
  public static Filter must(List<Condition> mustConditions) {
    return Filter.newBuilder().addAllMust(mustConditions).build();
  }

  /**
   * Creates a {@link Filter} with "must not" conditions.
   *
   * @param mustNotConditions The list of "must not" conditions.
   * @return The created filter.
   */
  public static Filter mustNot(List<Condition> mustNotConditions) {
    return Filter.newBuilder().addAllMustNot(mustNotConditions).build();
  }

  /**
   * Creates a {@link Filter} with "should" conditions.
   *
   * @param shouldConditions The list of "should" conditions.
   * @return The created filter.
   */
  public static Filter should(List<Condition> shouldConditions) {
    return Filter.newBuilder().addAllShould(shouldConditions).build();
  }
}
