package io.qdrant.client;

import io.qdrant.client.grpc.serverless.ServerlessCollections.CollectionConfig;
import io.qdrant.client.grpc.serverless.ServerlessCollections.DenseVectorConfig;

/** Convenience methods for constructing Qdrant Serverless {@link CollectionConfig}. */
public final class ServerlessCollectionConfigFactory {
  private ServerlessCollectionConfigFactory() {}

  /** Sets the unnamed vector, the one point operations use when no vector name is given. */
  public static CollectionConfig.Builder defaultVector(DenseVectorConfig vector) {
    return CollectionConfig.newBuilder().putDenseVectors("", vector);
  }
}
