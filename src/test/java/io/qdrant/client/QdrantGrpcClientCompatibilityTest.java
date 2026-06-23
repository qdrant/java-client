package io.qdrant.client;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.grpc.CallCredentials;
import io.grpc.ManagedChannel;
import io.qdrant.client.grpc.QdrantGrpc;
import io.qdrant.client.grpc.QdrantOuterClass;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

/**
 * Unit tests for the version compatibility check performed by {@link
 * QdrantGrpcClient.Builder#build()}.
 */
class QdrantGrpcClientCompatibilityTest {

  @Test
  void compatibility_check_uses_api_key_credentials_from_with_api_key() {
    ManagedChannel channel = mock(ManagedChannel.class);
    QdrantGrpc.QdrantBlockingStub stub = mock(QdrantGrpc.QdrantBlockingStub.class);
    QdrantOuterClass.HealthCheckReply reply =
        QdrantOuterClass.HealthCheckReply.newBuilder()
            .setTitle("qdrant")
            .setVersion("1.0.0")
            .build();

    try (MockedStatic<QdrantGrpc> grpc = mockStatic(QdrantGrpc.class)) {
      grpc.when(() -> QdrantGrpc.newBlockingStub(any())).thenReturn(stub);
      when(stub.withCallCredentials(any())).thenReturn(stub);
      when(stub.healthCheck(any())).thenReturn(reply);

      QdrantGrpcClient client =
          QdrantGrpcClient.newBuilder(channel, false, true).withApiKey("my-api-key").build();
      client.close();

      ArgumentCaptor<CallCredentials> credentialsCaptor =
          ArgumentCaptor.forClass(CallCredentials.class);
      verify(stub).withCallCredentials(credentialsCaptor.capture());

      CallCredentials usedCredentials = credentialsCaptor.getValue();
      assertNotNull(
          usedCredentials,
          "Version compatibility check must run with the API key credentials, not null");
      assertInstanceOf(MetadataCredentials.class, usedCredentials);
    }
  }
}
