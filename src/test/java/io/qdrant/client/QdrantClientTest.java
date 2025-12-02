package io.qdrant.client;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.qdrant.QdrantContainer;

@Testcontainers
class QdrantClientTest {

  @Container
  private static final QdrantContainer QDRANT_CONTAINER =
      new QdrantContainer(DockerImage.QDRANT_IMAGE);

  private QdrantClient client;

  @BeforeEach
  public void setup() {
    ManagedChannel channel =
        Grpc.newChannelBuilder(
                QDRANT_CONTAINER.getGrpcHostAddress(), InsecureChannelCredentials.create())
            .build();
    QdrantGrpcClient grpcClient = QdrantGrpcClient.newBuilder(channel, true).build();
    client = new QdrantClient(grpcClient);
  }

  @AfterEach
  public void teardown() {
    client.close();
  }

  @Test
  void canAccessChannelOnGrpcClient() {
    Assertions.assertTrue(client.grpcClient().channel().authority().startsWith("localhost"));
  }

  @Test
  void connectionPoolingCreatesMultipleConnections() {
    String host = QDRANT_CONTAINER.getHost();
    int port = QDRANT_CONTAINER.getGrpcPort();

    QdrantClient pooledClient = new QdrantClient(host, port, false, 3);

    try {
      QdrantGrpcClient client1 = pooledClient.grpcClient();
      QdrantGrpcClient client2 = pooledClient.grpcClient();
      QdrantGrpcClient client3 = pooledClient.grpcClient();
      QdrantGrpcClient client4 = pooledClient.grpcClient(); // Should wrap around to first

      Assertions.assertSame(client1, client4); // Should wrap around to first client

      // Verify that different clients have different channels (true connection pooling)
      Assertions.assertNotSame(client1.channel(), client2.channel());
      Assertions.assertNotSame(client2.channel(), client3.channel());
    } finally {
      pooledClient.close();
    }
  }

  @Test
  void defaultConnectionPoolingWorks() {
    String host = QDRANT_CONTAINER.getHost();
    int port = QDRANT_CONTAINER.getGrpcPort();
    QdrantClient defaultClient = new QdrantClient(host, port, false);

    try {
      Assertions.assertNotNull(defaultClient.grpcClient());
    } finally {
      defaultClient.close();
    }
  }
}
