package io.qdrant.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.SnapshotsService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class QdrantClientSnapshotsTest {

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
  void testCollectionSnapshots() {
    String collectionName = UUID.randomUUID().toString();

    qdrantClient.createCollection(collectionName, 768, Distance.Cosine);

    assertEquals(
        qdrantClient.listSnapshots(collectionName).getSnapshotDescriptionsList().size(), 0);

    assertDoesNotThrow(
        () -> {
          SnapshotsService.CreateSnapshotResponse response =
              qdrantClient.createSnapshot(collectionName);
          String snapshotName = response.getSnapshotDescription().getName();

          assertEquals(
              qdrantClient.listSnapshots(collectionName).getSnapshotDescriptionsList().size(), 1);

          qdrantClient.deleteSnapshot(collectionName, snapshotName);

          assertEquals(
              qdrantClient.listSnapshots(collectionName).getSnapshotDescriptionsList().size(), 0);
        });
  }

  @Test
  void testFullSnapshots() {

    assertEquals(qdrantClient.listFullSnapshots().getSnapshotDescriptionsList().size(), 0);

    assertDoesNotThrow(
        () -> {
          SnapshotsService.CreateSnapshotResponse response = qdrantClient.createFullSnapshot();
          String snapshotName = response.getSnapshotDescription().getName();

          assertEquals(qdrantClient.listFullSnapshots().getSnapshotDescriptionsList().size(), 1);

          qdrantClient.deleteFullSnapshot(snapshotName);

          assertEquals(qdrantClient.listFullSnapshots().getSnapshotDescriptionsList().size(), 0);
        });
  }
}
