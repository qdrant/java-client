package io.qdrant.client.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.qdrant.client.grpc.Collections.ShardKey;

import org.junit.jupiter.api.Test;

class CollectionUtilTest {

  @Test
  void testShardKeyWithString() {
    String keyword = "somethinKeySomething";
    ShardKey shardKey = CollectionUtil.shardKey(keyword);
    assertNotNull(shardKey);
    assertEquals(keyword, shardKey.getKeyword());
  }

  @Test
  void testShardKeyWithLong() {
    long number = 12345L;
    ShardKey shardKey = CollectionUtil.shardKey(number);
    assertNotNull(shardKey);
    assertEquals(number, shardKey.getNumber());
  }
}