package io.qdrant.client;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.qdrant.client.grpc.Collections.AliasDescription;
import io.qdrant.client.grpc.Collections.AliasOperations;
import io.qdrant.client.grpc.Collections.ChangeAliases;
import io.qdrant.client.grpc.Collections.CollectionDescription;
import io.qdrant.client.grpc.Collections.CollectionExistsRequest;
import io.qdrant.client.grpc.Collections.CollectionExistsResponse;
import io.qdrant.client.grpc.Collections.CollectionInfo;
import io.qdrant.client.grpc.Collections.CollectionOperationResponse;
import io.qdrant.client.grpc.Collections.CreateAlias;
import io.qdrant.client.grpc.Collections.CreateCollection;
import io.qdrant.client.grpc.Collections.CreateShardKeyRequest;
import io.qdrant.client.grpc.Collections.CreateShardKeyResponse;
import io.qdrant.client.grpc.Collections.DeleteAlias;
import io.qdrant.client.grpc.Collections.DeleteCollection;
import io.qdrant.client.grpc.Collections.DeleteShardKeyRequest;
import io.qdrant.client.grpc.Collections.DeleteShardKeyResponse;
import io.qdrant.client.grpc.Collections.GetCollectionInfoRequest;
import io.qdrant.client.grpc.Collections.GetCollectionInfoResponse;
import io.qdrant.client.grpc.Collections.ListAliasesRequest;
import io.qdrant.client.grpc.Collections.ListAliasesResponse;
import io.qdrant.client.grpc.Collections.ListCollectionAliasesRequest;
import io.qdrant.client.grpc.Collections.ListCollectionsRequest;
import io.qdrant.client.grpc.Collections.ListCollectionsResponse;
import io.qdrant.client.grpc.Collections.PayloadIndexParams;
import io.qdrant.client.grpc.Collections.PayloadSchemaType;
import io.qdrant.client.grpc.Collections.RenameAlias;
import io.qdrant.client.grpc.Collections.ShardKey;
import io.qdrant.client.grpc.Collections.UpdateCollection;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Collections.VectorParamsMap;
import io.qdrant.client.grpc.Collections.VectorsConfig;
import io.qdrant.client.grpc.CollectionsGrpc;
import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points;
import io.qdrant.client.grpc.Points.BatchResult;
import io.qdrant.client.grpc.Points.ClearPayloadPoints;
import io.qdrant.client.grpc.Points.CountPoints;
import io.qdrant.client.grpc.Points.CountResponse;
import io.qdrant.client.grpc.Points.CreateFieldIndexCollection;
import io.qdrant.client.grpc.Points.DeleteFieldIndexCollection;
import io.qdrant.client.grpc.Points.DeletePayloadPoints;
import io.qdrant.client.grpc.Points.DeletePointVectors;
import io.qdrant.client.grpc.Points.DeletePoints;
import io.qdrant.client.grpc.Points.DiscoverBatchPoints;
import io.qdrant.client.grpc.Points.DiscoverBatchResponse;
import io.qdrant.client.grpc.Points.DiscoverPoints;
import io.qdrant.client.grpc.Points.DiscoverResponse;
import io.qdrant.client.grpc.Points.FieldType;
import io.qdrant.client.grpc.Points.Filter;
import io.qdrant.client.grpc.Points.GetPoints;
import io.qdrant.client.grpc.Points.GetResponse;
import io.qdrant.client.grpc.Points.PointGroup;
import io.qdrant.client.grpc.Points.PointId;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.PointVectors;
import io.qdrant.client.grpc.Points.PointsIdsList;
import io.qdrant.client.grpc.Points.PointsOperationResponse;
import io.qdrant.client.grpc.Points.PointsSelector;
import io.qdrant.client.grpc.Points.PointsUpdateOperation;
import io.qdrant.client.grpc.Points.QueryBatchPoints;
import io.qdrant.client.grpc.Points.QueryBatchResponse;
import io.qdrant.client.grpc.Points.QueryGroupsResponse;
import io.qdrant.client.grpc.Points.QueryPointGroups;
import io.qdrant.client.grpc.Points.QueryPoints;
import io.qdrant.client.grpc.Points.QueryResponse;
import io.qdrant.client.grpc.Points.ReadConsistency;
import io.qdrant.client.grpc.Points.RecommendBatchPoints;
import io.qdrant.client.grpc.Points.RecommendBatchResponse;
import io.qdrant.client.grpc.Points.RecommendGroupsResponse;
import io.qdrant.client.grpc.Points.RecommendPointGroups;
import io.qdrant.client.grpc.Points.RecommendPoints;
import io.qdrant.client.grpc.Points.RecommendResponse;
import io.qdrant.client.grpc.Points.RetrievedPoint;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.ScrollPoints;
import io.qdrant.client.grpc.Points.ScrollResponse;
import io.qdrant.client.grpc.Points.SearchBatchPoints;
import io.qdrant.client.grpc.Points.SearchBatchResponse;
import io.qdrant.client.grpc.Points.SearchGroupsResponse;
import io.qdrant.client.grpc.Points.SearchPointGroups;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.Points.SearchResponse;
import io.qdrant.client.grpc.Points.SetPayloadPoints;
import io.qdrant.client.grpc.Points.UpdateBatchPoints;
import io.qdrant.client.grpc.Points.UpdateBatchResponse;
import io.qdrant.client.grpc.Points.UpdatePointVectors;
import io.qdrant.client.grpc.Points.UpdateResult;
import io.qdrant.client.grpc.Points.UpsertPoints;
import io.qdrant.client.grpc.Points.VectorsSelector;
import io.qdrant.client.grpc.Points.WithPayloadSelector;
import io.qdrant.client.grpc.Points.WithVectorsSelector;
import io.qdrant.client.grpc.Points.WriteOrdering;
import io.qdrant.client.grpc.Points.WriteOrderingType;
import io.qdrant.client.grpc.PointsGrpc;
import io.qdrant.client.grpc.QdrantGrpc.QdrantFutureStub;
import io.qdrant.client.grpc.QdrantOuterClass.HealthCheckReply;
import io.qdrant.client.grpc.QdrantOuterClass.HealthCheckRequest;
import io.qdrant.client.grpc.SnapshotsGrpc;
import io.qdrant.client.grpc.SnapshotsService.CreateFullSnapshotRequest;
import io.qdrant.client.grpc.SnapshotsService.CreateSnapshotRequest;
import io.qdrant.client.grpc.SnapshotsService.CreateSnapshotResponse;
import io.qdrant.client.grpc.SnapshotsService.DeleteFullSnapshotRequest;
import io.qdrant.client.grpc.SnapshotsService.DeleteSnapshotRequest;
import io.qdrant.client.grpc.SnapshotsService.DeleteSnapshotResponse;
import io.qdrant.client.grpc.SnapshotsService.ListFullSnapshotsRequest;
import io.qdrant.client.grpc.SnapshotsService.ListSnapshotsRequest;
import io.qdrant.client.grpc.SnapshotsService.ListSnapshotsResponse;
import io.qdrant.client.grpc.SnapshotsService.SnapshotDescription;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Client for the Qdrant vector database. */
public class QdrantClient implements AutoCloseable {
  private static final Logger logger = LoggerFactory.getLogger(QdrantClient.class);
  private final QdrantGrpcClient grpcClient;

  /**
   * Creates a new instance of {@link QdrantClient}
   *
   * @param grpcClient The low-level gRPC client to use.
   */
  public QdrantClient(QdrantGrpcClient grpcClient) {
    this.grpcClient = grpcClient;
  }

  /**
   * Gets the low-level gRPC client. This is exposed to
   *
   * <ul>
   *   <li>Allow access to the underlying gRPC channel
   *   <li>Allow access to the gRPC client to make requests using the low-level gRPC client in cases
   *       where functionality may not yet be exposed by the higher level client.
   * </ul>
   *
   * @return The low-level gRPC client
   */
  public QdrantGrpcClient grpcClient() {
    return grpcClient;
  }

  /**
   * Gets detailed information about the qdrant cluster.
   *
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<HealthCheckReply> healthCheckAsync() {
    return healthCheckAsync(null);
  }

  /**
   * Gets detailed information about the qdrant cluster.
   *
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<HealthCheckReply> healthCheckAsync(@Nullable Duration timeout) {
    QdrantFutureStub qdrant =
        timeout != null
            ? this.grpcClient.qdrant().withDeadlineAfter(timeout.toMillis(), TimeUnit.MILLISECONDS)
            : this.grpcClient.qdrant();
    return qdrant.healthCheck(HealthCheckRequest.getDefaultInstance());
  }

  // region Collections

  /**
   * Creates a new collection with the given parameters
   *
   * @param collectionName The name of the collection.
   * @param vectorParams The vector parameters
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> createCollectionAsync(
      String collectionName, VectorParams vectorParams) {
    return createCollectionAsync(collectionName, vectorParams, null);
  }

  /**
   * Creates a new collection with the given parameters
   *
   * @param collectionName The name of the collection.
   * @param vectorParams The vector parameters
   * @param timeout The timeout for the call
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> createCollectionAsync(
      String collectionName, VectorParams vectorParams, @Nullable Duration timeout) {
    return createCollectionAsync(
        CreateCollection.newBuilder()
            .setCollectionName(collectionName)
            .setVectorsConfig(VectorsConfig.newBuilder().setParams(vectorParams).build())
            .build(),
        timeout);
  }

  /**
   * Creates a new collection with the given parameters
   *
   * @param collectionName The name of the collection.
   * @param namedVectorParams The named vector parameters
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> createCollectionAsync(
      String collectionName, Map<String, VectorParams> namedVectorParams) {
    return createCollectionAsync(collectionName, namedVectorParams, null);
  }

  /**
   * Creates a new collection with the given parameters
   *
   * @param collectionName The name of the collection.
   * @param namedVectorParams The named vector parameters
   * @param timeout The timeout for the call
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> createCollectionAsync(
      String collectionName,
      Map<String, VectorParams> namedVectorParams,
      @Nullable Duration timeout) {
    return createCollectionAsync(
        CreateCollection.newBuilder()
            .setCollectionName(collectionName)
            .setVectorsConfig(
                VectorsConfig.newBuilder()
                    .setParamsMap(VectorParamsMap.newBuilder().putAllMap(namedVectorParams).build())
                    .build())
            .build(),
        timeout);
  }

  /**
   * Creates a new collection with the given parameters
   *
   * @param createCollection The collection creation parameters
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> createCollectionAsync(
      CreateCollection createCollection) {
    return createCollectionAsync(createCollection, null);
  }

  /**
   * Creates a new collection with the given parameters
   *
   * @param createCollection The collection creation parameters
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> createCollectionAsync(
      CreateCollection createCollection, @Nullable Duration timeout) {
    String collectionName = createCollection.getCollectionName();
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");
    logger.debug("Create collection '{}'", collectionName);
    ListenableFuture<CollectionOperationResponse> future =
        getCollections(timeout).create(createCollection);
    addLogFailureCallback(future, "Create collection");
    return Futures.transform(
        future,
        response -> {
          if (!response.getResult()) {
            logger.error("Collection '{}' could not be created", collectionName);
            throw new QdrantException("Collection '" + collectionName + "' could not be created");
          }
          return response;
        },
        MoreExecutors.directExecutor());
  }

  /**
   * Deletes a collection if one exists, and creates a new collection with the given parameters.
   *
   * @param collectionName The name of the collection.
   * @param vectorParams The vector parameters
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> recreateCollectionAsync(
      String collectionName, VectorParams vectorParams) {
    return recreateCollectionAsync(collectionName, vectorParams, null);
  }

  /**
   * Deletes a collection if one exists, and creates a new collection with the given parameters.
   *
   * @param collectionName The name of the collection.
   * @param vectorParams The vector parameters
   * @param timeout The timeout for the call
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> recreateCollectionAsync(
      String collectionName, VectorParams vectorParams, @Nullable Duration timeout) {
    return recreateCollectionAsync(
        CreateCollection.newBuilder()
            .setCollectionName(collectionName)
            .setVectorsConfig(VectorsConfig.newBuilder().setParams(vectorParams).build())
            .build(),
        timeout);
  }

  /**
   * Deletes a collection if one exists, and creates a new collection with the given parameters.
   *
   * @param collectionName The name of the collection.
   * @param namedVectorParams The named vector parameters
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> recreateCollectionAsync(
      String collectionName, Map<String, VectorParams> namedVectorParams) {
    return recreateCollectionAsync(collectionName, namedVectorParams, null);
  }

  /**
   * Deletes a collection if one exists, and creates a new collection with the given parameters.
   *
   * @param collectionName The name of the collection.
   * @param namedVectorParams The named vector parameters
   * @param timeout The timeout for the call
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> recreateCollectionAsync(
      String collectionName,
      Map<String, VectorParams> namedVectorParams,
      @Nullable Duration timeout) {
    return recreateCollectionAsync(
        CreateCollection.newBuilder()
            .setCollectionName(collectionName)
            .setVectorsConfig(
                VectorsConfig.newBuilder()
                    .setParamsMap(VectorParamsMap.newBuilder().putAllMap(namedVectorParams).build())
                    .build())
            .build(),
        timeout);
  }

  /**
   * Deletes a collection if one exists, and creates a new collection with the given parameters.
   *
   * @param createCollection The collection creation parameters
   * @return a new instance of {@link CollectionOperationResponse}
   */
  public ListenableFuture<CollectionOperationResponse> recreateCollectionAsync(
      CreateCollection createCollection) {
    return recreateCollectionAsync(createCollection, null);
  }

  /**
   * Deletes a collection if one exists, and creates a new collection with the given parameters.
   *
   * @param createCollection The collection creation parameters
   * @param timeout The timeout for the call.
   * @return a new instance of {@link CollectionOperationResponse}
   */
  public ListenableFuture<CollectionOperationResponse> recreateCollectionAsync(
      CreateCollection createCollection, @Nullable Duration timeout) {
    return Futures.transformAsync(
        deleteCollectionAsync(createCollection.getCollectionName(), timeout),
        input -> createCollectionAsync(createCollection, timeout),
        MoreExecutors.directExecutor());
  }

  /**
   * Gets detailed information about an existing collection.
   *
   * @param collectionName The name of the collection.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionInfo> getCollectionInfoAsync(String collectionName) {
    return getCollectionInfoAsync(collectionName, null);
  }

  /**
   * Gets detailed information about an existing collection.
   *
   * @param collectionName The name of the collection.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionInfo> getCollectionInfoAsync(
      String collectionName, @Nullable Duration timeout) {
    logger.debug("Get collection info for '{}'", collectionName);
    GetCollectionInfoRequest request =
        GetCollectionInfoRequest.newBuilder().setCollectionName(collectionName).build();
    ListenableFuture<GetCollectionInfoResponse> future = getCollections(timeout).get(request);
    addLogFailureCallback(future, "Get collection info");
    return Futures.transform(
        future, GetCollectionInfoResponse::getResult, MoreExecutors.directExecutor());
  }

  /**
   * Deletes a collection and all its associated data.
   *
   * @param collectionName The name of the collection
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> deleteCollectionAsync(
      String collectionName) {
    return deleteCollectionAsync(collectionName, null);
  }

  /**
   * Deletes a collection and all its associated data.
   *
   * @param collectionName The name of the collection
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> deleteCollectionAsync(
      String collectionName, @Nullable Duration timeout) {
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");
    logger.debug("Delete collection '{}'", collectionName);

    DeleteCollection deleteCollection =
        DeleteCollection.newBuilder().setCollectionName(collectionName).build();
    ListenableFuture<CollectionOperationResponse> future =
        getCollections(timeout).delete(deleteCollection);
    addLogFailureCallback(future, "Delete collection");

    return Futures.transform(
        future,
        response -> {
          if (!response.getResult()) {
            logger.error("Collection '{}' could not be deleted", collectionName);
            throw new QdrantException("Collection '" + collectionName + "' could not be deleted");
          }
          return response;
        },
        MoreExecutors.directExecutor());
  }

  /**
   * Gets the names of all existing collections
   *
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<String>> listCollectionsAsync() {
    return listCollectionsAsync(null);
  }

  /**
   * Gets the names of all existing collections
   *
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<String>> listCollectionsAsync(@Nullable Duration timeout) {
    logger.debug("List collections");

    ListenableFuture<ListCollectionsResponse> future =
        getCollections(timeout).list(ListCollectionsRequest.getDefaultInstance());

    addLogFailureCallback(future, "List collection");
    return Futures.transform(
        future,
        response ->
            response.getCollectionsList().stream()
                .map(CollectionDescription::getName)
                .collect(Collectors.toList()),
        MoreExecutors.directExecutor());
  }

  /**
   * Update parameters of the collection
   *
   * @param updateCollection The update parameters.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> updateCollectionAsync(
      UpdateCollection updateCollection) {
    return updateCollectionAsync(updateCollection, null);
  }

  /**
   * Update parameters of the collection
   *
   * @param updateCollection The update parameters.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> updateCollectionAsync(
      UpdateCollection updateCollection, @Nullable Duration timeout) {
    String collectionName = updateCollection.getCollectionName();
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");
    logger.debug("Update collection '{}'", collectionName);

    ListenableFuture<CollectionOperationResponse> future =
        getCollections(timeout).update(updateCollection);
    addLogFailureCallback(future, "Update collection");
    return Futures.transform(
        future,
        response -> {
          if (!response.getResult()) {
            logger.error("Collection '{}' could not be updated", collectionName);
            throw new QdrantException("Collection '" + collectionName + "' could not be updated");
          }
          return response;
        },
        MoreExecutors.directExecutor());
  }

  /**
   * Check if a collection exists
   *
   * @param collectionName The name of the collection.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<Boolean> collectionExistsAsync(String collectionName) {
    return collectionExistsAsync(collectionName, null);
  }

  /**
   * Check if a collection exists
   *
   * @param collectionName The name of the collection.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<Boolean> collectionExistsAsync(
      String collectionName, @Nullable Duration timeout) {
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");
    logger.debug("Collection exists '{}'", collectionName);

    ListenableFuture<CollectionExistsResponse> future =
        getCollections(timeout)
            .collectionExists(
                CollectionExistsRequest.newBuilder().setCollectionName(collectionName).build());
    addLogFailureCallback(future, "Collection exists");
    return Futures.transform(
        future, response -> response.getResult().getExists(), MoreExecutors.directExecutor());
  }

  // endregion

  // region Alias Management

  /**
   * Creates an alias for a given collection.
   *
   * @param aliasName The alias to be created.
   * @param collectionName The collection for which the alias is to be created.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> createAliasAsync(
      String aliasName, String collectionName) {
    return createAliasAsync(aliasName, collectionName, null);
  }

  /**
   * Creates an alias for a given collection.
   *
   * @param aliasName The alias to be created.
   * @param collectionName The collection for which the alias is to be created.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> createAliasAsync(
      String aliasName, String collectionName, @Nullable Duration timeout) {
    return updateAliasesAsync(
        ImmutableList.of(
            AliasOperations.newBuilder()
                .setCreateAlias(
                    CreateAlias.newBuilder()
                        .setAliasName(aliasName)
                        .setCollectionName(collectionName)
                        .build())
                .build()),
        timeout);
  }

  /**
   * Renames an existing alias.
   *
   * @param oldAliasName The old alias name.
   * @param newAliasName The new alias name.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> renameAliasAsync(
      String oldAliasName, String newAliasName) {
    return renameAliasAsync(oldAliasName, newAliasName, null);
  }

  /**
   * Renames an existing alias.
   *
   * @param oldAliasName The old alias name.
   * @param newAliasName The new alias name.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> renameAliasAsync(
      String oldAliasName, String newAliasName, @Nullable Duration timeout) {
    return updateAliasesAsync(
        ImmutableList.of(
            AliasOperations.newBuilder()
                .setRenameAlias(
                    RenameAlias.newBuilder()
                        .setOldAliasName(oldAliasName)
                        .setNewAliasName(newAliasName)
                        .build())
                .build()),
        timeout);
  }

  /**
   * Deletes an alias.
   *
   * @param aliasName The alias to be deleted.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> deleteAliasAsync(String aliasName) {
    return deleteAliasAsync(aliasName, null);
  }

  /**
   * Deletes an alias.
   *
   * @param aliasName The alias to be deleted.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> deleteAliasAsync(
      String aliasName, @Nullable Duration timeout) {
    return updateAliasesAsync(
        ImmutableList.of(
            AliasOperations.newBuilder()
                .setDeleteAlias(DeleteAlias.newBuilder().setAliasName(aliasName).build())
                .build()),
        timeout);
  }

  /**
   * Update the aliases of existing collections.
   *
   * @param aliasOperations The list of operations to perform.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> updateAliasesAsync(
      List<AliasOperations> aliasOperations) {
    return updateAliasesAsync(aliasOperations, null);
  }

  /**
   * Update the aliases of existing collections.
   *
   * @param aliasOperations The list of operations to perform.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CollectionOperationResponse> updateAliasesAsync(
      List<AliasOperations> aliasOperations, @Nullable Duration timeout) {
    ChangeAliases request = ChangeAliases.newBuilder().addAllActions(aliasOperations).build();

    if (logger.isDebugEnabled()) {
      for (AliasOperations aliasOperation : aliasOperations) {
        switch (aliasOperation.getActionCase()) {
          case CREATE_ALIAS:
            CreateAlias createAlias = aliasOperation.getCreateAlias();
            logger.debug(
                "Create alias '{}' for collection '{}'",
                createAlias.getAliasName(),
                createAlias.getCollectionName());
            break;
          case RENAME_ALIAS:
            RenameAlias renameAlias = aliasOperation.getRenameAlias();
            logger.debug(
                "Rename alias '{}' to '{}'",
                renameAlias.getOldAliasName(),
                renameAlias.getNewAliasName());
            break;
          case DELETE_ALIAS:
            DeleteAlias deleteAlias = aliasOperation.getDeleteAlias();
            logger.debug("Delete alias '{}'", deleteAlias.getAliasName());
            break;
          case ACTION_NOT_SET:
            break;
        }
      }
    }

    ListenableFuture<CollectionOperationResponse> future =
        getCollections(timeout).updateAliases(request);
    addLogFailureCallback(future, "Update aliases");
    return Futures.transform(
        future,
        response -> {
          if (!response.getResult()) {
            logger.error("Alias update operation could not be performed");
            throw new QdrantException("Alias update could not be performed");
          }
          return response;
        },
        MoreExecutors.directExecutor());
  }

  /**
   * Gets a list of all aliases for a collection.
   *
   * @param collectionName The name of the collection.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<String>> listCollectionAliasesAsync(String collectionName) {
    return listCollectionAliasesAsync(collectionName, null);
  }

  /**
   * Gets a list of all aliases for a collection.
   *
   * @param collectionName The name of the collection.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<String>> listCollectionAliasesAsync(
      String collectionName, @Nullable Duration timeout) {
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");
    logger.debug("List aliases for collection '{}'", collectionName);

    ListCollectionAliasesRequest request =
        ListCollectionAliasesRequest.newBuilder().setCollectionName(collectionName).build();

    ListenableFuture<ListAliasesResponse> future =
        getCollections(timeout).listCollectionAliases(request);
    addLogFailureCallback(future, "List collection aliases");
    return Futures.transform(
        future,
        response ->
            response.getAliasesList().stream()
                .map(AliasDescription::getAliasName)
                .collect(Collectors.toList()),
        MoreExecutors.directExecutor());
  }

  /**
   * Gets a list of all aliases for all existing collections.
   *
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<AliasDescription>> listAliasesAsync() {
    return listAliasesAsync(null);
  }

  /**
   * Gets a list of all aliases for all existing collections.
   *
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<AliasDescription>> listAliasesAsync(@Nullable Duration timeout) {
    logger.debug("List all aliases");
    ListenableFuture<ListAliasesResponse> future =
        getCollections(timeout).listAliases(ListAliasesRequest.getDefaultInstance());
    addLogFailureCallback(future, "List aliases");
    return Futures.transform(
        future, ListAliasesResponse::getAliasesList, MoreExecutors.directExecutor());
  }

  // endregion

  // region ShardKey Management

  /**
   * Creates a shard key for a collection.
   *
   * @param createShardKey The request object for the operation.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CreateShardKeyResponse> createShardKeyAsync(
      CreateShardKeyRequest createShardKey) {
    return createShardKeyAsync(createShardKey, null);
  }

  /**
   * Creates a shard key for a collection.
   *
   * @param createShardKey The request object for the operation.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<CreateShardKeyResponse> createShardKeyAsync(
      CreateShardKeyRequest createShardKey, @Nullable Duration timeout) {
    String collectionName = createShardKey.getCollectionName();
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");
    ShardKey shardKey = createShardKey.getRequest().getShardKey();
    logger.debug("Create shard key '{}' for '{}'", shardKey, collectionName);

    ListenableFuture<CreateShardKeyResponse> future =
        getCollections(timeout).createShardKey(createShardKey);
    addLogFailureCallback(future, "Create shard key");
    return Futures.transform(
        future,
        response -> {
          if (!response.getResult()) {
            logger.error("Shard key could not be created for '{}'", collectionName);
            throw new QdrantException(
                "Shard key " + shardKey + " could not be created for " + collectionName);
          }
          return response;
        },
        MoreExecutors.directExecutor());
  }

  /**
   * Deletes a shard key for a collection.
   *
   * @param deleteShardKey The request object for the operation.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<DeleteShardKeyResponse> deleteShardKeyAsync(
      DeleteShardKeyRequest deleteShardKey) {
    return deleteShardKeyAsync(deleteShardKey, null);
  }

  /**
   * Deletes a shard key for a collection.
   *
   * @param deleteShardKey The request object for the operation.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<DeleteShardKeyResponse> deleteShardKeyAsync(
      DeleteShardKeyRequest deleteShardKey, @Nullable Duration timeout) {
    String collectionName = deleteShardKey.getCollectionName();
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");
    ShardKey shardKey = deleteShardKey.getRequest().getShardKey();
    logger.debug("Delete shard key '{}' for '{}'", shardKey, collectionName);

    ListenableFuture<DeleteShardKeyResponse> future =
        getCollections(timeout).deleteShardKey(deleteShardKey);
    addLogFailureCallback(future, "Delete shard key");
    return Futures.transform(
        future,
        response -> {
          if (!response.getResult()) {
            logger.error("Shard key '{}' could not be deleted for '{}'", shardKey, collectionName);
            throw new QdrantException(
                "Shard key " + shardKey + " could not be created for " + collectionName);
          }
          return response;
        },
        MoreExecutors.directExecutor());
  }

  // endregion

  // region Point Management

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
    return upsertAsync(collectionName, points, null);
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
    return upsertAsync(
        UpsertPoints.newBuilder()
            .setCollectionName(collectionName)
            .addAllPoints(points)
            .setWait(true)
            .build(),
        timeout);
  }

  /**
   * Perform insert and updates on points. If a point with a given ID already exists, it will be
   * overwritten.
   *
   * @param request The upsert points request
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> upsertAsync(UpsertPoints request) {
    return upsertAsync(request, null);
  }

  /**
   * Perform insert and updates on points. If a point with a given ID already exists, it will be
   * overwritten.
   *
   * @param request The upsert points request
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> upsertAsync(
      UpsertPoints request, @Nullable Duration timeout) {
    String collectionName = request.getCollectionName();
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");
    logger.debug("Upsert {} points into '{}'", request.getPointsList().size(), collectionName);
    ListenableFuture<PointsOperationResponse> future = getPoints(timeout).upsert(request);
    addLogFailureCallback(future, "Upsert");
    return Futures.transform(
        future, PointsOperationResponse::getResult, MoreExecutors.directExecutor());
  }

  /**
   * Deletes points. The call waits for the changes to be applied.
   *
   * @param collectionName The name of the collection.
   * @param ids The ids of points to delete.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteAsync(String collectionName, List<PointId> ids) {
    return deleteAsync(collectionName, ids, null);
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
    return deleteAsync(
        DeletePoints.newBuilder()
            .setCollectionName(collectionName)
            .setPoints(
                PointsSelector.newBuilder()
                    .setPoints(PointsIdsList.newBuilder().addAllIds(ids).build())
                    .build())
            .setWait(true)
            .build(),
        timeout);
  }

  /**
   * Deletes points. The call waits for the changes to be applied.
   *
   * @param collectionName The name of the collection.
   * @param filter A filter selecting the points to be deleted.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteAsync(String collectionName, Filter filter) {
    return deleteAsync(collectionName, filter, null);
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
    return deleteAsync(
        DeletePoints.newBuilder()
            .setCollectionName(collectionName)
            .setPoints(PointsSelector.newBuilder().setFilter(filter).build())
            .setWait(true)
            .build(),
        timeout);
  }

  /**
   * Deletes points.
   *
   * @param request The delete points request
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteAsync(DeletePoints request) {
    return deleteAsync(request, null);
  }

  /**
   * Deletes points.
   *
   * @param request The delete points request
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteAsync(
      DeletePoints request, @Nullable Duration timeout) {
    String collectionName = request.getCollectionName();
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");
    logger.debug("Delete from '{}'", collectionName);
    ListenableFuture<PointsOperationResponse> future = getPoints(timeout).delete(request);
    addLogFailureCallback(future, "Delete");
    return Futures.transform(
        future, PointsOperationResponse::getResult, MoreExecutors.directExecutor());
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
    return retrieveAsync(collectionName, id, true, false, readConsistency);
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
    return retrieveAsync(
        collectionName,
        ImmutableList.of(id),
        WithPayloadSelectorFactory.enable(withPayload),
        WithVectorsSelectorFactory.enable(withVectors),
        readConsistency);
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
    return retrieveAsync(collectionName, ids, true, false, readConsistency);
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
    return retrieveAsync(
        collectionName,
        ids,
        WithPayloadSelectorFactory.enable(withPayload),
        WithVectorsSelectorFactory.enable(withVectors),
        readConsistency);
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
    return retrieveAsync(
        collectionName, ids, payloadSelector, vectorsSelector, readConsistency, null);
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
    logger.debug("Retrieve points from '{}'", collectionName);
    GetPoints.Builder requestBuilder =
        GetPoints.newBuilder()
            .setCollectionName(collectionName)
            .addAllIds(ids)
            .setWithPayload(payloadSelector)
            .setWithVectors(vectorsSelector);

    if (readConsistency != null) {
      requestBuilder.setReadConsistency(readConsistency);
    }

    return retrieveAsync(requestBuilder.build(), timeout);
  }

  /**
   * Retrieves points.
   *
   * @param request The get points request
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<RetrievedPoint>> retrieveAsync(
      GetPoints request, @Nullable Duration timeout) {
    Preconditions.checkArgument(
        !request.getCollectionName().isEmpty(), "Collection name must not be empty");

    logger.debug("Retrieve points from '{}'", request.getCollectionName());
    ListenableFuture<GetResponse> future = getPoints(timeout).get(request);
    addLogFailureCallback(future, "Retrieve");
    return Futures.transform(future, GetResponse::getResultList, MoreExecutors.directExecutor());
  }

  // region Update Vectors

  /**
   * Update named vectors for point.
   *
   * @param collectionName The name of the collection.
   * @param points The list of points and vectors to update.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> updateVectorsAsync(
      String collectionName, List<PointVectors> points) {
    return updateVectorsAsync(collectionName, points, null, null, null);
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
    return updateVectorsAsync(collectionName, points, null, null, timeout);
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
    logger.debug("Update vectors in '{}'", collectionName);
    UpdatePointVectors.Builder requestBuilder =
        UpdatePointVectors.newBuilder()
            .setCollectionName(collectionName)
            .addAllPoints(points)
            .setWait(wait == null || wait);

    if (ordering != null) {
      requestBuilder.setOrdering(WriteOrdering.newBuilder().setType(ordering).build());
    }

    return updateVectorsAsync(requestBuilder.build(), timeout);
  }

  /**
   * Update named vectors for point.
   *
   * @param request The update point vectors request
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> updateVectorsAsync(
      UpdatePointVectors request, @Nullable Duration timeout) {
    Preconditions.checkArgument(
        !request.getCollectionName().isEmpty(), "Collection name must not be empty");
    logger.debug("Update vectors in '{}'", request.getCollectionName());
    ListenableFuture<PointsOperationResponse> future = getPoints(timeout).updateVectors(request);
    addLogFailureCallback(future, "Update vectors");
    return Futures.transform(
        future, PointsOperationResponse::getResult, MoreExecutors.directExecutor());
  }

  // endregion

  // region Delete Vectors

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
    return deleteVectorsAsync(
        collectionName,
        vectors,
        PointsSelector.newBuilder().setFilter(filter).build(),
        null,
        null,
        null);
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
    return deleteVectorsAsync(
        collectionName,
        vectors,
        PointsSelector.newBuilder().setFilter(filter).build(),
        null,
        null,
        timeout);
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
    return deleteVectorsAsync(
        collectionName,
        vectors,
        PointsSelector.newBuilder().setFilter(filter).build(),
        wait,
        ordering,
        timeout);
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
    return deleteVectorsAsync(
        collectionName,
        vectors,
        PointsSelector.newBuilder()
            .setPoints(PointsIdsList.newBuilder().addAllIds(ids).build())
            .build(),
        null,
        null,
        null);
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
    return deleteVectorsAsync(
        collectionName,
        vectors,
        PointsSelector.newBuilder()
            .setPoints(PointsIdsList.newBuilder().addAllIds(ids).build())
            .build(),
        null,
        null,
        timeout);
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
    return deleteVectorsAsync(
        collectionName,
        vectors,
        PointsSelector.newBuilder()
            .setPoints(PointsIdsList.newBuilder().addAllIds(ids).build())
            .build(),
        wait,
        ordering,
        timeout);
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
    logger.debug("Delete vectors in '{}'", collectionName);
    DeletePointVectors.Builder requestBuilder =
        DeletePointVectors.newBuilder()
            .setCollectionName(collectionName)
            .setVectors(VectorsSelector.newBuilder().addAllNames(vectors).build())
            .setPointsSelector(pointsSelector)
            .setWait(wait == null || wait);

    if (ordering != null) {
      requestBuilder.setOrdering(WriteOrdering.newBuilder().setType(ordering).build());
    }

    return deleteVectorsAsync(requestBuilder.build(), timeout);
  }

  /**
   * Delete named vectors for points.
   *
   * @param request The delete point vectors request
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deleteVectorsAsync(
      DeletePointVectors request, @Nullable Duration timeout) {
    Preconditions.checkArgument(
        !request.getCollectionName().isEmpty(), "Collection name must not be empty");
    logger.debug("Delete vectors in '{}'", request.getCollectionName());
    ListenableFuture<PointsOperationResponse> future = getPoints(timeout).deleteVectors(request);
    addLogFailureCallback(future, "Delete vectors");
    return Futures.transform(
        future, PointsOperationResponse::getResult, MoreExecutors.directExecutor());
  }

  // endregion

  // endregion

  // region Set Payload

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
    return setPayloadAsync(collectionName, payload, (PointsSelector) null, wait, ordering, timeout);
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
    return setPayloadAsync(
        collectionName,
        payload,
        PointsSelector.newBuilder()
            .setPoints(PointsIdsList.newBuilder().addIds(id).build())
            .build(),
        wait,
        ordering,
        timeout);
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
    return setPayloadAsync(
        collectionName,
        payload,
        PointsSelector.newBuilder()
            .setPoints(PointsIdsList.newBuilder().addAllIds(ids).build())
            .build(),
        wait,
        ordering,
        timeout);
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
    return setPayloadAsync(
        collectionName,
        payload,
        PointsSelector.newBuilder().setFilter(filter).build(),
        wait,
        ordering,
        timeout);
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
    return setPayloadAsync(collectionName, payload, pointsSelector, wait, null, ordering, timeout);
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
    SetPayloadPoints.Builder requestBuilder =
        SetPayloadPoints.newBuilder()
            .setCollectionName(collectionName)
            .setWait(wait == null || wait)
            .putAllPayload(payload);

    if (pointsSelector != null) {
      requestBuilder.setPointsSelector(pointsSelector);
    }

    if (ordering != null) {
      requestBuilder.setOrdering(WriteOrdering.newBuilder().setType(ordering).build());
    }

    if (key != null) {
      requestBuilder.setKey(key);
    }

    return setPayloadAsync(requestBuilder.build(), timeout);
  }

  /**
   * Sets the payload for the points.
   *
   * @param request The set payload request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> setPayloadAsync(
      SetPayloadPoints request, @Nullable Duration timeout) {
    Preconditions.checkArgument(
        !request.getCollectionName().isEmpty(), "Collection name must not be empty");
    logger.debug("Set payload in '{}'", request.getCollectionName());
    ListenableFuture<PointsOperationResponse> future = getPoints(timeout).setPayload(request);
    addLogFailureCallback(future, "Set payload");
    return Futures.transform(
        future, PointsOperationResponse::getResult, MoreExecutors.directExecutor());
  }

  // endregion

  // region Overwrite payload

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
    return overwritePayloadAsync(
        collectionName, payload, (PointsSelector) null, wait, ordering, timeout);
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
    return overwritePayloadAsync(
        collectionName,
        payload,
        PointsSelector.newBuilder()
            .setPoints(PointsIdsList.newBuilder().addIds(id).build())
            .build(),
        wait,
        ordering,
        timeout);
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
    return overwritePayloadAsync(
        collectionName,
        payload,
        PointsSelector.newBuilder()
            .setPoints(PointsIdsList.newBuilder().addAllIds(ids).build())
            .build(),
        wait,
        ordering,
        timeout);
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
    return overwritePayloadAsync(
        collectionName,
        payload,
        PointsSelector.newBuilder().setFilter(filter).build(),
        wait,
        ordering,
        timeout);
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
    return overwritePayloadAsync(
        collectionName, payload, pointsSelector, wait, null, ordering, timeout);
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
    SetPayloadPoints.Builder requestBuilder =
        SetPayloadPoints.newBuilder()
            .setCollectionName(collectionName)
            .setWait(wait == null || wait)
            .putAllPayload(payload);

    if (pointsSelector != null) {
      requestBuilder.setPointsSelector(pointsSelector);
    }

    if (ordering != null) {
      requestBuilder.setOrdering(WriteOrdering.newBuilder().setType(ordering).build());
    }

    if (key != null) requestBuilder.setKey(key);

    return overwritePayloadAsync(requestBuilder.build(), timeout);
  }

  /**
   * Overwrites the payload for the points.
   *
   * @param request The overwrite payload request
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> overwritePayloadAsync(
      SetPayloadPoints request, @Nullable Duration timeout) {
    Preconditions.checkArgument(
        !request.getCollectionName().isEmpty(), "Collection name must not be empty");
    logger.debug("Set payload in '{}'", request.getCollectionName());
    ListenableFuture<PointsOperationResponse> future = getPoints(timeout).overwritePayload(request);
    addLogFailureCallback(future, "Overwrite payload");
    return Futures.transform(
        future, PointsOperationResponse::getResult, MoreExecutors.directExecutor());
  }

  // endregion

  // region Delete Payload

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
    return deletePayloadAsync(collectionName, keys, (PointsSelector) null, wait, ordering, timeout);
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
    return deletePayloadAsync(
        collectionName,
        keys,
        PointsSelector.newBuilder()
            .setPoints(PointsIdsList.newBuilder().addIds(id).build())
            .build(),
        wait,
        ordering,
        timeout);
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
    return deletePayloadAsync(
        collectionName,
        keys,
        PointsSelector.newBuilder()
            .setPoints(PointsIdsList.newBuilder().addAllIds(ids).build())
            .build(),
        wait,
        ordering,
        timeout);
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
    return deletePayloadAsync(
        collectionName,
        keys,
        PointsSelector.newBuilder().setFilter(filter).build(),
        wait,
        ordering,
        timeout);
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
    DeletePayloadPoints.Builder requestBuilder =
        DeletePayloadPoints.newBuilder()
            .setCollectionName(collectionName)
            .setWait(wait == null || wait)
            .addAllKeys(keys);

    if (pointsSelector != null) {
      requestBuilder.setPointsSelector(pointsSelector);
    }

    if (ordering != null) {
      requestBuilder.setOrdering(WriteOrdering.newBuilder().setType(ordering).build());
    }

    return deletePayloadAsync(requestBuilder.build(), timeout);
  }

  /**
   * Delete specified key payload for the points.
   *
   * @param request The delete payload request
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deletePayloadAsync(
      DeletePayloadPoints request, @Nullable Duration timeout) {
    Preconditions.checkArgument(
        !request.getCollectionName().isEmpty(), "Collection name must not be empty");
    logger.debug("Delete payload in '{}'", request.getCollectionName());
    ListenableFuture<PointsOperationResponse> future = getPoints(timeout).deletePayload(request);
    addLogFailureCallback(future, "Delete payload");
    return Futures.transform(
        future, PointsOperationResponse::getResult, MoreExecutors.directExecutor());
  }

  // endregion

  // region Clear Payload

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
    return clearPayloadAsync(collectionName, (PointsSelector) null, wait, ordering, timeout);
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
    return clearPayloadAsync(
        collectionName,
        PointsSelector.newBuilder()
            .setPoints(PointsIdsList.newBuilder().addIds(id).build())
            .build(),
        wait,
        ordering,
        timeout);
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
    return clearPayloadAsync(
        collectionName,
        PointsSelector.newBuilder()
            .setPoints(PointsIdsList.newBuilder().addAllIds(ids).build())
            .build(),
        wait,
        ordering,
        timeout);
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
    return clearPayloadAsync(
        collectionName,
        PointsSelector.newBuilder().setFilter(filter).build(),
        wait,
        ordering,
        timeout);
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
    ClearPayloadPoints.Builder requestBuilder =
        ClearPayloadPoints.newBuilder()
            .setCollectionName(collectionName)
            .setWait(wait == null || wait);

    if (pointsSelector != null) {
      requestBuilder.setPoints(pointsSelector);
    }

    if (ordering != null) {
      requestBuilder.setOrdering(WriteOrdering.newBuilder().setType(ordering).build());
    }

    return clearPayloadAsync(requestBuilder.build(), timeout);
  }

  /**
   * Removes all payload for the points.
   *
   * @param request The clear payload request
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> clearPayloadAsync(
      ClearPayloadPoints request, @Nullable Duration timeout) {
    Preconditions.checkArgument(
        !request.getCollectionName().isEmpty(), "Collection name must not be empty");
    logger.debug("Clear payload in '{}'", request.getCollectionName());
    ListenableFuture<PointsOperationResponse> future = getPoints(timeout).clearPayload(request);
    addLogFailureCallback(future, "Clear payload");
    return Futures.transform(
        future, PointsOperationResponse::getResult, MoreExecutors.directExecutor());
  }

  // endregion

  /**
   * Creates a payload field index in a collection.
   *
   * @param collectionName The name of the collection.
   * @param field The field name to index.
   * @param schemaType The schema type of the field.
   * @param indexParams Payload index parameters.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> createPayloadIndexAsync(
      String collectionName,
      String field,
      PayloadSchemaType schemaType,
      @Nullable PayloadIndexParams indexParams,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    CreateFieldIndexCollection.Builder requestBuilder =
        CreateFieldIndexCollection.newBuilder()
            .setCollectionName(collectionName)
            .setFieldName(field)
            .setWait(wait == null || wait);

    switch (schemaType) {
      case Keyword:
        requestBuilder.setFieldType(FieldType.FieldTypeKeyword);
        break;
      case Integer:
        requestBuilder.setFieldType(FieldType.FieldTypeInteger);
        break;
      case Float:
        requestBuilder.setFieldType(FieldType.FieldTypeFloat);
        break;
      case Geo:
        requestBuilder.setFieldType(FieldType.FieldTypeGeo);
        break;
      case Text:
        requestBuilder.setFieldType(FieldType.FieldTypeText);
        break;
      case Bool:
        requestBuilder.setFieldType(FieldType.FieldTypeBool);
        break;
      case Datetime:
        requestBuilder.setFieldType(FieldType.FieldTypeDatetime);
        break;
      case Uuid:
        requestBuilder.setFieldType(FieldType.FieldTypeUuid);
        break;
      default:
        throw new IllegalArgumentException("Invalid schemaType: '" + schemaType + "'");
    }

    if (indexParams != null) {
      requestBuilder.setFieldIndexParams(indexParams);
    }

    if (ordering != null) {
      requestBuilder.setOrdering(WriteOrdering.newBuilder().setType(ordering).build());
    }

    return createPayloadIndexAsync(requestBuilder.build(), timeout);
  }

  /**
   * Creates a payload field index in a collection.
   *
   * @param request The create field index request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> createPayloadIndexAsync(
      CreateFieldIndexCollection request, @Nullable Duration timeout) {
    logger.debug(
        "Create payload field index for '{}' in '{}'",
        request.getFieldName(),
        request.getCollectionName());
    ListenableFuture<PointsOperationResponse> future = getPoints(timeout).createFieldIndex(request);
    addLogFailureCallback(future, "Create payload field index");
    return Futures.transform(
        future, PointsOperationResponse::getResult, MoreExecutors.directExecutor());
  }

  /**
   * Deletes a payload field index in a collection.
   *
   * @param collectionName The name of the collection.
   * @param field The field name to index.
   * @param wait Whether to wait until the changes have been applied. Defaults to <code>true</code>.
   * @param ordering Write ordering guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<UpdateResult> deletePayloadIndexAsync(
      String collectionName,
      String field,
      @Nullable Boolean wait,
      @Nullable WriteOrderingType ordering,
      @Nullable Duration timeout) {
    DeleteFieldIndexCollection.Builder requestBuilder =
        DeleteFieldIndexCollection.newBuilder()
            .setCollectionName(collectionName)
            .setFieldName(field)
            .setWait(wait == null || wait);

    if (ordering != null) {
      requestBuilder.setOrdering(WriteOrdering.newBuilder().setType(ordering).build());
    }

    logger.debug("Delete payload field index for '{}' in '{}'", field, collectionName);
    ListenableFuture<PointsOperationResponse> future =
        getPoints(timeout).deleteFieldIndex(requestBuilder.build());
    addLogFailureCallback(future, "Delete payload field index");
    return Futures.transform(
        future, PointsOperationResponse::getResult, MoreExecutors.directExecutor());
  }

  /**
   * Retrieves closest points based on vector similarity and the given filtering conditions.
   *
   * @param request the search request
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<ScoredPoint>> searchAsync(SearchPoints request) {
    return searchAsync(request, null);
  }

  /**
   * Retrieves closest points based on vector similarity and the given filtering conditions.
   *
   * @param request the search request
   * @param timeout the timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<ScoredPoint>> searchAsync(
      SearchPoints request, @Nullable Duration timeout) {
    Preconditions.checkArgument(
        !request.getCollectionName().isEmpty(), "Collection name must not be empty");
    Preconditions.checkArgument(!request.getVectorList().isEmpty(), "Vector must not be empty");

    logger.debug("Search on '{}'", request.getCollectionName());
    ListenableFuture<SearchResponse> future = getPoints(timeout).search(request);
    addLogFailureCallback(future, "Search");
    return Futures.transform(future, SearchResponse::getResultList, MoreExecutors.directExecutor());
  }

  /**
   * Retrieves closest points based on vector similarity and the given filtering conditions.
   *
   * @param collectionName The name of the collection
   * @param searches The searches to be performed in the batch.
   * @param readConsistency Options for specifying read consistency guarantees.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<BatchResult>> searchBatchAsync(
      String collectionName,
      List<SearchPoints> searches,
      @Nullable ReadConsistency readConsistency) {
    return searchBatchAsync(collectionName, searches, readConsistency, null);
  }

  /**
   * Retrieves closest points based on vector similarity and the given filtering conditions.
   *
   * @param collectionName The name of the collection
   * @param searches The searches to be performed in the batch.
   * @param readConsistency Options for specifying read consistency guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<BatchResult>> searchBatchAsync(
      String collectionName,
      List<SearchPoints> searches,
      @Nullable ReadConsistency readConsistency,
      @Nullable Duration timeout) {
    // TODO: Workaround for https://github.com/qdrant/qdrant/issues/2880
    searches =
        Lists.transform(
            searches,
            searchPoints -> searchPoints.toBuilder().setCollectionName(collectionName).build());

    SearchBatchPoints.Builder requestBuilder =
        SearchBatchPoints.newBuilder()
            .setCollectionName(collectionName)
            .addAllSearchPoints(searches);

    if (readConsistency != null) {
      requestBuilder.setReadConsistency(readConsistency);
    }

    logger.debug("Search batch on '{}'", collectionName);
    ListenableFuture<SearchBatchResponse> future =
        getPoints(timeout).searchBatch(requestBuilder.build());
    addLogFailureCallback(future, "Search batch");
    return Futures.transform(
        future, SearchBatchResponse::getResultList, MoreExecutors.directExecutor());
  }

  /**
   * Retrieves closest points based on vector similarity and the given filtering conditions, grouped
   * by a given field.
   *
   * @param request The search group request
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<PointGroup>> searchGroupsAsync(SearchPointGroups request) {
    return searchGroupsAsync(request, null);
  }

  /**
   * Retrieves closest points based on vector similarity and the given filtering conditions, grouped
   * by a given field.
   *
   * @param request The search group request
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<PointGroup>> searchGroupsAsync(
      SearchPointGroups request, @Nullable Duration timeout) {
    Preconditions.checkArgument(
        !request.getCollectionName().isEmpty(), "Collection name must not be empty");
    logger.debug("Search groups on '{}'", request.getCollectionName());
    ListenableFuture<SearchGroupsResponse> future = getPoints(timeout).searchGroups(request);
    addLogFailureCallback(future, "Search groups");
    return Futures.transform(
        future, response -> response.getResult().getGroupsList(), MoreExecutors.directExecutor());
  }

  /**
   * Iterates over all or filtered points.
   *
   * @param request The scroll request
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<ScrollResponse> scrollAsync(ScrollPoints request) {
    return scrollAsync(request, null);
  }

  /**
   * Iterates over all or filtered points.
   *
   * @param request The scroll request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<ScrollResponse> scrollAsync(
      ScrollPoints request, @Nullable Duration timeout) {
    Preconditions.checkArgument(
        !request.getCollectionName().isEmpty(), "Collection name must not be empty");
    logger.debug("Scroll on '{}'", request.getCollectionName());
    ListenableFuture<ScrollResponse> future = getPoints(timeout).scroll(request);
    addLogFailureCallback(future, "Scroll");
    return future;
  }

  /**
   * Look for the points which are closer to stored positive examples and at the same time further
   * to negative examples.
   *
   * @param request The recommend request
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<ScoredPoint>> recommendAsync(RecommendPoints request) {
    return recommendAsync(request, null);
  }

  /**
   * Look for the points which are closer to stored positive examples and at the same time further
   * to negative examples.
   *
   * @param request The recommend request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<ScoredPoint>> recommendAsync(
      RecommendPoints request, @Nullable Duration timeout) {
    Preconditions.checkArgument(
        !request.getCollectionName().isEmpty(), "Collection name must not be empty");
    logger.debug("Recommend on '{}'", request.getCollectionName());
    ListenableFuture<RecommendResponse> future = getPoints(timeout).recommend(request);
    addLogFailureCallback(future, "Recommend");
    return Futures.transform(
        future, RecommendResponse::getResultList, MoreExecutors.directExecutor());
  }

  /**
   * Look for the points which are closer to stored positive examples and at the same time further
   * to negative examples.
   *
   * @param collectionName The name of the collection.
   * @param recommendSearches The list of recommendation searches.
   * @param readConsistency Options for specifying read consistency guarantees.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<BatchResult>> recommendBatchAsync(
      String collectionName,
      List<RecommendPoints> recommendSearches,
      @Nullable ReadConsistency readConsistency) {
    return recommendBatchAsync(collectionName, recommendSearches, readConsistency, null);
  }

  /**
   * Look for the points which are closer to stored positive examples and at the same time further
   * to negative examples.
   *
   * @param collectionName The name of the collection.
   * @param recommendSearches The list of recommendation searches.
   * @param readConsistency Options for specifying read consistency guarantees.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<BatchResult>> recommendBatchAsync(
      String collectionName,
      List<RecommendPoints> recommendSearches,
      @Nullable ReadConsistency readConsistency,
      @Nullable Duration timeout) {
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");

    // TODO: Workaround for https://github.com/qdrant/qdrant/issues/2880
    recommendSearches =
        Lists.transform(
            recommendSearches,
            recommendPoints ->
                recommendPoints.toBuilder().setCollectionName(collectionName).build());

    RecommendBatchPoints.Builder requestBuilder =
        RecommendBatchPoints.newBuilder()
            .setCollectionName(collectionName)
            .addAllRecommendPoints(recommendSearches);

    if (readConsistency != null) {
      requestBuilder.setReadConsistency(readConsistency);
    }

    logger.debug("Recommend batch on '{}'", collectionName);
    ListenableFuture<RecommendBatchResponse> future =
        getPoints(timeout).recommendBatch(requestBuilder.build());
    addLogFailureCallback(future, "Recommend batch");
    return Futures.transform(
        future, RecommendBatchResponse::getResultList, MoreExecutors.directExecutor());
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
    return batchUpdateAsync(collectionName, operations, null, null, null);
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

    UpdateBatchPoints.Builder requestBuilder =
        UpdateBatchPoints.newBuilder()
            .setCollectionName(collectionName)
            .addAllOperations(operations)
            .setWait(wait == null || wait);

    if (ordering != null) {
      requestBuilder.setOrdering(ordering);
    }
    return batchUpdateAsync(requestBuilder.build(), timeout);
  }

  /**
   * Performs a batch update of points.
   *
   * @param request The update batch request.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<UpdateResult>> batchUpdateAsync(
      UpdateBatchPoints request, @Nullable Duration timeout) {
    String collectionName = request.getCollectionName();
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");
    logger.debug("Batch update points on '{}'", collectionName);
    ListenableFuture<UpdateBatchResponse> future = getPoints(timeout).updateBatch(request);
    addLogFailureCallback(future, "Batch update points");
    return Futures.transform(
        future, UpdateBatchResponse::getResultList, MoreExecutors.directExecutor());
  }

  /**
   * Look for the points which are closer to stored positive examples and at the same time further
   * to negative examples, grouped by a given field
   *
   * @param request The recommend groups request
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<PointGroup>> recommendGroupsAsync(RecommendPointGroups request) {
    return recommendGroupsAsync(request, null);
  }

  /**
   * Look for the points which are closer to stored positive examples and at the same time further
   * to negative examples, grouped by a given field
   *
   * @param request The recommend groups request
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<PointGroup>> recommendGroupsAsync(
      RecommendPointGroups request, @Nullable Duration timeout) {
    String collectionName = request.getCollectionName();
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");
    logger.debug("Recommend groups on '{}'", collectionName);
    ListenableFuture<RecommendGroupsResponse> future = getPoints(timeout).recommendGroups(request);
    addLogFailureCallback(future, "Recommend groups");
    return Futures.transform(
        future, response -> response.getResult().getGroupsList(), MoreExecutors.directExecutor());
  }

  /**
   * Use the context and a target to find the most similar points to the target. Constraints by the
   * context.
   *
   * @param request The discover points request
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<ScoredPoint>> discoverAsync(DiscoverPoints request) {
    return discoverAsync(request, null);
  }

  /**
   * Use the context and a target to find the most similar points to the target. Constraints by the
   * context.
   *
   * @param request The discover points request
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<ScoredPoint>> discoverAsync(
      DiscoverPoints request, @Nullable Duration timeout) {
    String collectionName = request.getCollectionName();
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");
    logger.debug("Discover on '{}'", collectionName);
    ListenableFuture<DiscoverResponse> future = getPoints(timeout).discover(request);
    addLogFailureCallback(future, "Discover");
    return Futures.transform(
        future, DiscoverResponse::getResultList, MoreExecutors.directExecutor());
  }

  /**
   * Use the context and a target to find the most similar points to the target in a batch.
   * Constrained by the context.
   *
   * @param collectionName The name of the collection
   * @param discoverSearches The list for discover point searches
   * @param readConsistency Options for specifying read consistency guarantees
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<BatchResult>> discoverBatchAsync(
      String collectionName,
      List<DiscoverPoints> discoverSearches,
      @Nullable ReadConsistency readConsistency) {
    return discoverBatchAsync(collectionName, discoverSearches, readConsistency, null);
  }

  /**
   * Use the context and a target to find the most similar points to the target in a batch.
   * Constrained by the context.
   *
   * @param collectionName The name of the collection
   * @param discoverSearches The list for discover point searches
   * @param readConsistency Options for specifying read consistency guarantees
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<BatchResult>> discoverBatchAsync(
      String collectionName,
      List<DiscoverPoints> discoverSearches,
      @Nullable ReadConsistency readConsistency,
      @Nullable Duration timeout) {
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");

    DiscoverBatchPoints.Builder requestBuilder =
        DiscoverBatchPoints.newBuilder()
            .setCollectionName(collectionName)
            .addAllDiscoverPoints(discoverSearches);

    if (readConsistency != null) {
      requestBuilder.setReadConsistency(readConsistency);
    }
    logger.debug("Discover batch on '{}'", collectionName);
    ListenableFuture<DiscoverBatchResponse> future =
        getPoints(timeout).discoverBatch(requestBuilder.build());
    addLogFailureCallback(future, "Discover batch");
    return Futures.transform(
        future, DiscoverBatchResponse::getResultList, MoreExecutors.directExecutor());
  }

  /**
   * Count the points in a collection. The count is exact
   *
   * @param collectionName The name of the collection.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<Long> countAsync(String collectionName) {
    return countAsync(collectionName, null, null, null);
  }

  /**
   * Count the points in a collection. The count is exact
   *
   * @param collectionName The name of the collection.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<Long> countAsync(String collectionName, @Nullable Duration timeout) {
    return countAsync(collectionName, null, null, timeout);
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
    return countAsync(collectionName, filter, exact, null);
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
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");
    CountPoints.Builder requestBuilder =
        CountPoints.newBuilder().setCollectionName(collectionName).setExact(exact == null || exact);

    if (filter != null) {
      requestBuilder.setFilter(filter);
    }

    logger.debug("Count on '{}'", collectionName);
    ListenableFuture<CountResponse> future = getPoints(timeout).count(requestBuilder.build());
    addLogFailureCallback(future, "Count");
    return Futures.transform(
        future, response -> response.getResult().getCount(), MoreExecutors.directExecutor());
  }

  /**
   * Universally query points. Covers all capabilities of search, recommend, discover, filters. Also
   * enables hybrid and multi-stage queries.
   *
   * @param request the query request
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<ScoredPoint>> queryAsync(QueryPoints request) {
    return queryAsync(request, null);
  }

  /**
   * Universally query points. Covers all capabilities of search, recommend, discover, filters. Also
   * enables hybrid and multi-stage queries.
   *
   * @param request the query request
   * @param timeout the timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<ScoredPoint>> queryAsync(
      QueryPoints request, @Nullable Duration timeout) {
    Preconditions.checkArgument(
        !request.getCollectionName().isEmpty(), "Collection name must not be empty");

    logger.debug("Query on '{}'", request.getCollectionName());
    ListenableFuture<QueryResponse> future = getPoints(timeout).query(request);
    addLogFailureCallback(future, "Query");
    return Futures.transform(future, QueryResponse::getResultList, MoreExecutors.directExecutor());
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
    return queryBatchAsync(collectionName, queries, null, null);
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
    return queryBatchAsync(collectionName, queries, readConsistency, null);
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
    QueryBatchPoints.Builder requestBuilder =
        QueryBatchPoints.newBuilder().setCollectionName(collectionName).addAllQueryPoints(queries);

    if (readConsistency != null) {
      requestBuilder.setReadConsistency(readConsistency);
    }

    logger.debug("Query batch on '{}'", collectionName);
    ListenableFuture<QueryBatchResponse> future =
        getPoints(timeout).queryBatch(requestBuilder.build());
    addLogFailureCallback(future, "Query batch");
    return Futures.transform(
        future, QueryBatchResponse::getResultList, MoreExecutors.directExecutor());
  }

  /**
   * Universally query points. Covers all capabilities of search, recommend, discover, filters.
   * Grouped by a payload field.
   *
   * @param request the query request
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<PointGroup>> queryGroupsAsync(QueryPointGroups request) {
    return queryGroupsAsync(request, null);
  }

  /**
   * Universally query points. Covers all capabilities of search, recommend, discover, filters.
   * Grouped by a payload field.
   *
   * @param request the query request
   * @param timeout the timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<PointGroup>> queryGroupsAsync(
      QueryPointGroups request, @Nullable Duration timeout) {
    Preconditions.checkArgument(
        !request.getCollectionName().isEmpty(), "Collection name must not be empty");

    logger.debug("Query groups on '{}'", request.getCollectionName());
    ListenableFuture<QueryGroupsResponse> future = getPoints(timeout).queryGroups(request);
    addLogFailureCallback(future, "Query groups");
    return Futures.transform(
        future, response -> response.getResult().getGroupsList(), MoreExecutors.directExecutor());
  }

  /**
   * Perform facet counts. For each value in the field, count the number of points that have this
   * value and match the conditions.
   *
   * @param request the facet counts request
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<Points.FacetHit>> facetAsync(Points.FacetCounts request) {
    return facetAsync(request, null);
  }

  /**
   * Perform facet counts. For each value in the field, count the number of points that have this
   * value and match the conditions.
   *
   * @param request the facet counts request
   * @param timeout the timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<Points.FacetHit>> facetAsync(
      Points.FacetCounts request, @Nullable Duration timeout) {
    Preconditions.checkArgument(
        !request.getCollectionName().isEmpty(), "Collection name must not be empty");

    logger.debug("Facet on '{}'", request.getCollectionName());
    ListenableFuture<Points.FacetResponse> future = getPoints(timeout).facet(request);
    addLogFailureCallback(future, "Facet");
    return Futures.transform(
        future, Points.FacetResponse::getHitsList, MoreExecutors.directExecutor());
  }

  // region distance matrix

  /**
   * Compute distance matrix for sampled points with a pair based output format.
   *
   * @param request the search matrix pairs request
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<Points.SearchMatrixPairs> searchMatrixPairsAsync(
      Points.SearchMatrixPoints request) {
    return searchMatrixPairsAsync(request, null);
  }

  /**
   * Compute distance matrix for sampled points with a pair based output format.
   *
   * @param request the search matrix pairs request
   * @param timeout the timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<Points.SearchMatrixPairs> searchMatrixPairsAsync(
      Points.SearchMatrixPoints request, @Nullable Duration timeout) {
    Preconditions.checkArgument(
        !request.getCollectionName().isEmpty(), "Collection name must not be empty");

    logger.debug("Search matrix pairs on '{}'", request.getCollectionName());
    ListenableFuture<Points.SearchMatrixPairsResponse> future =
        getPoints(timeout).searchMatrixPairs(request);
    addLogFailureCallback(future, "Search matrix pairs");
    return Futures.transform(
        future, Points.SearchMatrixPairsResponse::getResult, MoreExecutors.directExecutor());
  }

  /**
   * Compute distance matrix for sampled points with an offset based output format
   *
   * @param request the search matrix pairs request
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<Points.SearchMatrixOffsets> searchMatrixOffsetsAsync(
      Points.SearchMatrixPoints request) {
    return searchMatrixOffsetsAsync(request, null);
  }

  /**
   * Compute distance matrix for sampled points with an offset based output format
   *
   * @param request the search matrix pairs request
   * @param timeout the timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<Points.SearchMatrixOffsets> searchMatrixOffsetsAsync(
      Points.SearchMatrixPoints request, @Nullable Duration timeout) {
    Preconditions.checkArgument(
        !request.getCollectionName().isEmpty(), "Collection name must not be empty");

    logger.debug("Search matrix offsets on '{}'", request.getCollectionName());
    ListenableFuture<Points.SearchMatrixOffsetsResponse> future =
        getPoints(timeout).searchMatrixOffsets(request);
    addLogFailureCallback(future, "Search matrix offsets");
    return Futures.transform(
        future, Points.SearchMatrixOffsetsResponse::getResult, MoreExecutors.directExecutor());
  }

  // endregion

  // region Snapshot Management

  /**
   * Create snapshot for a given collection.
   *
   * @param collectionName The name of the collection.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<SnapshotDescription> createSnapshotAsync(String collectionName) {
    return createSnapshotAsync(collectionName, null);
  }

  /**
   * Create snapshot for a given collection.
   *
   * @param collectionName The name of the collection.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<SnapshotDescription> createSnapshotAsync(
      String collectionName, @Nullable Duration timeout) {
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");
    logger.debug("Create snapshot of '{}'", collectionName);
    ListenableFuture<CreateSnapshotResponse> future =
        getSnapshots(timeout)
            .create(CreateSnapshotRequest.newBuilder().setCollectionName(collectionName).build());
    addLogFailureCallback(future, "Create snapshot");
    return Futures.transform(
        future, CreateSnapshotResponse::getSnapshotDescription, MoreExecutors.directExecutor());
  }

  /**
   * Get list of snapshots for a collection.
   *
   * @param collectionName The name of the collection.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<SnapshotDescription>> listSnapshotAsync(String collectionName) {
    return listSnapshotAsync(collectionName, null);
  }

  /**
   * Get list of snapshots for a collection.
   *
   * @param collectionName The name of the collection.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<SnapshotDescription>> listSnapshotAsync(
      String collectionName, @Nullable Duration timeout) {
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");
    logger.debug("List snapshots of '{}'", collectionName);
    ListenableFuture<ListSnapshotsResponse> future =
        getSnapshots(timeout)
            .list(ListSnapshotsRequest.newBuilder().setCollectionName(collectionName).build());
    addLogFailureCallback(future, "List snapshots");
    return Futures.transform(
        future, ListSnapshotsResponse::getSnapshotDescriptionsList, MoreExecutors.directExecutor());
  }

  /**
   * Delete snapshot for a given collection.
   *
   * @param collectionName The name of the collection.
   * @param snapshotName The name of the snapshot.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<DeleteSnapshotResponse> deleteSnapshotAsync(
      String collectionName, String snapshotName) {
    return deleteSnapshotAsync(collectionName, snapshotName, null);
  }

  /**
   * Delete snapshot for a given collection.
   *
   * @param collectionName The name of the collection.
   * @param snapshotName The name of the snapshot.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<DeleteSnapshotResponse> deleteSnapshotAsync(
      String collectionName, String snapshotName, @Nullable Duration timeout) {
    Preconditions.checkArgument(!collectionName.isEmpty(), "Collection name must not be empty");
    Preconditions.checkArgument(!snapshotName.isEmpty(), "Snapshot name must not be empty");
    logger.debug("Delete snapshot '{}' of '{}'", snapshotName, collectionName);
    ListenableFuture<DeleteSnapshotResponse> future =
        getSnapshots(timeout)
            .delete(
                DeleteSnapshotRequest.newBuilder()
                    .setCollectionName(collectionName)
                    .setSnapshotName(snapshotName)
                    .build());
    addLogFailureCallback(future, "Delete snapshot");
    return future;
  }

  /**
   * Create snapshot for a whole storage.
   *
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<SnapshotDescription> createFullSnapshotAsync() {
    return createFullSnapshotAsync(null);
  }

  /**
   * Create snapshot for a whole storage.
   *
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<SnapshotDescription> createFullSnapshotAsync(@Nullable Duration timeout) {
    logger.debug("Create full snapshot for a whole storage");
    ListenableFuture<CreateSnapshotResponse> future =
        getSnapshots(timeout).createFull(CreateFullSnapshotRequest.getDefaultInstance());
    addLogFailureCallback(future, "Create full snapshot");
    return Futures.transform(
        future, CreateSnapshotResponse::getSnapshotDescription, MoreExecutors.directExecutor());
  }

  /**
   * Get list of snapshots for a whole storage.
   *
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<SnapshotDescription>> listFullSnapshotAsync() {
    return listFullSnapshotAsync(null);
  }

  /**
   * Get list of snapshots for a whole storage.
   *
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<List<SnapshotDescription>> listFullSnapshotAsync(
      @Nullable Duration timeout) {
    logger.debug("List full snapshots for a whole storage");
    ListenableFuture<ListSnapshotsResponse> future =
        getSnapshots(timeout).listFull(ListFullSnapshotsRequest.getDefaultInstance());
    addLogFailureCallback(future, "List full snapshots");
    return Futures.transform(
        future, ListSnapshotsResponse::getSnapshotDescriptionsList, MoreExecutors.directExecutor());
  }

  /**
   * Delete snapshot for a whole storage.
   *
   * @param snapshotName The name of the snapshot.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<DeleteSnapshotResponse> deleteFullSnapshotAsync(String snapshotName) {
    return deleteFullSnapshotAsync(snapshotName, null);
  }

  /**
   * Delete snapshot for a whole storage.
   *
   * @param snapshotName The name of the snapshot.
   * @param timeout The timeout for the call.
   * @return a new instance of {@link ListenableFuture}
   */
  public ListenableFuture<DeleteSnapshotResponse> deleteFullSnapshotAsync(
      String snapshotName, @Nullable Duration timeout) {
    Preconditions.checkArgument(!snapshotName.isEmpty(), "Snapshot name must not be empty");
    logger.debug("Delete full snapshot '{}'", snapshotName);
    ListenableFuture<DeleteSnapshotResponse> future =
        getSnapshots(timeout)
            .deleteFull(
                DeleteFullSnapshotRequest.newBuilder().setSnapshotName(snapshotName).build());
    addLogFailureCallback(future, "Delete full snapshot");
    return future;
  }

  // endregion

  @Override
  public void close() {
    grpcClient.close();
  }

  private <V> void addLogFailureCallback(ListenableFuture<V> future, String message) {
    Futures.addCallback(
        future,
        new FutureCallback<V>() {
          @Override
          public void onSuccess(V result) {}

          @Override
          public void onFailure(Throwable t) {
            logger.error(message + " operation failed", t);
          }
        },
        MoreExecutors.directExecutor());
  }

  private CollectionsGrpc.CollectionsFutureStub getCollections(@Nullable Duration timeout) {
    return timeout != null
        ? this.grpcClient.collections().withDeadlineAfter(timeout.toMillis(), TimeUnit.MILLISECONDS)
        : this.grpcClient.collections();
  }

  private PointsGrpc.PointsFutureStub getPoints(@Nullable Duration timeout) {
    return timeout != null
        ? this.grpcClient.points().withDeadlineAfter(timeout.toMillis(), TimeUnit.MILLISECONDS)
        : this.grpcClient.points();
  }

  private SnapshotsGrpc.SnapshotsFutureStub getSnapshots(@Nullable Duration timeout) {
    return timeout != null
        ? this.grpcClient.snapshots().withDeadlineAfter(timeout.toMillis(), TimeUnit.MILLISECONDS)
        : this.grpcClient.snapshots();
  }
}
