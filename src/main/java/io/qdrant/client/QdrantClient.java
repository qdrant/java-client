package io.qdrant.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.CollectionsGrpc;
import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points;
import io.qdrant.client.grpc.PointsGrpc;
import io.qdrant.client.grpc.QdrantGrpc;
import io.qdrant.client.grpc.QdrantOuterClass;
import io.qdrant.client.grpc.SnapshotsGrpc;
import io.qdrant.client.grpc.SnapshotsService;
import io.qdrant.client.utils.PointUtil;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/** Client for interfacing with the Qdrant service. */
public class QdrantClient {
  private QdrantGrpc.QdrantBlockingStub qdrantStub;
  private CollectionsGrpc.CollectionsBlockingStub collectionsStub;
  private PointsGrpc.PointsBlockingStub pointsStub;
  private SnapshotsGrpc.SnapshotsBlockingStub snapshotStub;

  /**
   * Constructs a new QdrantClient with the specified URL and API key<br>
   * Uses TLS if the URL is https, otherwise uses plaintext.
   *
   * @param url The URL of the Qdrant service.
   * @param apiKey The API key for authentication.
   * @throws MalformedURLException If the URL is malformed.
   * @throws IllegalArgumentException If the protocol is invalid.
   */
  public QdrantClient(String url, String apiKey)
      throws MalformedURLException, IllegalArgumentException {
    TokenInterceptor interceptor = new TokenInterceptor(apiKey);
    ManagedChannel channel = createManagedChannel(url, interceptor);
    initializeStubs(channel);
  }

  /**
   * Constructs a new QdrantClient with the specified URL<br>
   * Uses TLS if the URL is https, otherwise uses plaintext.
   *
   * @param url the URL of the Qdrant service
   * @throws MalformedURLException If the URL is malformed
   * @throws IllegalArgumentException If the protocol is invalid.
   */
  public QdrantClient(String url) throws MalformedURLException, IllegalArgumentException {
    ManagedChannel channel = createManagedChannel(url, null);
    initializeStubs(channel);
  }

  /**
   * Creates a managed channel based on the provided URL and interceptor.
   *
   * @param url The URL of the gRPC server.
   * @param interceptor The token interceptor to be added to the channel.
   * @return The created managed channel.
   * @throws MalformedURLException If the provided URL is malformed.
   * @throws IllegalArgumentException If the provided protocol is invalid.
   */
  private ManagedChannel createManagedChannel(String url, TokenInterceptor interceptor)
      throws MalformedURLException, IllegalArgumentException {
    URL parsedUrl = new URL(url);

    ManagedChannelBuilder<?> channelBuilder =
        ManagedChannelBuilder.forAddress(parsedUrl.getHost(), parsedUrl.getPort());

    switch (parsedUrl.getProtocol().toUpperCase()) {
      case "HTTPS":
        // TLS is enabled by default
        // Specifying explicitly for clarity
        channelBuilder.useTransportSecurity();
        break;
      case "HTTP":
        channelBuilder.usePlaintext();
        break;
      default:
        throw new IllegalArgumentException(
            "Invalid protocol. Only 'http' and 'https' are supported for gRPC URLs.");
    }

    if (interceptor != null) {
      // Add token interceptor if apiKey is provided
      channelBuilder.intercept(interceptor);
    }

    return channelBuilder.build();
  }

  /**
   * Initializes the gRPC stubs for Qdrant client.
   *
   * @param channel The managed channel used for communication.
   */
  private void initializeStubs(ManagedChannel channel) {
    qdrantStub = QdrantGrpc.newBlockingStub(channel);
    collectionsStub = CollectionsGrpc.newBlockingStub(channel);
    pointsStub = PointsGrpc.newBlockingStub(channel);
    snapshotStub = SnapshotsGrpc.newBlockingStub(channel);
  }

  /**
   * Retrieves a list of collections.
   *
   * @return The response containing the list of collections.
   */
  public Collections.ListCollectionsResponse listCollections() {
    Collections.ListCollectionsRequest request =
        Collections.ListCollectionsRequest.newBuilder().build();
    return collectionsStub.list(request);
  }

  /**
   * Performs a health check on the Qdrant service.
   *
   * @return The health check reply from the Qdrant service.
   */
  public QdrantOuterClass.HealthCheckReply healthCheck() {
    QdrantOuterClass.HealthCheckRequest request =
        QdrantOuterClass.HealthCheckRequest.newBuilder().build();
    return qdrantStub.healthCheck(request);
  }

  /**
   * Checks if a collection with the given name exists.
   *
   * @param collectionName The name of the collection to check.
   * @return True if the collection exists, false otherwise.
   */
  public boolean hasCollection(String collectionName) {
    return listCollections().getCollectionsList().stream()
        .anyMatch(c -> c.getName().equals(collectionName));
  }

  /**
   * Creates a new collection with the specified name, vector size, and distance metric.
   *
   * @param collectionName The name of the collection to be created.
   * @param vectorSize The size of the vectors in the collection.
   * @param distance The distance metric to be used for vector comparison.
   * @return The response containing the operation status.
   */
  public Collections.CollectionOperationResponse createCollection(
      String collectionName, long vectorSize, Collections.Distance distance) {
    Collections.VectorParams.Builder params =
        Collections.VectorParams.newBuilder().setDistance(distance).setSize(vectorSize);
    Collections.VectorsConfig config =
        Collections.VectorsConfig.newBuilder().setParams(params).build();
    Collections.CreateCollection details =
        Collections.CreateCollection.newBuilder()
            .setVectorsConfig(config)
            .setCollectionName(collectionName)
            .build();
    return createCollection(details);
  }

  /**
   * Creates a new collection with the specified details.
   *
   * @param details The details of the collection to be created.
   * @return The response containing the operation status.
   */
  public Collections.CollectionOperationResponse createCollection(
      Collections.CreateCollection details) {
    return collectionsStub.create(details);
  }

  /**
   * Deletes and creates a new collection with the specified name, vector size, and distance metric.
   *
   * @param collectionName The name of the collection to be created.
   * @param vectorSize The size of the vectors in the collection.
   * @param distance The distance metric to be used for vector comparison.
   * @return The response containing the operation status.
   */
  public Collections.CollectionOperationResponse recreateCollection(
      String collectionName, long vectorSize, Collections.Distance distance) {

    Collections.VectorParams.Builder params =
        Collections.VectorParams.newBuilder().setDistance(distance).setSize(vectorSize);
    Collections.VectorsConfig config =
        Collections.VectorsConfig.newBuilder().setParams(params).build();
    Collections.CreateCollection details =
        Collections.CreateCollection.newBuilder()
            .setVectorsConfig(config)
            .setCollectionName(collectionName)
            .build();
    return recreateCollection(details);
  }

  /**
   * Deletes and creates a new collection with the specified details.
   *
   * @param details The details of the collection to be created.
   * @return The response containing the operation status.
   */
  public Collections.CollectionOperationResponse recreateCollection(
      Collections.CreateCollection details) {
    deleteCollection(details.getCollectionName());
    return collectionsStub.create(details);
  }

  /**
   * Updates a collection with the specified details.
   *
   * @param details The details of the update operation.
   * @return The response containing the operation status.
   */
  public Collections.CollectionOperationResponse updateCollection(
      Collections.UpdateCollection details) {
    return collectionsStub.update(details);
  }

  /**
   * Deletes a collection with the specified name.
   *
   * @param collectionName the name of the collection to be deleted
   * @return the response of the collection deletion operation
   */
  public Collections.CollectionOperationResponse deleteCollection(String collectionName) {
    Collections.DeleteCollection request =
        Collections.DeleteCollection.newBuilder().setCollectionName(collectionName).build();
    return collectionsStub.delete(request);
  }

  /**
   * Retrieves information about a collection.
   *
   * @param collectionName The name of the collection.
   * @return The response containing the collection information.
   */
  public Collections.GetCollectionInfoResponse getCollectionInfo(String collectionName) {
    Collections.GetCollectionInfoRequest request =
        Collections.GetCollectionInfoRequest.newBuilder().setCollectionName(collectionName).build();
    return collectionsStub.get(request);
  }

  /**
   * Creates an alias for a collection.
   *
   * @param collectionName The name of the collection.
   * @param aliasName The name of the alias.
   * @return The response of the collection operation.
   */
  public Collections.CollectionOperationResponse createAlias(
      String collectionName, String aliasName) {
    Collections.CreateAlias createAlias =
        Collections.CreateAlias.newBuilder()
            .setCollectionName(collectionName)
            .setAliasName(aliasName)
            .build();
    Collections.AliasOperations operations =
        Collections.AliasOperations.newBuilder().setCreateAlias(createAlias).build();
    Collections.ChangeAliases changeAliases =
        Collections.ChangeAliases.newBuilder().addActions(operations).build();

    return this.updateAliases(changeAliases);
  }

  /**
   * Deletes an alias with the specified name.
   *
   * @param aliasName the name of the alias to be deleted
   * @return the response of the collection operation
   */
  public Collections.CollectionOperationResponse deleteAlias(String aliasName) {
    Collections.DeleteAlias deleteAlias =
        Collections.DeleteAlias.newBuilder().setAliasName(aliasName).build();
    Collections.AliasOperations operations =
        Collections.AliasOperations.newBuilder().setDeleteAlias(deleteAlias).build();
    Collections.ChangeAliases changeAliases =
        Collections.ChangeAliases.newBuilder().addActions(operations).build();

    return this.updateAliases(changeAliases);
  }

  /**
   * Renames an alias in the Qdrant collection.
   *
   * @param oldAliasName The current name of the alias.
   * @param newAliasName The new name for the alias.
   * @return The response containing the result of the alias rename operation.
   */
  public Collections.CollectionOperationResponse renameAlias(
      String oldAliasName, String newAliasName) {
    Collections.RenameAlias renameAlias =
        Collections.RenameAlias.newBuilder()
            .setOldAliasName(oldAliasName)
            .setNewAliasName(newAliasName)
            .build();
    Collections.AliasOperations operations =
        Collections.AliasOperations.newBuilder().setRenameAlias(renameAlias).build();
    Collections.ChangeAliases changeAliases =
        Collections.ChangeAliases.newBuilder().addActions(operations).build();
    return this.updateAliases(changeAliases);
  }

  /**
   * Updates the aliases for collections.
   *
   * @param details The details of the aliases to be changed.
   * @return The response of the collection operation.
   */
  public Collections.CollectionOperationResponse updateAliases(Collections.ChangeAliases details) {
    return collectionsStub.updateAliases(details);
  }

  /**
   * Retrieves the list of aliases for a given collection.
   *
   * @param collectionName The name of the collection.
   * @return The response containing the list of aliases.
   */
  public Collections.ListAliasesResponse listCollectionAliases(String collectionName) {
    Collections.ListCollectionAliasesRequest request =
        Collections.ListCollectionAliasesRequest.newBuilder()
            .setCollectionName(collectionName)
            .build();
    return collectionsStub.listCollectionAliases(request);
  }

  /**
   * Retrieves a list of aliases.
   *
   * @return The response containing the list of aliases.
   */
  public Collections.ListAliasesResponse listAliases() {
    Collections.ListAliasesRequest request = Collections.ListAliasesRequest.newBuilder().build();
    return collectionsStub.listAliases(request);
  }

  /**
   * Retrieves the cluster information for a specific collection.
   *
   * @param collectionName The name of the collection.
   * @return The cluster information for the collection.
   */
  public Collections.CollectionClusterInfoResponse getCollectionClusterInfo(String collectionName) {
    Collections.CollectionClusterInfoRequest request =
        Collections.CollectionClusterInfoRequest.newBuilder()
            .setCollectionName(collectionName)
            .build();
    return collectionsStub.collectionClusterInfo(request);
  }
  
  /**
   * Updates the cluster setup for a collection.
   *
   * @param collectionName The name of the collection.
   * @param request The request object containing the updated cluster setup.
   * @return The response object indicating the success or failure of the update operation.
   */
  public Collections.UpdateCollectionClusterSetupResponse updateCollectionClusterSetup(
      String collectionName, Collections.UpdateCollectionClusterSetupRequest request) {
    return collectionsStub.updateCollectionClusterSetup(request);
  }

  /** Internal batch update method */
  private Points.UpdateBatchResponse batchUpdate(
      String collectionName,
      List<Points.PointsUpdateOperation> operations,
      Points.WriteOrderingType ordering,
      Boolean wait) {
    Points.UpdateBatchPoints.Builder request =
        Points.UpdateBatchPoints.newBuilder()
            .setCollectionName(collectionName)
            .setWait(wait)
            .addAllOperations(operations);

    if (ordering != null) {
      request.setOrdering(PointUtil.ordering(ordering));
    }
    return pointsStub.updateBatch(request.build());
  }

  /**
   * Performs a batch update operation on a collection. Does not wait for the operation to complete
   * before returning.
   *
   * @param collectionName The name of the collection.
   * @param operations The list of update operations to be performed.
   * @return The response of the batch update operation.
   */
  public Points.UpdateBatchResponse batchUpdate(
      String collectionName,
      List<Points.PointsUpdateOperation> operations,
      Points.WriteOrderingType ordering) {
    return batchUpdate(collectionName, operations, ordering, false);
  }

  /**
   * Performs a batch update operation on a collection. Waits for the operation to complete before
   * returning.
   *
   * @param collectionName The name of the collection.
   * @param operations The list of update operations to be performed.
   * @return The response of the batch update operation.
   */
  public Points.UpdateBatchResponse batchUpdateBlocking(
      String collectionName,
      List<Points.PointsUpdateOperation> operations,
      Points.WriteOrderingType ordering) {
    return batchUpdate(collectionName, operations, ordering, true);
  }

  /** Internal upsert method */
  private Points.PointsOperationResponse upsertPoints(
      String collectionName,
      List<Points.PointStruct> points,
      Points.WriteOrderingType ordering,
      Boolean wait) {
    Points.UpsertPoints.Builder request =
        Points.UpsertPoints.newBuilder()
            .setCollectionName(collectionName)
            .addAllPoints(points)
            .setWait(wait);

    if (ordering != null) {
      request.setOrdering(PointUtil.ordering(ordering));
    }
    return pointsStub.upsert(request.build());
  }

  /**
   * Upserts the given points into the specified collection. Does not wait for the operation to
   * complete before returning.
   *
   * @param collectionName The name of the collection.
   * @param points The list of points to be upserted.
   * @param ordering The write ordering for the upsert operation.
   * @return The response of the upsert operation.
   */
  public Points.PointsOperationResponse upsertPoints(
      String collectionName, List<Points.PointStruct> points, Points.WriteOrderingType ordering) {
    return upsertPoints(collectionName, points, ordering, false);
  }

  /**
   * Upserts the given points into the specified collection. Waits for the operation to complete
   * before returning.
   *
   * @param collectionName The name of the collection.
   * @param points The list of points to be upserted.
   * @param ordering The write ordering for the upsert operation.
   * @return The response of the upsert operation.
   */
  public Points.PointsOperationResponse upsertPointsBlocking(
      String collectionName, List<Points.PointStruct> points, Points.WriteOrderingType ordering) {
    return upsertPoints(collectionName, points, ordering, true);
  }

  /** Internal batch upsert method */
  private Points.PointsOperationResponse upsertPointsBatch(
      String collectionName,
      List<Points.PointStruct> points,
      Points.WriteOrderingType ordering,
      Boolean wait,
      int chunkSize) {
    int listSize = points.size();
    double timeTaken = 0;
    Points.UpdateResult result = null;

    for (int i = 0; i < listSize; i += chunkSize) {
      int end = Math.min(i + chunkSize, listSize);
      List<Points.PointStruct> chunk = points.subList(i, end);
      Points.PointsOperationResponse response = upsertPoints(collectionName, chunk, ordering, wait);
      timeTaken += response.getTime();
      result = response.getResult();
    }
    return Points.PointsOperationResponse.newBuilder().setTime(timeTaken).setResult(result).build();
  }

  /**
   * Upserts a batch of points in the specified collection. Does not wait for the operation to
   * complete before returning.
   *
   * @param collectionName The name of the collection.
   * @param points The list of points to upsert.
   * @return The response of the points operation.
   */
  public Points.PointsOperationResponse upsertPointsBatch(
      String collectionName,
      List<Points.PointStruct> points,
      Points.WriteOrderingType ordering,
      int chunkSize) {
    return upsertPointsBatch(collectionName, points, ordering, false, chunkSize);
  }

  /**
   * Upserts a batch of points in the specified collection. Waits for the operation to complete
   * before returning.
   *
   * @param collectionName The name of the collection.
   * @param points The list of points to upsert.
   * @return The response of the points operation.
   */
  public Points.PointsOperationResponse upsertPointsBatchBlocking(
      String collectionName,
      List<Points.PointStruct> points,
      Points.WriteOrderingType ordering,
      int chunkSize) {
    return upsertPointsBatch(collectionName, points, ordering, true, chunkSize);
  }

  /** Internal update method */
  private Points.PointsOperationResponse setPayload(
      String collectionName,
      Points.PointsSelector points,
      Map<String, Value> payload,
      Points.WriteOrderingType ordering,
      Boolean wait) {
    Points.SetPayloadPoints.Builder request =
        Points.SetPayloadPoints.newBuilder()
            .setCollectionName(collectionName)
            .setPointsSelector(points)
            .putAllPayload(payload)
            .setWait(wait);

    if (ordering != null) {
      request.setOrdering(PointUtil.ordering(ordering));
    }
    return pointsStub.setPayload(request.build());
  }

  /**
   * Sets the payload of the specified points in a collection. Does not wait for the operation to
   * complete before returning.
   *
   * @param collectionName The name of the collection.
   * @param points The selector for the points to be updated.
   * @param payload The new payload to be assigned to the points.
   * @param ordering The ordering of the write operation.
   * @return The response of the points operation.
   */
  public Points.PointsOperationResponse setPayload(
      String collectionName,
      Points.PointsSelector points,
      Map<String, Value> payload,
      Points.WriteOrderingType ordering) {
    return setPayload(collectionName, points, payload, ordering, false);
  }

  /**
   * Sets the payload of the specified points in a collection. Waits for the operation to complete
   * before returning.
   *
   * @param collectionName The name of the collection.
   * @param points The selector for the points to be updated.
   * @param payload The new payload to be assigned to the points.
   * @param ordering The ordering of the write operation.
   * @return The response of the points operation.
   */
  public Points.PointsOperationResponse setPayloadBlocking(
      String collectionName,
      Points.PointsSelector points,
      Map<String, Value> payload,
      Points.WriteOrderingType ordering) {
    return setPayload(collectionName, points, payload, ordering, true);
  }

  /** Internal payload overwrite method */
  private Points.PointsOperationResponse overwritePayload(
      String collectionName,
      Points.PointsSelector points,
      Map<String, Value> payload,
      Points.WriteOrderingType ordering,
      Boolean wait) {
    Points.SetPayloadPoints.Builder request =
        Points.SetPayloadPoints.newBuilder()
            .setCollectionName(collectionName)
            .setPointsSelector(points)
            .putAllPayload(payload)
            .setWait(wait);

    if (ordering != null) {
      request.setOrdering(PointUtil.ordering(ordering));
    }
    return pointsStub.overwritePayload(request.build());
  }

  /**
   * Overwrites the payload of the specified points in a collection. Does not wait for the operation
   * to complete before returning.
   *
   * @param collectionName The name of the collection.
   * @param points The selector for the points to be overwritten.
   * @param payload The new payload to be assigned to the points.
   * @param ordering The ordering of the write operation.
   * @return The response of the points operation.
   */
  public Points.PointsOperationResponse overwritePayload(
      String collectionName,
      Points.PointsSelector points,
      Map<String, Value> payload,
      Points.WriteOrderingType ordering) {
    return overwritePayload(collectionName, points, payload, ordering, false);
  }

  /**
   * Overwrites the payload of the specified points in a collection. Waits for the operation to
   * complete before returning.
   *
   * @param collectionName The name of the collection.
   * @param points The selector for the points to be overwritten.
   * @param payload The new payload to be assigned to the points.
   * @param ordering The ordering of the write operation.
   * @return The response of the points operation.
   */
  public Points.PointsOperationResponse overwritePayloadBlocking(
      String collectionName,
      Points.PointsSelector points,
      Map<String, Value> payload,
      Points.WriteOrderingType ordering) {
    return overwritePayload(collectionName, points, payload, ordering, true);
  }

  /** Internal payload delete method */
  private Points.PointsOperationResponse deletePayload(
      String collectionName,
      Points.PointsSelector points,
      List<String> keys,
      Points.WriteOrderingType ordering,
      Boolean wait) {
    Points.DeletePayloadPoints.Builder request =
        Points.DeletePayloadPoints.newBuilder()
            .setCollectionName(collectionName)
            .addAllKeys(keys)
            .setWait(wait)
            .setPointsSelector(points);

    if (ordering != null) {
      request.setOrdering(PointUtil.ordering(ordering));
    }
    return pointsStub.deletePayload(request.build());
  }

  /**
   * Deletes the payload associated with the specified collection, points, keys, and ordering. Does
   * not wait for the operation to complete before returning.
   *
   * @param collectionName The name of the collection.
   * @param points The points selector.
   * @param keys The list of keys.
   * @param ordering The write ordering.
   * @return The response of the points operation.
   */
  public Points.PointsOperationResponse deletePayload(
      String collectionName,
      Points.PointsSelector points,
      List<String> keys,
      Points.WriteOrderingType ordering) {
    return deletePayload(collectionName, points, keys, ordering, false);
  }

  /**
   * Deletes the payload associated with the specified collection, points, keys, and ordering. Waits
   * for the operation to complete before returning.
   *
   * @param collectionName The name of the collection.
   * @param points The points selector.
   * @param keys The list of keys.
   * @param ordering The write ordering.
   * @return The response of the points operation.
   */
  public Points.PointsOperationResponse deletePayloadBlocking(
      String collectionName,
      Points.PointsSelector points,
      List<String> keys,
      Points.WriteOrderingType ordering) {
    return deletePayload(collectionName, points, keys, ordering, true);
  }

  /** Internal payload clear method */
  private Points.PointsOperationResponse clearPayload(
      String collectionName,
      Points.PointsSelector points,
      Points.WriteOrderingType ordering,
      Boolean wait) {
    Points.ClearPayloadPoints.Builder request =
        Points.ClearPayloadPoints.newBuilder()
            .setCollectionName(collectionName)
            .setPoints(points)
            .setWait(wait);

    if (ordering != null) {
      request.setOrdering(PointUtil.ordering(ordering));
    }

    return pointsStub.clearPayload(request.build());
  }

  /**
   * Clears the payload associated with the specified collection, points and ordering. Does not wait
   * for the operation to complete before returning.
   *
   * @param collectionName The name of the collection.
   * @param points The points to be cleared.
   * @return The response of the clearPayload operation.
   */
  public Points.PointsOperationResponse clearPayload(
      String collectionName, Points.PointsSelector points, Points.WriteOrderingType ordering) {
    return clearPayload(collectionName, points, ordering, false);
  }

  /**
   * Clears the payload associated with the specified collection, points and ordering. Waits for the
   * operation to complete before returning.
   *
   * @param collectionName The name of the collection.
   * @param points The points to be cleared.
   * @return The response of the clearPayload operation.
   */
  public Points.PointsOperationResponse clearPayloadBlocking(
      String collectionName, Points.PointsSelector points, Points.WriteOrderingType ordering) {
    return clearPayload(collectionName, points, ordering, true);
  }

  /**
   * Retrieves points from a collection.
   *
   * @param collectionName The name of the collection.
   * @param points The IDs of the points to retrieve.
   * @param withVectors The selector for including vectors in the response.
   * @param withPayload The selector for including payload in the response.
   * @param readConsistency The read consistency level for the operation.
   * @return The response containing the retrieved points.
   */
  public Points.GetResponse getPoints(
      String collectionName,
      Iterable<? extends Points.PointId> points,
      Points.WithVectorsSelector withVectors,
      Points.WithPayloadSelector withPayload,
      Points.ReadConsistencyType readConsistency) {
    Points.GetPoints.Builder request =
        Points.GetPoints.newBuilder()
            .setCollectionName(collectionName)
            .addAllIds(points)
            .setWithVectors(withVectors)
            .setWithPayload(withPayload);

    if (readConsistency != null) {
      request.setReadConsistency(PointUtil.consistency(readConsistency));
    }

    return pointsStub.get(request.build());
  }

  /**
   * Performs a search operation on the points.
   *
   * @param request The search request containing the query parameters.
   * @return The response containing the search results.
   */
  public Points.SearchResponse searchPoints(Points.SearchPoints request) {
    return pointsStub.search(request);
  }

  /**
   * Performs a batch search for points.
   *
   * @param request The search request containing the batch points to search for.
   * @return The response containing the search results.
   */
  public Points.SearchBatchResponse searchBatchPoints(Points.SearchBatchPoints request) {
    return pointsStub.searchBatch(request);
  }

  /**
   * Searches for point groups based on the given request.
   *
   * @param request The search request containing the criteria for searching point groups.
   * @return The response containing the search results for point groups.
   */
  public Points.SearchGroupsResponse searchGroups(Points.SearchPointGroups request) {
    return pointsStub.searchGroups(request);
  }

  /** Internal delete method */
  private Points.PointsOperationResponse deletePoints(
      String collectionName,
      Points.PointsSelector points,
      Points.WriteOrderingType ordering,
      Boolean wait) {
    Points.DeletePoints.Builder request =
        Points.DeletePoints.newBuilder()
            .setCollectionName(collectionName)
            .setPoints(points)
            .setWait(wait);

    if (ordering != null) {
      request.setOrdering(PointUtil.ordering(ordering));
    }
    return pointsStub.delete(request.build());
  }

  /**
   * Deletes points from a collection. Does not wait for the operation to complete before returning.
   *
   * @param collectionName The name of the collection from which points will be deleted.
   * @param points The selector for the points to be deleted.
   * @param ordering The ordering of the write operation.
   * @return The response of the points deletion operation.
   */
  public Points.PointsOperationResponse deletePoints(
      String collectionName, Points.PointsSelector points, Points.WriteOrderingType ordering) {
    return deletePoints(collectionName, points, ordering, false);
  }

  /**
   * Deletes points from a collection. Waits for the operation to complete before returning.
   *
   * @param collectionName The name of the collection from which points will be deleted.
   * @param points The selector for the points to be deleted.
   * @param ordering The ordering of the write operation.
   * @return The response of the points deletion operation.
   */
  public Points.PointsOperationResponse deletePointsBlocking(
      String collectionName, Points.PointsSelector points, Points.WriteOrderingType ordering) {
    return deletePoints(collectionName, points, ordering, true);
  }

  /** Internal delete vectors method */
  private Points.PointsOperationResponse deleteVectors(
      String collectionName,
      Points.PointsSelector points,
      Points.VectorsSelector vectors,
      Points.WriteOrderingType ordering,
      Boolean wait) {
    Points.DeletePointVectors.Builder requests =
        Points.DeletePointVectors.newBuilder()
            .setCollectionName(collectionName)
            .setPointsSelector(points)
            .setVectors(vectors)
            .setWait(wait);

    if (ordering != null) {
      requests.setOrdering(PointUtil.ordering(ordering));
    }
    return pointsStub.deleteVectors(requests.build());
  }

  /**
   * Deletes vectors from a collection. Does not wait for the operation to complete before
   * returning.
   *
   * @param collectionName The name of the collection.
   * @param points The selector for points to delete.
   * @param vectors The selector for vectors to delete.
   * @param ordering The write ordering for the operation.
   * @return The response of the delete operation.
   */
  public Points.PointsOperationResponse deleteVectors(
      String collectionName,
      Points.PointsSelector points,
      Points.VectorsSelector vectors,
      Points.WriteOrderingType ordering) {
    return deleteVectors(collectionName, points, vectors, ordering, false);
  }

  /**
   * Deletes vectors from a collection. Waits for the operation to complete before returning.
   *
   * @param collectionName The name of the collection.
   * @param points The selector for points to delete.
   * @param vectors The selector for vectors to delete.
   * @param ordering The write ordering for the operation.
   * @return The response of the delete operation.
   */
  public Points.PointsOperationResponse deleteVectorsBlocking(
      String collectionName,
      Points.PointsSelector points,
      Points.VectorsSelector vectors,
      Points.WriteOrderingType ordering) {
    return deleteVectors(collectionName, points, vectors, ordering, true);
  }

  /** Internal update vectors method */
  private Points.PointsOperationResponse updateVectors(
      String collectionName,
      Iterable<? extends Points.PointVectors> points,
      Points.WriteOrderingType ordering,
      Boolean wait) {
    Points.UpdatePointVectors.Builder request =
        Points.UpdatePointVectors.newBuilder()
            .setCollectionName(collectionName)
            .addAllPoints(points)
            .setWait(wait);

    if (ordering != null) {
      request.setOrdering(PointUtil.ordering(ordering));
    }
    return pointsStub.updateVectors(request.build());
  }

  /**
   * Updates the vectors of points in a collection. Does not wait for the operation to complete
   * before returning.
   *
   * @param collectionName The name of the collection.
   * @param points An iterable of point vectors to update.
   * @param ordering The write ordering for the update operation.
   * @return The response of the points operation.
   */
  public Points.PointsOperationResponse updateVectors(
      String collectionName,
      Iterable<? extends Points.PointVectors> points,
      Points.WriteOrderingType ordering) {
    return updateVectors(collectionName, points, ordering, false);
  }

  /**
   * Updates the vectors of points in a collection. Waits for the operation to complete before
   * returning.
   *
   * @param collectionName The name of the collection.
   * @param points An iterable of point vectors to update.
   * @param ordering The write ordering for the update operation.
   * @return The response of the points operation.
   */
  public Points.PointsOperationResponse updateVectorsBlocking(
      String collectionName,
      Iterable<? extends Points.PointVectors> points,
      Points.WriteOrderingType ordering) {
    return updateVectors(collectionName, points, ordering, true);
  }

  /**
   * Retrieve points from a collection based on filters.
   *
   * @param request The search request containing the query parameters.
   * @return The response containing the scroll results.
   */
  public Points.ScrollResponse scroll(Points.ScrollPoints request) {
    return pointsStub.scroll(request);
  }

  /**
   * Recommends points based on the given positive/negative points recommendation request.
   *
   * @param request The points recommendation request.
   * @return The recommendation response.
   */
  public Points.RecommendResponse recommend(Points.RecommendPoints request) {
    return pointsStub.recommend(request);
  }

  /**
   * Recommends points batch based on the given positive/negative points recommendation request.
   *
   * @param request The batch recommendation points request.
   * @return The response containing the recommended points.
   */
  public Points.RecommendBatchResponse recommendBatch(Points.RecommendBatchPoints request) {
    return pointsStub.recommendBatch(request);
  }

  /**
   * Recommends groups based on the given positive/negative points recommendation request.
   *
   * @param request The request containing the point groups to recommend.
   * @return The response containing the recommended groups.
   */
  public Points.RecommendGroupsResponse recommendGroups(Points.RecommendPointGroups request) {
    return pointsStub.recommendGroups(request);
  }

  /**
   * Counts the number of points in a collection based on the given filters.
   *
   * @param collectionName The name of the collection.
   * @param filter The filter to be applied.
   * @return The response containing the points count result.
   */
  public Points.CountResponse count(String collectionName, Points.Filter filter) {
    Points.CountPoints request =
        Points.CountPoints.newBuilder().setCollectionName(collectionName).setFilter(filter).build();
    return pointsStub.count(request);
  }

  /**
   * Counts the number of points in a collection based on the given filters.
   *
   * @param request The request containing the filters and options.
   * @return The response containing the points count result.
   */
  public Points.CountResponse count(Points.CountPoints request) {
    return pointsStub.count(request);
  }

  /** Internal update batch method */
  private Points.UpdateBatchResponse updateBatchPoints(
      String collecionName,
      Iterable<? extends Points.PointsUpdateOperation> operations,
      Points.WriteOrderingType ordering,
      Boolean wait) {
    Points.UpdateBatchPoints.Builder request =
        Points.UpdateBatchPoints.newBuilder()
            .setCollectionName(collecionName)
            .addAllOperations(operations)
            .setWait(wait);

    if (ordering != null) {
      request.setOrdering(PointUtil.ordering(ordering));
    }
    return pointsStub.updateBatch(request.build());
  }

  /**
   * Updates a batch of points in a collection. Does not wait for the operation to complete before
   * returning.
   *
   * @param collecionName The name of the collection.
   * @param operations The operations to be performed on the points.
   * @param ordering The ordering of the write operations.
   * @return The response of the batch points update operation.
   */
  public Points.UpdateBatchResponse updateBatchPoints(
      String collecionName,
      Iterable<? extends Points.PointsUpdateOperation> operations,
      Points.WriteOrderingType ordering) {
    return updateBatchPoints(collecionName, operations, ordering, false);
  }

  /**
   * Updates a batch of points in a collection. Waits for the operation to complete before
   * returning.
   *
   * @param collectionName The name of the collection.
   * @param operations The operations to be performed on the points.
   * @param ordering The ordering of the write operations.
   * @return The response of the batch points update operation.
   */
  public Points.UpdateBatchResponse updateBatchPointsBlocking(
      String collectionName,
      Iterable<? extends Points.PointsUpdateOperation> operations,
      Points.WriteOrderingType ordering) {
    return updateBatchPoints(collectionName, operations, ordering, true);
  }

  /** Internal create field index method */
  private Points.PointsOperationResponse createFieldIndex(
      String collectionName,
      String fieldName,
      Points.FieldType fieldType,
      Collections.PayloadIndexParams fieldIndexParams,
      Points.WriteOrderingType ordering,
      Boolean wait) {
    Points.CreateFieldIndexCollection.Builder request =
        Points.CreateFieldIndexCollection.newBuilder()
            .setCollectionName(collectionName)
            .setFieldName(fieldName)
            .setFieldType(fieldType)
            .setFieldIndexParams(fieldIndexParams)
            .setWait(wait);

    if (ordering != null) {
      request.setOrdering(PointUtil.ordering(ordering));
    }
    return pointsStub.createFieldIndex(request.build());
  }

  /**
   * Creates a field index in the specified collection with the given parameters. Does not wait for
   * the operation to complete before returning.
   *
   * @param collectionName The name of the collection.
   * @param fieldName The name of the field.
   * @param fieldType The type of the field.
   * @param fieldIndexParams The index parameters for the field.
   * @param ordering The write ordering for the field.
   * @return The response of the field index creation operation.
   */
  public Points.PointsOperationResponse createFieldIndex(
      String collectionName,
      String fieldName,
      Points.FieldType fieldType,
      Collections.PayloadIndexParams fieldIndexParams,
      Points.WriteOrderingType ordering) {
    return createFieldIndex(
        collectionName, fieldName, fieldType, fieldIndexParams, ordering, false);
  }

  /**
   * Creates a field index in the specified collection with the given parameters. Waits for the
   * operation to complete before returning.
   *
   * @param collectionName The name of the collection.
   * @param fieldName The name of the field.
   * @param fieldType The type of the field.
   * @param fieldIndexParams The index parameters for the field.
   * @param ordering The write ordering for the field.
   * @return The response of the field index creation operation.
   */
  public Points.PointsOperationResponse createFieldIndexBlocking(
      String collectionName,
      String fieldName,
      Points.FieldType fieldType,
      Collections.PayloadIndexParams fieldIndexParams,
      Points.WriteOrderingType ordering) {
    return createFieldIndex(collectionName, fieldName, fieldType, fieldIndexParams, ordering, true);
  }

  /** Internal delete field index method */
  private Points.PointsOperationResponse deleteFieldIndex(
      String collectionName, String fieldName, Points.WriteOrderingType ordering, Boolean wait) {
    Points.DeleteFieldIndexCollection.Builder request =
        Points.DeleteFieldIndexCollection.newBuilder()
            .setCollectionName(collectionName)
            .setFieldName(fieldName)
            .setWait(wait);

    if (ordering != null) {
      request.setOrdering(PointUtil.ordering(ordering));
    }
    return pointsStub.deleteFieldIndex(request.build());
  }

  /**
   * Deletes the field index for a given collection and field name. Does not wait for the operation
   * to complete before returning.
   *
   * @param collectionName The name of the collection.
   * @param fieldName The name of the field.
   * @param ordering The write ordering for the operation.
   * @return The response of the delete operation.
   */
  public Points.PointsOperationResponse deleteFieldIndex(
      String collectionName, String fieldName, Points.WriteOrderingType ordering) {
    return deleteFieldIndex(collectionName, fieldName, ordering, false);
  }

  /**
   * Deletes the field index for a given collection and field name. Waits for the operation to
   * complete before returning.
   *
   * @param collectionName The name of the collection.
   * @param fieldName The name of the field.
   * @param ordering The write ordering for the operation.
   * @return The response of the delete operation.
   */
  public Points.PointsOperationResponse deleteFieldIndexBlocking(
      String collectionName, String fieldName, Points.WriteOrderingType ordering) {
    return deleteFieldIndex(collectionName, fieldName, ordering, true);
  }

  /**
   * Creates a snapshot of a collection.
   *
   * @param collectionName the name of the collection
   * @return The response containing information about the created snapshot
   */
  public SnapshotsService.CreateSnapshotResponse createSnapshot(String collectionName) {
    SnapshotsService.CreateSnapshotRequest request =
        SnapshotsService.CreateSnapshotRequest.newBuilder()
            .setCollectionName(collectionName)
            .build();
    return snapshotStub.create(request);
  }

  /**
   * Retrieves a list of snapshots for a given collection.
   *
   * @param collectionName the name of the collection
   * @return The response containing the list of snapshots
   */
  public SnapshotsService.ListSnapshotsResponse listSnapshots(String collectionName) {
    SnapshotsService.ListSnapshotsRequest request =
        SnapshotsService.ListSnapshotsRequest.newBuilder()
            .setCollectionName(collectionName)
            .build();
    return snapshotStub.list(request);
  }

  /**
   * Deletes a snapshot with the specified name from the given collection.
   *
   * @param collectionName The name of the collection.
   * @param snapshotName The name of the snapshot to be deleted.
   * @return The response indicating the success or failure of the snapshot deletion.
   */
  public SnapshotsService.DeleteSnapshotResponse deleteSnapshot(
      String collectionName, String snapshotName) {
    SnapshotsService.DeleteSnapshotRequest request =
        SnapshotsService.DeleteSnapshotRequest.newBuilder()
            .setCollectionName(collectionName)
            .setSnapshotName(snapshotName)
            .build();
    return snapshotStub.delete(request);
  }

  /**
   * Creates a full snapshot of the Qdrant database.
   *
   * @return The response indicating the status of the snapshot creation.
   */
  public SnapshotsService.CreateSnapshotResponse createFullSnapshot() {
    SnapshotsService.CreateFullSnapshotRequest request =
        SnapshotsService.CreateFullSnapshotRequest.newBuilder().build();
    return snapshotStub.createFull(request);
  }

  /**
   * Retrieves a list of full snapshots for a given collection.
   *
   * @return The response containing the list of full snapshots.
   */
  public SnapshotsService.ListSnapshotsResponse listFullSnapshots() {
    SnapshotsService.ListFullSnapshotsRequest request =
        SnapshotsService.ListFullSnapshotsRequest.newBuilder().build();
    return snapshotStub.listFull(request);
  }

  /**
   * Deletes a full snapshot.
   *
   * @param snapshotName the name of the snapshot to delete.
   * @return The response indicating the status of the snapshot deletion.
   */
  public SnapshotsService.DeleteSnapshotResponse deleteFullSnapshot(String snapshotName) {
    SnapshotsService.DeleteFullSnapshotRequest request =
        SnapshotsService.DeleteFullSnapshotRequest.newBuilder()
            .setSnapshotName(snapshotName)
            .build();
    return snapshotStub.deleteFull(request);
  }

  // TODO: Download snapshots REST
}
