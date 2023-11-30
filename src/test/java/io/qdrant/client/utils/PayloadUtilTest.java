package io.qdrant.client.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.qdrant.client.grpc.JsonWithInt.ListValue;
import io.qdrant.client.grpc.JsonWithInt.NullValue;
import io.qdrant.client.grpc.JsonWithInt.Struct;
import io.qdrant.client.grpc.JsonWithInt.Value;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PayloadUtilTest {

  @Test
  void testToPayloadStruct() {
    // Test case 1: Empty input map
    Map<String, Object> inputMap = new HashMap<>();
    Struct payloadStruct = PayloadUtil.toPayloadStruct(inputMap);
    assertTrue(payloadStruct.getFieldsMap().isEmpty());

    // Test case 2: Input map with different value types
    inputMap.put("name", "Elon");
    inputMap.put("age", 52);
    inputMap.put("isStudent", true);
    payloadStruct = PayloadUtil.toPayloadStruct(inputMap);
    assertEquals("Elon", payloadStruct.getFieldsMap().get("name").getStringValue());
    assertEquals(52, payloadStruct.getFieldsMap().get("age").getIntegerValue());
    assertEquals(true, payloadStruct.getFieldsMap().get("isStudent").getBoolValue());
  }

  @Test
  void testToPayload() {
    // Test case 1: Empty input map
    Map<String, Object> inputMap = new HashMap<>();
    Map<String, Value> payload = PayloadUtil.toPayload(inputMap);
    assertTrue(payload.isEmpty());

    // Test case 2: Input map with different value types
    inputMap.put("name", "Elon");
    inputMap.put("age", 52);
    inputMap.put("isStudent", true);
    payload = PayloadUtil.toPayload(inputMap);
    assertEquals("Elon", payload.get("name").getStringValue());
    assertEquals(52, payload.get("age").getIntegerValue());
    assertEquals(true, payload.get("isStudent").getBoolValue());
  }

  @Test
  void testStructToHashMap() {
    // Test case 1: Empty struct
    Struct.Builder structBuilder = Struct.newBuilder();
    Struct struct = structBuilder.build();
    Map<String, Object> structMap = PayloadUtil.payloadStructToHashMap(struct);
    assertTrue(structMap.isEmpty());

    // Test case 2: Struct with different value types
    structBuilder.putFields("name", Value.newBuilder().setStringValue("Elon").build());
    structBuilder.putFields("age", Value.newBuilder().setIntegerValue(52).build());
    structBuilder.putFields("isStudent", Value.newBuilder().setBoolValue(true).build());
    struct = structBuilder.build();
    structMap = PayloadUtil.payloadStructToHashMap(struct);
    assertEquals("Elon", structMap.get("name"));
    assertEquals(52, (int) structMap.get("age"));
    assertEquals(true, structMap.get("isStudent"));
  }

  @Test
  void testToHashMap() {
    // Test case 1: Empty payload
    Map<String, Value> payload = new HashMap<>();
    Map<String, Object> hashMap = PayloadUtil.toHashMap(payload);
    assertTrue(hashMap.isEmpty());

    // Test case 2: Payload with different value types
    payload.put("name", Value.newBuilder().setStringValue("Elon").build());
    payload.put("age", Value.newBuilder().setIntegerValue(52).build());
    payload.put("isStudent", Value.newBuilder().setBoolValue(true).build());
    hashMap = PayloadUtil.toHashMap(payload);
    assertEquals("Elon", hashMap.get("name"));
    assertEquals(52, hashMap.get("age"));
    assertEquals(true, hashMap.get("isStudent"));
  }

  @Test
  void testSetValue() {
    // Test case 1: Set string value
    Value.Builder valueBuilder = Value.newBuilder();
    PayloadUtil.setValue(valueBuilder, "Elon");
    assertEquals("Elon", valueBuilder.getStringValue());

    // Test case 2: Set integer value
    valueBuilder = Value.newBuilder();
    PayloadUtil.setValue(valueBuilder, 52);
    assertEquals(52, valueBuilder.getIntegerValue());

    // Test case 3: Set boolean value
    valueBuilder = Value.newBuilder();
    PayloadUtil.setValue(valueBuilder, true);
    assertEquals(true, valueBuilder.getBoolValue());
  }

  @Test
  void testListToListValue() {
    // Test case 1: Empty list
    List<Object> list = new ArrayList<>();
    ListValue.Builder listValueBuilder = PayloadUtil.listToListValue(list);
    assertTrue(listValueBuilder.getValuesList().isEmpty());

    // Test case 2: List with different value types
    list.add("Elon");
    list.add(52);
    list.add(true);
    listValueBuilder = PayloadUtil.listToListValue(list);
    assertEquals("Elon", listValueBuilder.getValuesList().get(0).getStringValue());
    assertEquals(52, listValueBuilder.getValuesList().get(1).getIntegerValue());
    assertEquals(true, listValueBuilder.getValuesList().get(2).getBoolValue());
  }

  @Test
  void testListValueToList() {
    // Test case 1: Empty list value
    ListValue.Builder listValueBuilder = ListValue.newBuilder();
    ListValue listValue = listValueBuilder.build();
    Object[] objectList = (Object[]) PayloadUtil.listValueToList(listValue);
    assertTrue(objectList.length == 0);

    // Test case 2: List value with different value types
    listValueBuilder.addValues(Value.newBuilder().setStringValue("Elon").build());
    listValueBuilder.addValues(Value.newBuilder().setIntegerValue(52).build());
    listValueBuilder.addValues(Value.newBuilder().setBoolValue(true).build());
    listValue = listValueBuilder.build();
    objectList = (Object[]) PayloadUtil.listValueToList(listValue);
    assert (objectList instanceof Object[]);

    assertEquals("Elon", objectList[0]);
    assertEquals(52, objectList[1]);
    assertEquals(true, objectList[2]);
  }

  @Test
  void testValueToObject() {
    // Test case 1: String value
    Value value = Value.newBuilder().setStringValue("Elon").build();
    Object object = PayloadUtil.valueToObject(value);
    assertEquals("Elon", object);

    // Test case 2: Integer value
    value = Value.newBuilder().setIntegerValue(52).build();
    object = PayloadUtil.valueToObject(value);
    assertEquals(52, object);

    // Test case 3: Boolean value
    value = Value.newBuilder().setBoolValue(true).build();
    object = PayloadUtil.valueToObject(value);
    assertEquals(true, object);

    // Test case 4: Null value
    value = Value.newBuilder().setNullValue(NullValue.NULL_VALUE).build();
    object = PayloadUtil.valueToObject(value);
    assertNull(object);
  }
}
