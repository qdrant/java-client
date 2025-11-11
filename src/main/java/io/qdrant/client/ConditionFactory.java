package io.qdrant.client;

import io.qdrant.client.grpc.Points.Condition;
import io.qdrant.client.grpc.Points.DatetimeRange;
import io.qdrant.client.grpc.Points.FieldCondition;
import io.qdrant.client.grpc.Points.Filter;
import io.qdrant.client.grpc.Points.GeoBoundingBox;
import io.qdrant.client.grpc.Points.GeoLineString;
import io.qdrant.client.grpc.Points.GeoPoint;
import io.qdrant.client.grpc.Points.GeoPolygon;
import io.qdrant.client.grpc.Points.GeoRadius;
import io.qdrant.client.grpc.Points.HasIdCondition;
import io.qdrant.client.grpc.Points.HasVectorCondition;
import io.qdrant.client.grpc.Points.IsEmptyCondition;
import io.qdrant.client.grpc.Points.IsNullCondition;
import io.qdrant.client.grpc.Points.Match;
import io.qdrant.client.grpc.Points.NestedCondition;
import io.qdrant.client.grpc.Points.PointId;
import io.qdrant.client.grpc.Points.Range;
import io.qdrant.client.grpc.Points.RepeatedIntegers;
import io.qdrant.client.grpc.Points.RepeatedStrings;
import io.qdrant.client.grpc.Points.ValuesCount;
import java.util.List;

/** Convenience methods for constructing {@link Condition} */
public final class ConditionFactory {
  private ConditionFactory() {}

  /**
   * Match all records with the provided id
   *
   * @param id The id to match
   * @return a new instance of {@link Condition}
   */
  public static Condition hasId(PointId id) {
    return Condition.newBuilder()
        .setHasId(HasIdCondition.newBuilder().addHasId(id).build())
        .build();
  }

  /**
   * Match all records with the provided ids
   *
   * @param ids The ids to match
   * @return a new instance of {@link Condition}
   */
  public static Condition hasId(List<PointId> ids) {
    return Condition.newBuilder()
        .setHasId(HasIdCondition.newBuilder().addAllHasId(ids).build())
        .build();
  }

  /**
   * Match all records where the given field either does not exist, or has null or empty value.
   *
   * @param field The name of the field
   * @return a new instance of {@link Condition}
   */
  public static Condition isEmpty(String field) {
    return Condition.newBuilder()
        .setIsEmpty(IsEmptyCondition.newBuilder().setKey(field).build())
        .build();
  }

  /**
   * Match all records where the given field is null.
   *
   * @param field The name of the field
   * @return a new instance of {@link Condition}
   */
  public static Condition isNull(String field) {
    return Condition.newBuilder()
        .setIsNull(IsNullCondition.newBuilder().setKey(field).build())
        .build();
  }

  /**
   * Match records where the given field matches the given keyword
   *
   * @param field The name of the field
   * @param keyword The keyword to match
   * @return a new instance of {@link Condition}
   */
  public static Condition matchKeyword(String field, String keyword) {
    return Condition.newBuilder()
        .setField(
            FieldCondition.newBuilder()
                .setKey(field)
                .setMatch(Match.newBuilder().setKeyword(keyword).build())
                .build())
        .build();
  }

  /**
   * Match records where the given field matches the given text.
   *
   * @param field The name of the field
   * @param text The text to match
   * @return a new instance of {@link Condition}
   */
  public static Condition matchText(String field, String text) {
    return Condition.newBuilder()
        .setField(
            FieldCondition.newBuilder()
                .setKey(field)
                .setMatch(Match.newBuilder().setText(text).build())
                .build())
        .build();
  }

  /**
   * Match records where the given field matches the given phrase.
   *
   * @param field The name of the field
   * @param phrase The phrase to match
   * @return a new instance of {@link Condition}
   */
  public static Condition matchPhrase(String field, String phrase) {
    return Condition.newBuilder()
        .setField(
            FieldCondition.newBuilder()
                .setKey(field)
                .setMatch(Match.newBuilder().setPhrase(phrase).build())
                .build())
        .build();
  }

  /**
   * Match records where the given field matches any word in the text.
   *
   * @param field The name of the field
   * @param textAny The text to match
   * @return a new instance of {@link Condition}
   */
  public static Condition matchTextAny(String field, String textAny) {
    return Condition.newBuilder()
        .setField(
            FieldCondition.newBuilder()
                .setKey(field)
                .setMatch(Match.newBuilder().setTextAny(textAny).build())
                .build())
        .build();
  }

  /**
   * Match records where the given field matches the given boolean value.
   *
   * @param field The name of the field
   * @param value The value to match
   * @return a new instance of {@link Condition}
   */
  public static Condition match(String field, boolean value) {
    return Condition.newBuilder()
        .setField(
            FieldCondition.newBuilder()
                .setKey(field)
                .setMatch(Match.newBuilder().setBoolean(value).build())
                .build())
        .build();
  }

  /**
   * Match records where the given field matches the given long value.
   *
   * @param field The name of the field
   * @param value The value to match
   * @return a new instance of {@link Condition}
   */
  public static Condition match(String field, long value) {
    return Condition.newBuilder()
        .setField(
            FieldCondition.newBuilder()
                .setKey(field)
                .setMatch(Match.newBuilder().setInteger(value).build())
                .build())
        .build();
  }

  /**
   * Match records where the given field matches any of the given keywords.
   *
   * @param field The name of the field
   * @param keywords The keywords to match
   * @return a new instance of {@link Condition}
   */
  public static Condition matchKeywords(String field, List<String> keywords) {
    return Condition.newBuilder()
        .setField(
            FieldCondition.newBuilder()
                .setKey(field)
                .setMatch(
                    Match.newBuilder()
                        .setKeywords(RepeatedStrings.newBuilder().addAllStrings(keywords).build())
                        .build())
                .build())
        .build();
  }

  /**
   * Match records where the given field matches any of the given values.
   *
   * @param field The name of the field
   * @param values The values to match
   * @return a new instance of {@link Condition}
   */
  public static Condition matchValues(String field, List<Long> values) {
    return Condition.newBuilder()
        .setField(
            FieldCondition.newBuilder()
                .setKey(field)
                .setMatch(
                    Match.newBuilder()
                        .setIntegers(RepeatedIntegers.newBuilder().addAllIntegers(values).build())
                        .build())
                .build())
        .build();
  }

  /**
   * Match records where the given field does not match any of the given keywords.
   *
   * @param field The name of the field
   * @param keywords The keywords not to match
   * @return a new instance of {@link Condition}
   */
  public static Condition matchExceptKeywords(String field, List<String> keywords) {
    return Condition.newBuilder()
        .setField(
            FieldCondition.newBuilder()
                .setKey(field)
                .setMatch(
                    Match.newBuilder()
                        .setExceptKeywords(
                            RepeatedStrings.newBuilder().addAllStrings(keywords).build())
                        .build())
                .build())
        .build();
  }

  /**
   * Match records where the given field does not match any of the given values.
   *
   * @param field The name of the field
   * @param values The values not to match
   * @return a new instance of {@link Condition}
   */
  public static Condition matchExceptValues(String field, List<Long> values) {
    return Condition.newBuilder()
        .setField(
            FieldCondition.newBuilder()
                .setKey(field)
                .setMatch(
                    Match.newBuilder()
                        .setExceptIntegers(
                            RepeatedIntegers.newBuilder().addAllIntegers(values).build())
                        .build())
                .build())
        .build();
  }

  /**
   * Match records where the given nested field matches the given condition.
   *
   * @param field The name of the nested field.
   * @param condition The condition to match.
   * @return a new instance of {@link Condition}
   */
  public static Condition nested(String field, Condition condition) {
    return Condition.newBuilder()
        .setNested(
            NestedCondition.newBuilder()
                .setKey(field)
                .setFilter(Filter.newBuilder().addMust(condition).build())
                .build())
        .build();
  }

  /**
   * Match records where the given nested field matches the given filter.
   *
   * @param field The name of the nested field.
   * @param filter The filter to match.
   * @return a new instance of {@link Condition}
   */
  public static Condition nested(String field, Filter filter) {
    return Condition.newBuilder()
        .setNested(NestedCondition.newBuilder().setKey(field).setFilter(filter))
        .build();
  }

  /**
   * Match records where the given field matches the given range.
   *
   * @param field The name of the nested field.
   * @param range The range to match.
   * @return a new instance of {@link Condition}
   */
  public static Condition range(String field, Range range) {
    return Condition.newBuilder()
        .setField(FieldCondition.newBuilder().setKey(field).setRange(range).build())
        .build();
  }

  /**
   * Match records where the given field has values inside a circle centred at a given latitude and
   * longitude with a given radius.
   *
   * @param field The name of the field.
   * @param latitude The latitude of the center.
   * @param longitude The longitude of the center.
   * @param radius The radius in meters.
   * @return a new instance of {@link Condition}
   */
  public static Condition geoRadius(String field, double latitude, double longitude, float radius) {
    return Condition.newBuilder()
        .setField(
            FieldCondition.newBuilder()
                .setKey(field)
                .setGeoRadius(
                    GeoRadius.newBuilder()
                        .setCenter(GeoPoint.newBuilder().setLat(latitude).setLon(longitude).build())
                        .setRadius(radius)
                        .build())
                .build())
        .build();
  }

  /**
   * Match records where the given field has values inside a bounding box specified by the top left
   * and bottom right coordinates.
   *
   * @param field The name of the field.
   * @param topLeftLatitude The latitude of the top left point.
   * @param topLeftLongitude The longitude of the top left point.
   * @param bottomRightLatitude The latitude of the bottom right point.
   * @param bottomRightLongitude The longitude of the bottom right point.
   * @return a new instance of {@link Condition}
   */
  public static Condition geoBoundingBox(
      String field,
      double topLeftLatitude,
      double topLeftLongitude,
      double bottomRightLatitude,
      double bottomRightLongitude) {
    return Condition.newBuilder()
        .setField(
            FieldCondition.newBuilder()
                .setKey(field)
                .setGeoBoundingBox(
                    GeoBoundingBox.newBuilder()
                        .setTopLeft(
                            GeoPoint.newBuilder()
                                .setLat(topLeftLatitude)
                                .setLon(topLeftLongitude)
                                .build())
                        .setBottomRight(
                            GeoPoint.newBuilder()
                                .setLat(bottomRightLatitude)
                                .setLon(bottomRightLongitude)
                                .build())
                        .build())
                .build())
        .build();
  }

  /**
   * Matches records where the given field has values inside the provided polygon. A polygon always
   * has an exterior ring and may optionally have interior rings, which represent independent areas
   * or holes. When defining a ring, you must pick either a clockwise or counterclockwise ordering
   * for your points. The first and last point of the polygon must be the same.
   *
   * @param field The name of the field.
   * @param exterior The exterior ring of the polygon.
   * @param interiors The interior rings of the polygon.
   * @return a new instance of {@link Condition}
   */
  public static Condition geoPolygon(
      String field, GeoLineString exterior, List<GeoLineString> interiors) {
    GeoPolygon.Builder geoPolygonBuilder = GeoPolygon.newBuilder().setExterior(exterior);

    if (!interiors.isEmpty()) {
      geoPolygonBuilder.addAllInteriors(interiors);
    }

    return Condition.newBuilder()
        .setField(
            FieldCondition.newBuilder()
                .setKey(field)
                .setGeoPolygon(geoPolygonBuilder.build())
                .build())
        .build();
  }

  /**
   * Matches records where the given field has a count of values within the specified count range
   *
   * @param field The name of the field.
   * @param valuesCount The count range to match.
   * @return a new instance of {@link Condition}
   */
  public static Condition valuesCount(String field, ValuesCount valuesCount) {
    return Condition.newBuilder()
        .setField(FieldCondition.newBuilder().setKey(field).setValuesCount(valuesCount).build())
        .build();
  }

  /**
   * Nests a filter
   *
   * @param filter The filter to nest.
   * @return a new instance of {@link Condition}
   */
  public static Condition filter(Filter filter) {
    return Condition.newBuilder().setFilter(filter).build();
  }

  /**
   * Matches records where the given field has a datetime value within the specified range
   *
   * @param field The name of the field.
   * @param datetimeRange The datetime range to match.
   * @return a new instance of {@link Condition}
   */
  public static Condition datetimeRange(String field, DatetimeRange datetimeRange) {
    return Condition.newBuilder()
        .setField(FieldCondition.newBuilder().setKey(field).setDatetimeRange(datetimeRange).build())
        .build();
  }

  /**
   * Matches records where a value for the given vector is present.
   *
   * @param vector The name of the vector.
   * @return a new instance of {@link Condition}
   */
  public static Condition hasVector(String vector) {
    return Condition.newBuilder()
        .setHasVector(HasVectorCondition.newBuilder().setHasVector(vector).build())
        .build();
  }
}
