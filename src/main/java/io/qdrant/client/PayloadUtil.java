package io.qdrant.client;

import static io.qdrant.client.grpc.JsonWithInt.ListValue;
import static io.qdrant.client.grpc.JsonWithInt.NullValue;
import static io.qdrant.client.grpc.JsonWithInt.Struct;
import static io.qdrant.client.grpc.JsonWithInt.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for working with Qdrant Payloads.
 */
public class PayloadUtil {

  private PayloadUtil () {
  }

  /**
   * Converts a map to a payload map.
   *
   * @param inputMap The input map to convert.
   * @return The converted payload map.
   */
  public static Map<String, Value> toPayload(Map<String, Object> inputMap) {
    Map<String, Value> map = new HashMap<>();
    for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
      String fieldName = entry.getKey();
      Object value = entry.getValue();

      Value.Builder valueBuilder = Value.newBuilder();
      setValue(valueBuilder, value);
      map.put(fieldName, valueBuilder.build());
    }
    return map;
  }

  /**
   * Converts a map to a payload struct.
   *
   * @param inputMap The input map to convert.
   * @return The converted payload struct.
   */
  public static Struct toPayloadStruct(Map<String, Object> inputMap) {
    Struct.Builder structBuilder = Struct.newBuilder();
    Map<String, Value> map = toPayload(inputMap);
    structBuilder.putAllFields(map);
    return structBuilder.build();
  }

  /**
   * Converts a payload struct to a Java Map.
   *
   * @param struct The payload struct to convert.
   * @return The converted hash map.
   */
  public static Map<String, Object> toMap(Struct struct) {
    Map<String, Object> structMap = toMap(struct.getFieldsMap());
    return structMap;
  }

  /**
   * Converts a payload map to a Java Map.
   *
   * @param payload The payload map to convert.
   * @return The converted hash map.
   */
  public static Map<String, Object> toMap(Map<String, Value> payload) {
    Map<String, Object> hashMap = new HashMap<>();
    for (Map.Entry<String, Value> entry : payload.entrySet()) {
      String fieldName = entry.getKey();
      Value fieldValue = entry.getValue();

      Object value = valueToObject(fieldValue);
      hashMap.put(fieldName, value);
    }
    return hashMap;
  }

  /**
   * Sets the value of a Value.Builder based on the given object.
   *
   * @param valueBuilder The Value.Builder to set the value for.
   * @param value The object value to set.
   */
  static void setValue(Value.Builder valueBuilder, Object value) {
    if (value == null) {
      valueBuilder.setNullValue(NullValue.NULL_VALUE);
    } else if (value instanceof String) {
      valueBuilder.setStringValue((String) value);
    } else if (value instanceof Integer) {
      valueBuilder.setIntegerValue((Integer) value);
    } else if (value instanceof Double) {
      valueBuilder.setDoubleValue((Double) value);
    } else if (value instanceof Boolean) {
      valueBuilder.setBoolValue((Boolean) value);
    } else if (value instanceof Map) {
      valueBuilder.setStructValue(toPayloadStruct((Map<String, Object>) value));
    } else if (value instanceof List) {
      valueBuilder.setListValue(listToListValue((List<Object>) value));
    }
  }

  /**
   * Converts a list to a ListValue.Builder.
   *
   * @param list The list to convert.
   * @return The converted ListValue.Builder.
   */
  static ListValue.Builder listToListValue(List<Object> list) {
    ListValue.Builder listValueBuilder = ListValue.newBuilder();

    for (Object element : list) {
      Value.Builder elementBuilder = Value.newBuilder();
      setValue(elementBuilder, element);
      listValueBuilder.addValues(elementBuilder.build());
    }

    return listValueBuilder;
  }

  /**
   * Converts a ListValue to an array of objects.
   *
   * @param listValue The ListValue to convert.
   * @return The converted array of objects.
   */
  static Object listValueToList(ListValue listValue) {
    return listValue.getValuesList().stream().map(PayloadUtil::valueToObject).toArray();
  }

  /**
   * Converts a Value to an object.
   *
   * @param value The Value to convert.
   * @return The converted object.
   */
  static Object valueToObject(Value value) {
    if (value.hasStringValue()) {
      return value.getStringValue();
    } else if (value.hasIntegerValue()) {
      // int64 is converted to long
      // We need to cast it to int
      return (int) value.getIntegerValue();
    } else if (value.hasDoubleValue()) {
      return value.getDoubleValue();
    } else if (value.hasBoolValue()) {
      return value.getBoolValue();
    } else if (value.hasNullValue()) {
      return null;
    } else if (value.hasStructValue()) {
      return toMap(value.getStructValue());
    } else if (value.hasListValue()) {
      return listValueToList(value.getListValue());
    }

    return null;
  }
}