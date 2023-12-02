package io.qdrant.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.grpc.StatusRuntimeException;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Collections.Distance;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class QdrantClientCollectionTest {

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
  void testAliasOperations() {
    String collectionName = UUID.randomUUID().toString();
    String aliasName = UUID.randomUUID().toString();

    assertThrows(
        StatusRuntimeException.class,
        () -> {
          // This should fail as collection does not exist
          qdrantClient.createAlias(collectionName, aliasName);
        });

    qdrantClient.createCollection(collectionName, 6, Distance.Euclid);
    assertDoesNotThrow(
        () -> {
          Collections.CollectionOperationResponse response =
              qdrantClient.createAlias(collectionName, aliasName);
          assertTrue(response.getResult());
        });

    Collections.ListAliasesResponse response = qdrantClient.listAliases();
    assertTrue(response.getAliasesCount() == 1);
    Collections.AliasDescription alias = response.getAliasesList().get(0);
    assertTrue(alias.getCollectionName().equals(collectionName));
    assertTrue(alias.getAliasName().equals(aliasName));

    String newAliasName = UUID.randomUUID().toString();
    qdrantClient.renameAlias(aliasName, newAliasName);
    response = qdrantClient.listAliases();
    assertTrue(response.getAliasesCount() == 1);

    alias = response.getAliasesList().get(0);
    assertTrue(alias.getCollectionName().equals(collectionName));
    assertTrue(alias.getAliasName().equals(newAliasName));

    qdrantClient.deleteAlias(newAliasName);
    response = qdrantClient.listAliases();
  }

  @Test
  void testListCollections() {
    assertDoesNotThrow(
        () -> {
          Collections.ListCollectionsResponse response = qdrantClient.listCollections();
          assertTrue(response.getCollectionsCount() >= 0);
        });
  }

  @Test
  void testHasCollection() {
    String collectionName = UUID.randomUUID().toString();
    boolean exists = qdrantClient.hasCollection(collectionName);
    assertFalse(exists);

    qdrantClient.createCollection(collectionName, 6, Distance.Euclid);

    exists = qdrantClient.hasCollection(collectionName);
    assertTrue(exists);
  }

  @Test
  void testCollectionConfigOperations() {
    long vectorSize = 128;
    String collectionName = UUID.randomUUID().toString();
    Collections.Distance distance = Collections.Distance.Cosine;

    Collections.VectorParams.Builder params =
        Collections.VectorParams.newBuilder().setDistance(distance).setSize(vectorSize);

    Collections.VectorsConfig config =
        Collections.VectorsConfig.newBuilder().setParams(params).build();

    Collections.HnswConfigDiff hnsw =
        Collections.HnswConfigDiff.newBuilder().setM(16).setEfConstruct(200).build();

    Collections.CreateCollection details =
        Collections.CreateCollection.newBuilder()
            .setVectorsConfig(config)
            .setHnswConfig(hnsw)
            .setCollectionName(collectionName)
            .build();

    Collections.CollectionOperationResponse response = qdrantClient.createCollection(details);
    assertTrue(response.getResult());

    Collections.GetCollectionInfoResponse info = qdrantClient.getCollectionInfo(collectionName);
    assertTrue(info.getResult().getConfig().getHnswConfig().getM() == 16);

    Collections.UpdateCollection updateCollection =
        Collections.UpdateCollection.newBuilder()
            .setCollectionName(collectionName)
            .setHnswConfig(Collections.HnswConfigDiff.newBuilder().setM(32).build())
            .build();
    qdrantClient.updateCollection(updateCollection);

    info = qdrantClient.getCollectionInfo(collectionName);
    assertTrue(info.getResult().getConfig().getHnswConfig().getM() == 32);
  }

  @Test
  void testRecreateCollection() {
    String collectionName = UUID.randomUUID().toString();

    qdrantClient.createCollection(collectionName, 6, Distance.Euclid);
    assertDoesNotThrow(
        () -> {
          qdrantClient.recreateCollection(collectionName, 12, Distance.Dot);
        });
  }

  @Test
  void testDeleteCollection() {
    String collectionName = UUID.randomUUID().toString();

    Collections.CollectionOperationResponse response =
        qdrantClient.deleteCollection(collectionName);
    assertFalse(response.getResult());

    qdrantClient.createCollection(collectionName, 6, Distance.Euclid);

    response = qdrantClient.deleteCollection(collectionName);
    assertTrue(response.getResult());
  }
}
