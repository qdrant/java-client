package io.qdrant.client;

import static io.qdrant.client.VectorInputFactory.multiVectorInput;
import static io.qdrant.client.VectorInputFactory.vectorInput;

import io.qdrant.client.grpc.Points.ContextInput;
import io.qdrant.client.grpc.Points.DiscoverInput;
import io.qdrant.client.grpc.Points.Fusion;
import io.qdrant.client.grpc.Points.OrderBy;
import io.qdrant.client.grpc.Points.PointId;
import io.qdrant.client.grpc.Points.Query;
import io.qdrant.client.grpc.Points.RecommendInput;
import io.qdrant.client.grpc.Points.Sample;
import io.qdrant.client.grpc.Points.VectorInput;
import java.util.List;
import java.util.UUID;

/** Convenience methods for constructing {@link Query} */
public final class QueryFactory {
  private QueryFactory() {}

  /**
   * Creates a {@link Query} for recommendation.
   *
   * @param input An instance of {@link RecommendInput}
   * @return a new instance of {@link Query}
   */
  public static Query recommend(RecommendInput input) {
    return Query.newBuilder().setRecommend(input).build();
  }

  /**
   * Creates a {@link Query} for discovery.
   *
   * @param input An instance of {@link DiscoverInput}
   * @return a new instance of {@link Query}
   */
  public static Query discover(DiscoverInput input) {
    return Query.newBuilder().setDiscover(input).build();
  }

  /**
   * Creates a {@link Query} for context search.
   *
   * @param input An instance of {@link ContextInput}
   * @return a new instance of {@link Query}
   */
  public static Query context(ContextInput input) {
    return Query.newBuilder().setContext(input).build();
  }

  /**
   * Creates a {@link Query} for pre-fetch results fusion.
   *
   * @param fusion An instance of {@link Fusion}
   * @return a new instance of {@link Query}
   */
  public static Query fusion(Fusion fusion) {
    return Query.newBuilder().setFusion(fusion).build();
  }

  /**
   * Creates a {@link Query} to order points by a payload field.
   *
   * @param key Name of the payload field to order by
   * @return a new instance of {@link Query}
   */
  public static Query orderBy(String key) {
    OrderBy orderBy = OrderBy.newBuilder().setKey(key).build();
    return Query.newBuilder().setOrderBy(orderBy).build();
  }

  /**
   * Creates a {@link Query} to order points by a payload field.
   *
   * @param orderBy An instance of {@link OrderBy}
   * @return a new instance of {@link Query}
   */
  public static Query orderBy(OrderBy orderBy) {
    return Query.newBuilder().setOrderBy(orderBy).build();
  }

  // region Nearest search queries

  /**
   * Creates a {@link Query} for nearest search.
   *
   * @param input An instance of {@link VectorInput}
   * @return a new instance of {@link Query}
   */
  public static Query nearest(VectorInput input) {
    return Query.newBuilder().setNearest(input).build();
  }

  /**
   * Creates a {@link Query} from a list of floats
   *
   * @param values A map of vector names to values
   * @return A new instance of {@link Query}
   */
  public static Query nearest(List<Float> values) {
    return Query.newBuilder().setNearest(vectorInput(values)).build();
  }

  /**
   * Creates a {@link Query} from a list of floats
   *
   * @param values A list of values
   * @return A new instance of {@link Query}
   */
  public static Query nearest(float... values) {
    return Query.newBuilder().setNearest(vectorInput(values)).build();
  }

  /**
   * Creates a {@link Query} from a list of floats and integers as indices
   *
   * @param values The list of floats representing the vector.
   * @param indices The list of integers representing the indices.
   * @return A new instance of {@link Query}
   */
  public static Query nearest(List<Float> values, List<Integer> indices) {
    return Query.newBuilder().setNearest(vectorInput(values, indices)).build();
  }

  /**
   * Creates a {@link Query} from a nested array of floats representing a multi vector
   *
   * @param vectors The nested array of floats.
   * @return A new instance of {@link Query}
   */
  public static Query nearest(float[][] vectors) {
    return Query.newBuilder().setNearest(multiVectorInput(vectors)).build();
  }

  /**
   * Creates a {@link Query} from a {@link long}
   *
   * @param id The point id
   * @return a new instance of {@link Query}
   */
  public static Query nearest(long id) {
    return Query.newBuilder().setNearest(vectorInput(id)).build();
  }

  /**
   * Creates a {@link Query} from a {@link UUID}
   *
   * @param id The pint id
   * @return a new instance of {@link Query}
   */
  public static Query nearest(UUID id) {
    return Query.newBuilder().setNearest(vectorInput(id)).build();
  }

  /**
   * Creates a {@link Query} from a {@link PointId}
   *
   * @param id The pint id
   * @return a new instance of {@link Query}
   */
  public static Query nearest(PointId id) {
    return Query.newBuilder().setNearest(vectorInput(id)).build();
  }

  /**
   * Creates a {@link Query} from a nested list of floats representing a multi vector
   *
   * @param vectors The nested list of floats.
   * @return A new instance of {@link Query}
   */
  public static Query nearestMultiVector(List<List<Float>> vectors) {
    return Query.newBuilder().setNearest(multiVectorInput(vectors)).build();
  }

  /**
   * Creates a {@link Query} for sampling.
   *
   * @param sample An instance of {@link Sample}
   * @return A new instance of {@link Query}
   */
  public static Query sample(Sample sample) {
    return Query.newBuilder().setSample(sample).build();
  }

  // endregion
}
