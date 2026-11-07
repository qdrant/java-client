package io.qdrant.client;

import static io.qdrant.client.ConditionFactory.matchKeyword;
import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.QueryFactory.nearest;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.grpc.Status;
import io.qdrant.client.grpc.Common.Filter;
import io.qdrant.client.grpc.Points.CountPoints;
import io.qdrant.client.grpc.Points.DeletePoints;
import io.qdrant.client.grpc.Points.GetPoints;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.PointsIdsList;
import io.qdrant.client.grpc.Points.PointsSelector;
import io.qdrant.client.grpc.Points.QueryPoints;
import io.qdrant.client.grpc.Points.RetrievedPoint;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SetPayloadPoints;
import io.qdrant.client.grpc.Points.UpdateResult;
import io.qdrant.client.grpc.Points.UpdateStatus;
import io.qdrant.client.grpc.Points.UpsertPoints;
import io.qdrant.client.grpc.serverless.ServerlessCollections.CollectionConfig;
import io.qdrant.client.grpc.serverless.ServerlessCollections.CollectionSummary;
import io.qdrant.client.grpc.serverless.ServerlessCollections.DenseVectorConfig;
import io.qdrant.client.grpc.serverless.ServerlessCollections.Distance;
import io.qdrant.client.grpc.serverless.ServerlessCollections.GetCollectionResponse;
import io.qdrant.client.grpc.serverless.ServerlessCollections.IntegerIndex;
import io.qdrant.client.grpc.serverless.ServerlessCollections.ListCollectionsRequest;
import io.qdrant.client.grpc.serverless.ServerlessCollections.ListCollectionsResponse;
import io.qdrant.client.grpc.serverless.ServerlessCollections.PrecisionTier;
import io.qdrant.client.grpc.serverless.ServerlessCollections.SparseVectorConfig;
import io.qdrant.client.grpc.serverless.ServerlessCollections.StopwordsSet;
import io.qdrant.client.grpc.serverless.ServerlessCollections.TextIndex;
import io.qdrant.client.grpc.serverless.ServerlessCollections.Tokenizer;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Runs against the serverless space at QDRANT_SERVERLESS_URL. Skipped if it is not configured. */
class QdrantServerlessClientTest {
  private QdrantServerlessClient client;
  private String collectionName;

  @BeforeEach
  void setup() {
    String url = System.getenv("QDRANT_SERVERLESS_URL");
    String apiKey = System.getenv("QDRANT_SERVERLESS_API_KEY");
    // CI sets unavailable secrets to empty strings, e.g. on pull requests from forks.
    assumeTrue(
        url != null && !url.isEmpty() && apiKey != null && !apiKey.isEmpty(),
        "QDRANT_SERVERLESS_URL and QDRANT_SERVERLESS_API_KEY are not set");
    client =
        new QdrantServerlessClient(
            QdrantServerlessClient.newGrpcClientBuilder(URI.create(url).getHost())
                .withApiKey(apiKey)
                .withTimeout(Duration.ofSeconds(30))
                .build());
    collectionName = "java-client-test-" + System.nanoTime();
  }

  @AfterEach
  void teardown() throws Exception {
    if (client != null) {
      try {
        client.deleteCollectionAsync(collectionName).get();
      } finally {
        client.close();
      }
    }
  }

  @Test
  void collections() throws Exception {
    CollectionConfig config =
        CollectionConfig.newBuilder()
            .putDenseVectors(
                "",
                DenseVectorConfig.newBuilder()
                    .setSize(4)
                    .setDistance(Distance.COSINE)
                    .setPrecisionTier(PrecisionTier.FULL)
                    .build())
            .putSparseVectors("bm25", SparseVectorConfig.newBuilder().setUseIdf(true).build())
            .putPayloadIndexes("color", ServerlessPayloadIndexFactory.keyword())
            .putPayloadIndexes(
                "age",
                ServerlessPayloadIndexFactory.integer(
                    IntegerIndex.newBuilder().setRange(false).build()))
            .putPayloadIndexes("price", ServerlessPayloadIndexFactory.floatIndex())
            .putPayloadIndexes("user_id", ServerlessPayloadIndexFactory.uuid())
            .putPayloadIndexes("created", ServerlessPayloadIndexFactory.datetime())
            .putPayloadIndexes("geo", ServerlessPayloadIndexFactory.geo())
            .putPayloadIndexes("active", ServerlessPayloadIndexFactory.bool())
            .putPayloadIndexes(
                "title",
                ServerlessPayloadIndexFactory.text(
                    TextIndex.newBuilder()
                        .setTokenizer(Tokenizer.WORD)
                        .setStopwords(StopwordsSet.newBuilder().addLanguages("english"))
                        .setStemmer(ServerlessPayloadIndexFactory.snowballStemmer("english"))
                        .build()))
            .putPayloadIndexes(
                "body",
                ServerlessPayloadIndexFactory.text(
                    TextIndex.newBuilder()
                        .setStemmer(ServerlessPayloadIndexFactory.disabledStemmer())
                        .build()))
            .build();

    assertEquals("created", client.createCollectionAsync(collectionName, config).get().getResult());

    // The API may report an existing collection as an error or through the result.
    try {
      String result = client.createCollectionAsync(collectionName, config).get().getResult();
      assertNotEquals("created", result);
    } catch (ExecutionException e) {
      assertEquals(Status.Code.ALREADY_EXISTS, Status.fromThrowable(e.getCause()).getCode());
    }

    GetCollectionResponse info = client.getCollectionAsync(collectionName).get();
    assertTrue(info.getExists());
    assertEquals(config, info.getConfig());
    assertTrue(client.collectionExistsAsync(collectionName).get());
    assertFalse(client.getCollectionAsync(collectionName + "-missing").get().getExists());

    List<String> names = new ArrayList<>();
    ListCollectionsRequest.Builder request = ListCollectionsRequest.newBuilder().setLimit(100);
    ListCollectionsResponse page;
    do {
      page = client.listCollectionsAsync(request.build()).get();
      for (CollectionSummary summary : page.getCollectionsList()) {
        names.add(summary.getCollectionName());
      }
      request.setOffsetToken(page.getNextOffsetToken());
    } while (page.hasNextOffsetToken());
    assertTrue(names.contains(collectionName));
  }

  @Test
  void points() throws Exception {
    client
        .createCollectionAsync(
            collectionName,
            CollectionConfig.newBuilder()
                .putDenseVectors(
                    "", DenseVectorConfig.newBuilder().setSize(4).setDistance(Distance.DOT).build())
                .putPayloadIndexes("color", ServerlessPayloadIndexFactory.keyword())
                .build())
        .get();

    client
        .upsertAsync(
            UpsertPoints.newBuilder()
                .setCollectionName(collectionName)
                .setWait(true)
                .addPoints(
                    PointStruct.newBuilder()
                        .setId(id(1))
                        .setVectors(vectors(1, 0, 0, 0))
                        .putPayload("color", value("red")))
                .addPoints(
                    PointStruct.newBuilder()
                        .setId(id(2))
                        .setVectors(vectors(0, 1, 0, 0))
                        .putPayload("color", value("blue")))
                .build())
        .get();

    // Reads can lag a few seconds behind writes.
    eventually(
        () ->
            client
                    .countAsync(
                        CountPoints.newBuilder()
                            .setCollectionName(collectionName)
                            .setExact(true)
                            .setFilter(Filter.newBuilder().addMust(matchKeyword("color", "red")))
                            .build())
                    .get()
                == 1);

    eventually(
        () -> {
          List<ScoredPoint> hits =
              client
                  .queryAsync(
                      QueryPoints.newBuilder()
                          .setCollectionName(collectionName)
                          .setQuery(nearest(1, 0, 0, 0))
                          .build())
                  .get();
          return hits.size() == 2 && hits.get(0).getId().getNum() == 1;
        });

    PointsSelector first =
        PointsSelector.newBuilder().setPoints(PointsIdsList.newBuilder().addIds(id(1))).build();
    UpdateResult setPayload =
        client
            .setPayloadAsync(
                SetPayloadPoints.newBuilder()
                    .setCollectionName(collectionName)
                    .setWait(true)
                    .setPointsSelector(first)
                    .putPayload("size", value(10))
                    .build())
            .get();
    assertEquals(UpdateStatus.Completed, setPayload.getStatus());

    List<RetrievedPoint> points =
        client
            .retrieveAsync(
                GetPoints.newBuilder()
                    .setCollectionName(collectionName)
                    .addIds(id(1))
                    .setWithPayload(WithPayloadSelectorFactory.enable(true))
                    .build())
            .get();
    assertEquals(1, points.size());

    UpdateResult delete =
        client
            .deleteAsync(
                DeletePoints.newBuilder()
                    .setCollectionName(collectionName)
                    .setWait(true)
                    .setPoints(
                        PointsSelector.newBuilder()
                            .setPoints(PointsIdsList.newBuilder().addIds(id(2))))
                    .build())
            .get();
    assertEquals(UpdateStatus.Completed, delete.getStatus());
  }

  // Retries on false and on errors, e.g. while a new collection is not readable yet.
  private static void eventually(Callable<Boolean> condition) throws Exception {
    long deadline = System.nanoTime() + Duration.ofMinutes(1).toNanos();
    while (true) {
      try {
        if (condition.call()) {
          return;
        }
        if (System.nanoTime() >= deadline) {
          fail("condition not met within 1 minute");
        }
      } catch (Exception e) {
        if (System.nanoTime() >= deadline) {
          throw e;
        }
      }
      Thread.sleep(250);
    }
  }
}
