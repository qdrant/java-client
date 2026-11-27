package io.qdrant.client;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.qdrant.client.grpc.Common.Filter;
import io.qdrant.client.grpc.Common.PointId;
import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points.BatchResult;
import io.qdrant.client.grpc.Points.ClearPayloadPoints;
import io.qdrant.client.grpc.Points.CountPoints;
import io.qdrant.client.grpc.Points.DeletePayloadPoints;
import io.qdrant.client.grpc.Points.DeletePointVectors;
import io.qdrant.client.grpc.Points.DeletePoints;
import io.qdrant.client.grpc.Points.GetPoints;
import io.qdrant.client.grpc.Points.PointGroup;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.PointVectors;
import io.qdrant.client.grpc.Points.PointsSelector;
import io.qdrant.client.grpc.Points.PointsUpdateOperation;
import io.qdrant.client.grpc.Points.QueryBatchPoints;
import io.qdrant.client.grpc.Points.QueryPointGroups;
import io.qdrant.client.grpc.Points.QueryPoints;
import io.qdrant.client.grpc.Points.ReadConsistency;
import io.qdrant.client.grpc.Points.RetrievedPoint;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.ScrollPoints;
import io.qdrant.client.grpc.Points.ScrollResponse;
import io.qdrant.client.grpc.Points.SetPayloadPoints;
import io.qdrant.client.grpc.Points.UpdateBatchPoints;
import io.qdrant.client.grpc.Points.UpdatePointVectors;
import io.qdrant.client.grpc.Points.UpdateResult;
import io.qdrant.client.grpc.Points.UpsertPoints;
import io.qdrant.client.grpc.Points.WithPayloadSelector;
import io.qdrant.client.grpc.Points.WithVectorsSelector;
import io.qdrant.client.grpc.Points.WriteOrdering;
import io.qdrant.client.grpc.Points.WriteOrderingType;
import io.qdrant.client.grpc.serverless.CollectionsServiceGrpc.CollectionsServiceFutureStub;
import io.qdrant.client.grpc.serverless.ServerlessCollections.CollectionConfig;
import io.qdrant.client.grpc.serverless.ServerlessCollections.CreateCollectionRequest;
import io.qdrant.client.grpc.serverless.ServerlessCollections.CreateCollectionResponse;
import io.qdrant.client.grpc.serverless.ServerlessCollections.DeleteCollectionRequest;
import io.qdrant.client.grpc.serverless.ServerlessCollections.DeleteCollectionResponse;
import io.qdrant.client.grpc.serverless.ServerlessCollections.DenseVectorConfig;
import io.qdrant.client.grpc.serverless.ServerlessCollections.GetCollectionRequest;
import io.qdrant.client.grpc.serverless.ServerlessCollections.GetCollectionResponse;
import io.qdrant.client.grpc.serverless.ServerlessCollections.ListCollectionsRequest;
import io.qdrant.client.grpc.serverless.ServerlessCollections.ListCollectionsResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
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
   * Creates a collection with a single unnamed (default) dense vector. An existing collection fails
   * with {@code ALREADY_EXISTS}, or is reported in the response's {@code result}.
   *
   * @param collectionName The name of the collection.
   * @param vector The configuration of the default vector.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CreateCollectionResponse> createCollectionAsync(
      String collectionName, DenseVectorConfig vector) {
    return createCollectionAsync(collectionName, vector, null);
  }

  /**
   * Creates a collection with a single unnamed (default) dense vector. An existing collection fails
   * with {@code ALREADY_EXISTS}, or is reported in the response's {@code result}.
   *
   * @param collectionName The name of the collection.
   * @param vector The configuration of the default vector.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CreateCollectionResponse> createCollectionAsync(
      String collectionName, DenseVectorConfig vector, @Nullable Duration timeout) {
    return createCollectionAsync(
        collectionName, ServerlessCollectionConfigFactory.defaultVector(vector).build(), timeout);
  }

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
   * Universally query points in batch. Covers all capabilities of search, recommend, discover,
   * filters. Also enables hybrid and multi-stage queries.
   *
   * @param collectionName The name of the collection
   * @param queries The queries to be performed in the batch.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<BatchResult>> queryBatchAsync(
      String collectionName, List<QueryPoints> queries) {
    return points.queryBatchAsync(collectionName, queries);
  }

  /**
   * Universally query points in batch. Covers all capabilities of search, recommend, discover,
   * filters. Also enables hybrid and multi-stage queries.
   *
   * @param collectionName The name of the collection
   * @param queries The queries to be performed in the batch.
   * @param readConsistency Options for specifying read consistency guarantees.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<BatchResult>> queryBatchAsync(
      String collectionName, List<QueryPoints> queries, @Nullable ReadConsistency readConsistency) {
    return points.queryBatchAsync(collectionName, queries, readConsistency);
  }

  /**
   * Universally query points in batch. Covers all capabilities of search, recommend, discover,
   * filters. Also enables hybrid and multi-stage queries.
   *
   * @param collectionName The name of the collection
   * @param queries The queries to be performed in the batch.
   * @param readConsistency Options for specifying read consistency guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<BatchResult>> queryBatchAsync(
      String collectionName,
      List<QueryPoints> queries,
      @Nullable ReadConsistency readConsistency,
      @Nullable Duration timeout) {
    return points.queryBatchAsync(collectionName, queries, readConsistency, timeout);
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
   * Retrieves points. Includes all payload, excludes vectors.
   *
   * @param collectionName The name of the collection.
   * @param id The id of a point to retrieve
   * @param readConsistency Options for specifying read consistency guarantees.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<RetrievedPoint>> retrieveAsync(
      String collectionName, PointId id, @Nullable ReadConsistency readConsistency) {
    return points.retrieveAsync(collectionName, id, readConsistency);
  }

  /**
   * Retrieves points.
   *
   * @param collectionName The name of the collection.
   * @param id The id of a point to retrieve
   * @param withPayload Whether to include the payload or not.
   * @param withVectors Whether to include the vectors or not.
   * @param readConsistency Options for specifying read consistency guarantees.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<RetrievedPoint>> retrieveAsync(
      String collectionName,
      PointId id,
      boolean withPayload,
      boolean withVectors,
      @Nullable ReadConsistency readConsistency) {
    return points.retrieveAsync(collectionName, id, withPayload, withVectors, readConsistency);
  }

  /**
   * Retrieves points. Includes all payload, excludes vectors.
   *
   * @param collectionName The name of the collection.
   * @param ids The list of ids of points to retrieve
   * @param readConsistency Options for specifying read consistency guarantees.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<RetrievedPoint>> retrieveAsync(
      String collectionName, List<PointId> ids, @Nullable ReadConsistency readConsistency) {
    return points.retrieveAsync(collectionName, ids, readConsistency);
  }

  /**
   * Retrieves points.
   *
   * @param collectionName The name of the collection.
   * @param ids The list of ids of points to retrieve
   * @param withPayload Whether to include the payload or not.
   * @param withVectors Whether to include the vectors or not.
   * @param readConsistency Options for specifying read consistency guarantees.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<RetrievedPoint>> retrieveAsync(
      String collectionName,
      List<PointId> ids,
      boolean withPayload,
      boolean withVectors,
      @Nullable ReadConsistency readConsistency) {
    return points.retrieveAsync(collectionName, ids, withPayload, withVectors, readConsistency);
  }

  /**
   * Retrieves points.
   *
   * @param collectionName The name of the collection.
   * @param ids The list of ids of points to retrieve
   * @param payloadSelector Options for specifying which payload to include or not.
   * @param vectorsSelector Options for specifying which vectors to include into response.
   * @param readConsistency Options for specifying read consistency guarantees.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<RetrievedPoint>> retrieveAsync(
      String collectionName,
      List<PointId> ids,
      WithPayloadSelector payloadSelector,
      WithVectorsSelector vectorsSelector,
      @Nullable ReadConsistency readConsistency) {
    return points.retrieveAsync(
        collectionName, ids, payloadSelector, vectorsSelector, readConsistency);
  }

  /**
   * Retrieves points.
   *
   * @param collectionName The name of the collection.
   * @param ids The list of ids of points to retrieve
   * @param payloadSelector Options for specifying which payload to include or not.
   * @param vectorsSelector Options for specifying which vectors to include into response.
   * @param readConsistency Options for specifying read consistency guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<RetrievedPoint>> retrieveAsync(
      String collectionName,
      List<PointId> ids,
      WithPayloadSelector payloadSelector,
      WithVectorsSelector vectorsSelector,
      @Nullable ReadConsistency readConsistency,
      @Nullable Duration timeout) {
    return points.retrieveAsync(
        collectionName, ids, payloadSelector, vectorsSelector, readConsistency, timeout);
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
   * Count the points in a collection. The count is exact
   *
   * @param collectionName The name of the collection.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<Long> countAsync(String collectionName) {
    return points.countAsync(collectionName);
  }

  /**
   * Count the points in a collection. The count is exact
   *
   * @param collectionName The name of the collection.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<Long> countAsync(String collectionName, @Nullable Duration timeout) {
    return points.countAsync(collectionName, timeout);
  }

  /**
   * Count the points in a collection with the given filtering conditions.
   *
   * @param collectionName The name of the collection.
   * @param filter Filter conditions - return only those points that satisfy the specified
   *     conditions.
   * @param exact If <code>true</code>, returns the exact count, if <code>false</code>, returns an
   *     approximate count. Defaults to <code>true</code>.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<Long> countAsync(
      String collectionName, @Nullable Filter filter, @Nullable Boolean exact) {
    return points.countAsync(collectionName, filter, exact);
  }

  /**
   * Count the points in a collection with the given filtering conditions.
   *
   * @param collectionName The name of the collection.
   * @param filter Filter conditions - return only those points that satisfy the specified
   *     conditions.
   * @param exact If <code>true</code>, returns the exact count, if <code>false</code>, returns an
   *     approximate count. Defaults to <code>true</code>.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<Long> countAsync(
      String collectionName,
      @Nullable Filter filter,
      @Nullable Boolean exact,
      @Nullable Duration timeout) {
    return points.countAsync(collectionName, filter, exact, timeout);
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
   * Perform insert and updates on points. If a point with a given ID already exists, it will be
   * overwritten.
   *
   * @param collectionName The name of the collection.
   * @param points The points to be upserted
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> upsertAsync(
      String collectionName, List<PointStruct> points) {
    return this.points.upsertAsync(collectionName, points);
  }

  /**
   * Perform insert and updates on points. If a point with a given ID already exists, it will be
   * overwritten. The call waits for the changes to be applied.
   *
   * @param collectionName The name of the collection.
   * @param points The points to be upserted
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> upsertAsync(
      String collectionName, List<PointStruct> points, @Nullable Duration timeout) {
    return this.points.upsertAsync(collectionName, points, timeout);
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
   * Update named vectors for point.
   *
   * @param collectionName The name of the collection.
   * @param points The list of points and vectors to update.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> updateVectorsAsync(
      String collectionName, List<PointVectors> points) {
    return this.points.updateVectorsAsync(collectionName, points);
  }

  /**
   * Update named vectors for point.
   *
   * @param collectionName The name of the collection.
   * @param points The list of points and vectors to update.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> updateVectorsAsync(
      String collectionName, List<PointVectors> points, @Nullable Duration timeout) {
    return this.points.updateVectorsAsync(collectionName, points, timeout);
  }

  /**
   * Update named vectors for point.
   *
   * @param collectionName The name of the collection.
   * @param points The list of points and vectors to update.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> updateVectorsAsync(
      String collectionName,
      List<PointVectors> points,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return this.points.updateVectorsAsync(collectionName, points, wait, ordering, timeout);
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
   * Delete named vectors for points.
   *
   * @param collectionName The name of the collection.
   * @param vectors The list of vector names to delete.
   * @param filter A filter selecting the points to be deleted.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteVectorsAsync(
      String collectionName, List<String> vectors, Filter filter) {
    return points.deleteVectorsAsync(collectionName, vectors, filter);
  }

  /**
   * Delete named vectors for points.
   *
   * @param collectionName The name of the collection.
   * @param vectors The list of vector names to delete.
   * @param filter A filter selecting the points to be deleted.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteVectorsAsync(
      String collectionName, List<String> vectors, Filter filter, @Nullable Duration timeout) {
    return points.deleteVectorsAsync(collectionName, vectors, filter, timeout);
  }

  /**
   * Delete named vectors for points.
   *
   * @param collectionName The name of the collection.
   * @param vectors The list of vector names to delete.
   * @param filter A filter selecting the points to be deleted.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteVectorsAsync(
      String collectionName,
      List<String> vectors,
      Filter filter,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.deleteVectorsAsync(collectionName, vectors, filter, wait, ordering, timeout);
  }

  /**
   * Delete named vectors for points.
   *
   * @param collectionName The name of the collection.
   * @param vectors The list of vector names to delete.
   * @param ids The list of ids to delete.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteVectorsAsync(
      String collectionName, List<String> vectors, List<PointId> ids) {
    return points.deleteVectorsAsync(collectionName, vectors, ids);
  }

  /**
   * Delete named vectors for points.
   *
   * @param collectionName The name of the collection.
   * @param vectors The list of vector names to delete.
   * @param ids The list of ids to delete.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteVectorsAsync(
      String collectionName, List<String> vectors, List<PointId> ids, @Nullable Duration timeout) {
    return points.deleteVectorsAsync(collectionName, vectors, ids, timeout);
  }

  /**
   * Delete named vectors for points.
   *
   * @param collectionName The name of the collection.
   * @param vectors The list of vector names to delete.
   * @param ids The list of ids to delete.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteVectorsAsync(
      String collectionName,
      List<String> vectors,
      List<PointId> ids,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.deleteVectorsAsync(collectionName, vectors, ids, wait, ordering, timeout);
  }

  /**
   * Delete named vectors for points.
   *
   * @param collectionName The name of the collection.
   * @param vectors The list of vector names to delete.
   * @param pointsSelector A selector for the points to be deleted.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteVectorsAsync(
      String collectionName,
      List<String> vectors,
      PointsSelector pointsSelector,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.deleteVectorsAsync(
        collectionName, vectors, pointsSelector, wait, ordering, timeout);
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
   * Deletes points. The call waits for the changes to be applied.
   *
   * @param collectionName The name of the collection.
   * @param ids The ids of points to delete.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteAsync(String collectionName, List<PointId> ids) {
    return points.deleteAsync(collectionName, ids);
  }

  /**
   * Deletes points. The call waits for the changes to be applied.
   *
   * @param collectionName The name of the collection.
   * @param ids The ids of points to delete.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteAsync(
      String collectionName, List<PointId> ids, @Nullable Duration timeout) {
    return points.deleteAsync(collectionName, ids, timeout);
  }

  /**
   * Deletes points. The call waits for the changes to be applied.
   *
   * @param collectionName The name of the collection.
   * @param filter A filter selecting the points to be deleted.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteAsync(String collectionName, Filter filter) {
    return points.deleteAsync(collectionName, filter);
  }

  /**
   * Deletes points.
   *
   * @param collectionName The name of the collection.
   * @param filter A filter selecting the points to be deleted.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteAsync(
      String collectionName, Filter filter, @Nullable Duration timeout) {
    return points.deleteAsync(collectionName, filter, timeout);
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
   * Sets the payload for all points in the collection.
   *
   * @param collectionName The name of the collection.
   * @param payload New payload values
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> setPayloadAsync(
      String collectionName,
      Map<String, Value> payload,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.setPayloadAsync(collectionName, payload, wait, ordering, timeout);
  }

  /**
   * Sets the payload for the given id.
   *
   * @param collectionName The name of the collection.
   * @param payload New payload values
   * @param id The id for which to set the payload.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> setPayloadAsync(
      String collectionName,
      Map<String, Value> payload,
      PointId id,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.setPayloadAsync(collectionName, payload, id, wait, ordering, timeout);
  }

  /**
   * Sets the payload for the given ids.
   *
   * @param collectionName The name of the collection.
   * @param payload New payload values
   * @param ids The ids for which to set the payload.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> setPayloadAsync(
      String collectionName,
      Map<String, Value> payload,
      List<PointId> ids,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.setPayloadAsync(collectionName, payload, ids, wait, ordering, timeout);
  }

  /**
   * Sets the payload for the given ids.
   *
   * @param collectionName The name of the collection.
   * @param payload New payload values
   * @param filter A filter selecting the points to be set.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> setPayloadAsync(
      String collectionName,
      Map<String, Value> payload,
      Filter filter,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.setPayloadAsync(collectionName, payload, filter, wait, ordering, timeout);
  }

  /**
   * Sets the payload for the points.
   *
   * @param collectionName The name of the collection.
   * @param payload New payload values
   * @param pointsSelector selector for the points whose payloads are to be set.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> setPayloadAsync(
      String collectionName,
      Map<String, Value> payload,
      @Nullable PointsSelector pointsSelector,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.setPayloadAsync(collectionName, payload, pointsSelector, wait, ordering, timeout);
  }

  /**
   * Sets the payload for the points.
   *
   * @param collectionName The name of the collection.
   * @param payload New payload values
   * @param pointsSelector Selector for the points whose payloads are to be set.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param key The key for which to set the payload if nested
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> setPayloadAsync(
      String collectionName,
      Map<String, Value> payload,
      @Nullable PointsSelector pointsSelector,
      @Nullable Boolean wait,
      @Nullable String key,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.setPayloadAsync(
        collectionName, payload, pointsSelector, wait, key, ordering, timeout);
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
   * Overwrites the payload for all points in the collection.
   *
   * @param collectionName The name of the collection.
   * @param payload New payload values
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> overwritePayloadAsync(
      String collectionName,
      Map<String, Value> payload,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.overwritePayloadAsync(collectionName, payload, wait, ordering, timeout);
  }

  /**
   * Overwrites the payload for the given id.
   *
   * @param collectionName The name of the collection.
   * @param payload New payload values
   * @param id The id for which to overwrite the payload.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> overwritePayloadAsync(
      String collectionName,
      Map<String, Value> payload,
      PointId id,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.overwritePayloadAsync(collectionName, payload, id, wait, ordering, timeout);
  }

  /**
   * Overwrites the payload for the given ids.
   *
   * @param collectionName The name of the collection.
   * @param payload New payload values
   * @param ids The ids for which to overwrite the payload.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> overwritePayloadAsync(
      String collectionName,
      Map<String, Value> payload,
      List<PointId> ids,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.overwritePayloadAsync(collectionName, payload, ids, wait, ordering, timeout);
  }

  /**
   * Overwrites the payload for the filtered points.
   *
   * @param collectionName The name of the collection.
   * @param payload New payload values
   * @param filter A filter selecting the points for which to overwrite the payload.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> overwritePayloadAsync(
      String collectionName,
      Map<String, Value> payload,
      Filter filter,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.overwritePayloadAsync(collectionName, payload, filter, wait, ordering, timeout);
  }

  /**
   * Overwrites the payload for the points.
   *
   * @param collectionName The name of the collection.
   * @param payload New payload values
   * @param pointsSelector A selector for the points whose payloads are to be overwritten.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> overwritePayloadAsync(
      String collectionName,
      Map<String, Value> payload,
      @Nullable PointsSelector pointsSelector,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.overwritePayloadAsync(
        collectionName, payload, pointsSelector, wait, ordering, timeout);
  }

  /**
   * Overwrites the payload for the points.
   *
   * @param collectionName The name of the collection.
   * @param payload New payload values
   * @param pointsSelector Selector for the points whose payloads are to be overwritten.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param key The key for which to overwrite the payload if nested
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> overwritePayloadAsync(
      String collectionName,
      Map<String, Value> payload,
      @Nullable PointsSelector pointsSelector,
      @Nullable Boolean wait,
      @Nullable String key,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.overwritePayloadAsync(
        collectionName, payload, pointsSelector, wait, key, ordering, timeout);
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
   * Delete specified key payload for all points.
   *
   * @param collectionName The name of the collection.
   * @param keys List of keys to delete.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deletePayloadAsync(
      String collectionName,
      List<String> keys,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.deletePayloadAsync(collectionName, keys, wait, ordering, timeout);
  }

  /**
   * Delete specified key payload for the given id.
   *
   * @param collectionName The name of the collection.
   * @param keys List of keys to delete.
   * @param id The id for which to delete the payload.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deletePayloadAsync(
      String collectionName,
      List<String> keys,
      PointId id,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.deletePayloadAsync(collectionName, keys, id, wait, ordering, timeout);
  }

  /**
   * Delete specified key payload for the given ids.
   *
   * @param collectionName The name of the collection.
   * @param keys List of keys to delete.
   * @param ids The ids for which to delete the payload.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deletePayloadAsync(
      String collectionName,
      List<String> keys,
      List<PointId> ids,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.deletePayloadAsync(collectionName, keys, ids, wait, ordering, timeout);
  }

  /**
   * Delete specified key payload for the filtered points.
   *
   * @param collectionName The name of the collection.
   * @param keys List of keys to delete.
   * @param filter A filter selecting the points to for which to delete the payload.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deletePayloadAsync(
      String collectionName,
      List<String> keys,
      Filter filter,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.deletePayloadAsync(collectionName, keys, filter, wait, ordering, timeout);
  }

  /**
   * Delete specified key payload for the points.
   *
   * @param collectionName The name of the collection.
   * @param keys List of keys to delete.
   * @param pointsSelector selector for the points whose payloads are to be deleted.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deletePayloadAsync(
      String collectionName,
      List<String> keys,
      @Nullable PointsSelector pointsSelector,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.deletePayloadAsync(collectionName, keys, pointsSelector, wait, ordering, timeout);
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
   * Remove all payload for all points.
   *
   * @param collectionName The name of the collection.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> clearPayloadAsync(
      String collectionName,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.clearPayloadAsync(collectionName, wait, ordering, timeout);
  }

  /**
   * Removes all payload for the given id.
   *
   * @param collectionName The name of the collection.
   * @param id The id for which to remove the payload.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> clearPayloadAsync(
      String collectionName,
      PointId id,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.clearPayloadAsync(collectionName, id, wait, ordering, timeout);
  }

  /**
   * Removes all payload for the given ids.
   *
   * @param collectionName The name of the collection.
   * @param ids The ids for which to remove the payload.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> clearPayloadAsync(
      String collectionName,
      List<PointId> ids,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.clearPayloadAsync(collectionName, ids, wait, ordering, timeout);
  }

  /**
   * Removes all payload for the filtered points.
   *
   * @param collectionName The name of the collection.
   * @param filter A filter selecting the points for which to remove the payload.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> clearPayloadAsync(
      String collectionName,
      Filter filter,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.clearPayloadAsync(collectionName, filter, wait, ordering, timeout);
  }

  /**
   * Removes all payload for the points.
   *
   * @param collectionName The name of the collection.
   * @param pointsSelector A selector for the points whose payloads are to be removed.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> clearPayloadAsync(
      String collectionName,
      @Nullable PointsSelector pointsSelector,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    return points.clearPayloadAsync(collectionName, pointsSelector, wait, ordering, timeout);
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
   * Performs a batch update of points.
   *
   * @param collectionName The name of the collection.
   * @param operations The list of point update operations.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<UpdateResult>> batchUpdateAsync(
      String collectionName, List<PointsUpdateOperation> operations) {
    return points.batchUpdateAsync(collectionName, operations);
  }

  /**
   * Performs a batch update of points.
   *
   * @param collectionName The name of the collection.
   * @param operations The list of point update operations.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<UpdateResult>> batchUpdateAsync(
      String collectionName,
      List<PointsUpdateOperation> operations,
      @Nullable Boolean wait,
      @Nullable WriteOrdering ordering,
      @Nullable Duration timeout) {
    return points.batchUpdateAsync(collectionName, operations, wait, ordering, timeout);
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
