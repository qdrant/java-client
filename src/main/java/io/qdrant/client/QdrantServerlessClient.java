package io.qdrant.client;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.qdrant.client.grpc.Points.BatchResult;
import io.qdrant.client.grpc.Points.ClearPayloadPoints;
import io.qdrant.client.grpc.Points.CountPoints;
import io.qdrant.client.grpc.Points.DeletePayloadPoints;
import io.qdrant.client.grpc.Points.DeletePointVectors;
import io.qdrant.client.grpc.Points.DeletePoints;
import io.qdrant.client.grpc.Points.GetPoints;
import io.qdrant.client.grpc.Points.PointGroup;
import io.qdrant.client.grpc.Points.QueryBatchPoints;
import io.qdrant.client.grpc.Points.QueryPointGroups;
import io.qdrant.client.grpc.Points.QueryPoints;
import io.qdrant.client.grpc.Points.RetrievedPoint;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.ScrollPoints;
import io.qdrant.client.grpc.Points.ScrollResponse;
import io.qdrant.client.grpc.Points.SetPayloadPoints;
import io.qdrant.client.grpc.Points.UpdateBatchPoints;
import io.qdrant.client.grpc.Points.UpdatePointVectors;
import io.qdrant.client.grpc.Points.UpdateResult;
import io.qdrant.client.grpc.Points.UpsertPoints;
import io.qdrant.client.grpc.serverless.CollectionsServiceGrpc.CollectionsServiceFutureStub;
import io.qdrant.client.grpc.serverless.ServerlessCollections.CollectionConfig;
import io.qdrant.client.grpc.serverless.ServerlessCollections.CreateCollectionRequest;
import io.qdrant.client.grpc.serverless.ServerlessCollections.CreateCollectionResponse;
import io.qdrant.client.grpc.serverless.ServerlessCollections.DeleteCollectionRequest;
import io.qdrant.client.grpc.serverless.ServerlessCollections.DeleteCollectionResponse;
import io.qdrant.client.grpc.serverless.ServerlessCollections.GetCollectionRequest;
import io.qdrant.client.grpc.serverless.ServerlessCollections.GetCollectionResponse;
import io.qdrant.client.grpc.serverless.ServerlessCollections.ListCollectionsRequest;
import io.qdrant.client.grpc.serverless.ServerlessCollections.ListCollectionsResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for Qdrant Serverless. Experimental: the API may change without notice.
 *
 * <p>Point operations take the same requests as {@link QdrantClient}.
 */
public class QdrantServerlessClient implements AutoCloseable {
  private static final Logger logger = LoggerFactory.getLogger(QdrantServerlessClient.class);

  private static final int GRPC_PORT = 443;

  private final QdrantGrpcClient grpcClient;
  private final QdrantClient points;

  /**
   * Creates a new instance of {@link QdrantServerlessClient}. Build the gRPC client with {@link
   * #newGrpcClientBuilder(String)}: serverless has no server version to check compatibility with.
   *
   * @param grpcClient The low-level gRPC client to use.
   */
  public QdrantServerlessClient(QdrantGrpcClient grpcClient) {
    this.grpcClient = grpcClient;
    this.points = new QdrantClient(grpcClient);
  }

  /**
   * Creates a builder for a gRPC client connecting to Qdrant Serverless over TLS on port 443,
   * without the server version compatibility check.
   *
   * @param host The host of the serverless space.
   * @return a new instance of {@link QdrantGrpcClient.Builder}
   */
  public static QdrantGrpcClient.Builder newGrpcClientBuilder(String host) {
    return QdrantGrpcClient.newBuilder(host, GRPC_PORT, true, false);
  }

  /**
   * Gets the low-level gRPC client.
   *
   * @return The low-level gRPC client
   */
  public QdrantGrpcClient grpcClient() {
    return grpcClient;
  }

  // region Collections

  /**
   * Creates a collection. An existing collection fails with {@code ALREADY_EXISTS}, or is reported
   * in the response's {@code result}.
   *
   * @param collectionName The name of the collection.
   * @param config The configuration of the collection.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CreateCollectionResponse> createCollectionAsync(
      String collectionName, CollectionConfig config) {
    return createCollectionAsync(collectionName, config, null);
  }

  /**
   * Creates a collection. An existing collection fails with {@code ALREADY_EXISTS}, or is reported
   * in the response's {@code result}.
   *
   * @param collectionName The name of the collection.
   * @param config The configuration of the collection.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CreateCollectionResponse> createCollectionAsync(
      String collectionName, CollectionConfig config, @Nullable Duration timeout) {
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");
    logger.debug("Create collection '{}'", collectionName);
    ListenableFuture<CreateCollectionResponse> future =
        getCollections(timeout)
            .createCollection(
                CreateCollectionRequest.newBuilder()
                    .setCollectionName(collectionName)
                    .setConfig(config)
                    .build());
    QdrantClient.addLogFailureCallback(logger, future, "Create collection");
    return future;
  }

  /**
   * Deletes a collection and all of its data.
   *
   * @param collectionName The name of the collection.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<DeleteCollectionResponse> deleteCollectionAsync(String collectionName) {
    return deleteCollectionAsync(collectionName, null);
  }

  /**
   * Deletes a collection and all of its data.
   *
   * @param collectionName The name of the collection.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<DeleteCollectionResponse> deleteCollectionAsync(
      String collectionName, @Nullable Duration timeout) {
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");
    logger.debug("Delete collection '{}'", collectionName);
    ListenableFuture<DeleteCollectionResponse> future =
        getCollections(timeout)
            .deleteCollection(
                DeleteCollectionRequest.newBuilder().setCollectionName(collectionName).build());
    QdrantClient.addLogFailureCallback(logger, future, "Delete collection");
    return future;
  }

  /**
   * Gets a collection's configuration and point count. A missing collection is reported by {@link
   * GetCollectionResponse#getExists()}, not as an error.
   *
   * @param collectionName The name of the collection.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<GetCollectionResponse> getCollectionAsync(String collectionName) {
    return getCollectionAsync(collectionName, null);
  }

  /**
   * Gets a collection's configuration and point count. A missing collection is reported by {@link
   * GetCollectionResponse#getExists()}, not as an error.
   *
   * @param collectionName The name of the collection.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<GetCollectionResponse> getCollectionAsync(
      String collectionName, @Nullable Duration timeout) {
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");
    logger.debug("Get collection info for '{}'", collectionName);
    ListenableFuture<GetCollectionResponse> future =
        getCollections(timeout)
            .getCollection(
                GetCollectionRequest.newBuilder().setCollectionName(collectionName).build());
    QdrantClient.addLogFailureCallback(logger, future, "Get collection info");
    return future;
  }

  /**
   * Checks whether a collection exists.
   *
   * @param collectionName The name of the collection.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<Boolean> collectionExistsAsync(String collectionName) {
    return collectionExistsAsync(collectionName, null);
  }

  /**
   * Checks whether a collection exists.
   *
   * @param collectionName The name of the collection.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<Boolean> collectionExistsAsync(
      String collectionName, @Nullable Duration timeout) {
    return Futures.transform(
        getCollectionAsync(collectionName, timeout),
        GetCollectionResponse::getExists,
        MoreExecutors.directExecutor());
  }

  /**
   * Lists the first page of collections.
   *
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<ListCollectionsResponse> listCollectionsAsync() {
    return listCollectionsAsync(ListCollectionsRequest.getDefaultInstance(), null);
  }

  /**
   * Lists a page of collections. Pass {@link ListCollectionsResponse#getNextOffsetToken()} as the
   * offset token to get the next page.
   *
   * @param request The list collections request.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<ListCollectionsResponse> listCollectionsAsync(
      ListCollectionsRequest request) {
    return listCollectionsAsync(request, null);
  }

  /**
   * Lists a page of collections. Pass {@link ListCollectionsResponse#getNextOffsetToken()} as the
   * offset token to get the next page.
   *
   * @param request The list collections request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<ListCollectionsResponse> listCollectionsAsync(
      ListCollectionsRequest request, @Nullable Duration timeout) {
    logger.debug("List collections");
    ListenableFuture<ListCollectionsResponse> future =
        getCollections(timeout).listCollections(request);
    QdrantClient.addLogFailureCallback(logger, future, "List collections");
    return future;
  }

  // endregion

  // region Points

  /**
   * Queries points.
   *
   * @param request The query request.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<ScoredPoint>> queryAsync(QueryPoints request) {
    return points.queryAsync(request);
  }

  /**
   * Queries points.
   *
   * @param request The query request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<ScoredPoint>> queryAsync(
      QueryPoints request, @Nullable Duration timeout) {
    return points.queryAsync(request, timeout);
  }

  /**
   * Runs several queries in one request.
   *
   * @param request The query batch request.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<BatchResult>> queryBatchAsync(QueryBatchPoints request) {
    return queryBatchAsync(request, null);
  }

  /**
   * Runs several queries in one request.
   *
   * @param request The query batch request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<BatchResult>> queryBatchAsync(
      QueryBatchPoints request, @Nullable Duration timeout) {
    return points.queryBatchAsync(request, timeout);
  }

  /**
   * Queries points grouped by a payload field.
   *
   * @param request The query groups request.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<PointGroup>> queryGroupsAsync(QueryPointGroups request) {
    return points.queryGroupsAsync(request);
  }

  /**
   * Queries points grouped by a payload field.
   *
   * @param request The query groups request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<PointGroup>> queryGroupsAsync(
      QueryPointGroups request, @Nullable Duration timeout) {
    return points.queryGroupsAsync(request, timeout);
  }

  /**
   * Retrieves points by IDs.
   *
   * @param request The get points request.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<RetrievedPoint>> retrieveAsync(GetPoints request) {
    return points.retrieveAsync(request, null);
  }

  /**
   * Retrieves points by IDs.
   *
   * @param request The get points request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<RetrievedPoint>> retrieveAsync(
      GetPoints request, @Nullable Duration timeout) {
    return points.retrieveAsync(request, timeout);
  }

  /**
   * Gets a page of points.
   *
   * @param request The scroll request.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<ScrollResponse> scrollAsync(ScrollPoints request) {
    return points.scrollAsync(request);
  }

  /**
   * Gets a page of points.
   *
   * @param request The scroll request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<ScrollResponse> scrollAsync(
      ScrollPoints request, @Nullable Duration timeout) {
    return points.scrollAsync(request, timeout);
  }

  /**
   * Counts points.
   *
   * @param request The count request.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<Long> countAsync(CountPoints request) {
    return countAsync(request, null);
  }

  /**
   * Counts points.
   *
   * @param request The count request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<Long> countAsync(CountPoints request, @Nullable Duration timeout) {
    return points.countAsync(request, timeout);
  }

  /**
   * Inserts or overwrites points.
   *
   * @param request The upsert request.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> upsertAsync(UpsertPoints request) {
    return points.upsertAsync(request);
  }

  /**
   * Inserts or overwrites points.
   *
   * @param request The upsert request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> upsertAsync(
      UpsertPoints request, @Nullable Duration timeout) {
    return points.upsertAsync(request, timeout);
  }

  /**
   * Updates vectors of points.
   *
   * @param request The update vectors request.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> updateVectorsAsync(UpdatePointVectors request) {
    return points.updateVectorsAsync(request);
  }

  /**
   * Updates vectors of points.
   *
   * @param request The update vectors request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> updateVectorsAsync(
      UpdatePointVectors request, @Nullable Duration timeout) {
    return points.updateVectorsAsync(request, timeout);
  }

  /**
   * Removes vectors from points.
   *
   * @param request The delete vectors request.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteVectorsAsync(DeletePointVectors request) {
    return points.deleteVectorsAsync(request, null);
  }

  /**
   * Removes vectors from points.
   *
   * @param request The delete vectors request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteVectorsAsync(
      DeletePointVectors request, @Nullable Duration timeout) {
    return points.deleteVectorsAsync(request, timeout);
  }

  /**
   * Deletes points.
   *
   * @param request The delete request.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteAsync(DeletePoints request) {
    return points.deleteAsync(request);
  }

  /**
   * Deletes points.
   *
   * @param request The delete request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteAsync(
      DeletePoints request, @Nullable Duration timeout) {
    return points.deleteAsync(request, timeout);
  }

  /**
   * Sets payload fields of points.
   *
   * @param request The set payload request.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> setPayloadAsync(SetPayloadPoints request) {
    return points.setPayloadAsync(request, null);
  }

  /**
   * Sets payload fields of points.
   *
   * @param request The set payload request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> setPayloadAsync(
      SetPayloadPoints request, @Nullable Duration timeout) {
    return points.setPayloadAsync(request, timeout);
  }

  /**
   * Replaces the entire payload of points.
   *
   * @param request The set payload request.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> overwritePayloadAsync(SetPayloadPoints request) {
    return points.overwritePayloadAsync(request, null);
  }

  /**
   * Replaces the entire payload of points.
   *
   * @param request The set payload request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> overwritePayloadAsync(
      SetPayloadPoints request, @Nullable Duration timeout) {
    return points.overwritePayloadAsync(request, timeout);
  }

  /**
   * Removes payload fields from points.
   *
   * @param request The delete payload request.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deletePayloadAsync(DeletePayloadPoints request) {
    return points.deletePayloadAsync(request, null);
  }

  /**
   * Removes payload fields from points.
   *
   * @param request The delete payload request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deletePayloadAsync(
      DeletePayloadPoints request, @Nullable Duration timeout) {
    return points.deletePayloadAsync(request, timeout);
  }

  /**
   * Removes the entire payload of points.
   *
   * @param request The clear payload request.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> clearPayloadAsync(ClearPayloadPoints request) {
    return points.clearPayloadAsync(request, null);
  }

  /**
   * Removes the entire payload of points.
   *
   * @param request The clear payload request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> clearPayloadAsync(
      ClearPayloadPoints request, @Nullable Duration timeout) {
    return points.clearPayloadAsync(request, timeout);
  }

  /**
   * Runs several update operations in one request.
   *
   * @param request The update batch request.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<UpdateResult>> batchUpdateAsync(UpdateBatchPoints request) {
    return points.batchUpdateAsync(request, null);
  }

  /**
   * Runs several update operations in one request.
   *
   * @param request The update batch request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<UpdateResult>> batchUpdateAsync(
      UpdateBatchPoints request, @Nullable Duration timeout) {
    return points.batchUpdateAsync(request, timeout);
  }

  // endregion

  @Override
  public void close() {
    grpcClient.close();
  }

  private CollectionsServiceFutureStub getCollections(@Nullable Duration timeout) {
    return timeout != null
        ? grpcClient
            .serverlessCollections()
            .withDeadlineAfter(timeout.toMillis(), TimeUnit.MILLISECONDS)
        : grpcClient.serverlessCollections();
  }
}
