package io.qdrant.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.MalformedURLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class QdrantClientTest {

  private static QdrantClient qdrantClient;

  @BeforeAll
  static void setUp() throws Exception {
    String qdrantUrl = System.getenv("QDRANT_URL");
    String apiKey = System.getenv("QDRANT_API_KEY");

    if (qdrantUrl == null) {
      qdrantUrl = "http://localhost:6334";
    }

    if (apiKey == null) {
      qdrantClient = new QdrantClient(qdrantUrl);
    } else {
      qdrantClient = new QdrantClient(qdrantUrl, apiKey);
    }
  }

  @Test
  void testInvalidProtocol() {
    assertThrows(IllegalArgumentException.class, () -> new QdrantClient("ftp://localhost:6334"));
  }

  @Test
  void testMalformedUrl() {
    assertThrows(MalformedURLException.class, () -> new QdrantClient("qdrant/qdrant:latest"));
  }

  @Test
  void testHealthCheck() {
    assertEquals(qdrantClient.healthCheck().getTitle(), "qdrant - vector search engine");
  }
}
