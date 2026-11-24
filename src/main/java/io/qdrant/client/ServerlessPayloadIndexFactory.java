package io.qdrant.client;

import io.qdrant.client.grpc.serverless.ServerlessCollections.BoolIndex;
import io.qdrant.client.grpc.serverless.ServerlessCollections.DatetimeIndex;
import io.qdrant.client.grpc.serverless.ServerlessCollections.DisabledStemmer;
import io.qdrant.client.grpc.serverless.ServerlessCollections.FloatIndex;
import io.qdrant.client.grpc.serverless.ServerlessCollections.GeoIndex;
import io.qdrant.client.grpc.serverless.ServerlessCollections.IntegerIndex;
import io.qdrant.client.grpc.serverless.ServerlessCollections.KeywordIndex;
import io.qdrant.client.grpc.serverless.ServerlessCollections.PayloadIndexConfig;
import io.qdrant.client.grpc.serverless.ServerlessCollections.SnowballParams;
import io.qdrant.client.grpc.serverless.ServerlessCollections.StemmingAlgorithm;
import io.qdrant.client.grpc.serverless.ServerlessCollections.TextIndex;
import io.qdrant.client.grpc.serverless.ServerlessCollections.UuidIndex;

/**
 * Convenience methods for constructing Qdrant Serverless {@link PayloadIndexConfig} and {@link
 * StemmingAlgorithm}.
 */
public final class ServerlessPayloadIndexFactory {
  private ServerlessPayloadIndexFactory() {}

  /**
   * Creates a keyword index with default options.
   *
   * @return a new instance of {@link PayloadIndexConfig}
   */
  public static PayloadIndexConfig keyword() {
    return keyword(KeywordIndex.getDefaultInstance());
  }

  /**
   * Creates a keyword index.
   *
   * @param index The keyword index options.
   * @return a new instance of {@link PayloadIndexConfig}
   */
  public static PayloadIndexConfig keyword(KeywordIndex index) {
    return PayloadIndexConfig.newBuilder().setKeyword(index).build();
  }

  /**
   * Creates an integer index with default options.
   *
   * @return a new instance of {@link PayloadIndexConfig}
   */
  public static PayloadIndexConfig integer() {
    return integer(IntegerIndex.getDefaultInstance());
  }

  /**
   * Creates an integer index.
   *
   * @param index The integer index options.
   * @return a new instance of {@link PayloadIndexConfig}
   */
  public static PayloadIndexConfig integer(IntegerIndex index) {
    return PayloadIndexConfig.newBuilder().setInteger(index).build();
  }

  /**
   * Creates a float index with default options.
   *
   * @return a new instance of {@link PayloadIndexConfig}
   */
  public static PayloadIndexConfig floatIndex() {
    return floatIndex(FloatIndex.getDefaultInstance());
  }

  /**
   * Creates a float index.
   *
   * @param index The float index options.
   * @return a new instance of {@link PayloadIndexConfig}
   */
  public static PayloadIndexConfig floatIndex(FloatIndex index) {
    return PayloadIndexConfig.newBuilder().setFloat(index).build();
  }

  /**
   * Creates a UUID index with default options.
   *
   * @return a new instance of {@link PayloadIndexConfig}
   */
  public static PayloadIndexConfig uuid() {
    return uuid(UuidIndex.getDefaultInstance());
  }

  /**
   * Creates a UUID index.
   *
   * @param index The UUID index options.
   * @return a new instance of {@link PayloadIndexConfig}
   */
  public static PayloadIndexConfig uuid(UuidIndex index) {
    return PayloadIndexConfig.newBuilder().setUuid(index).build();
  }

  /**
   * Creates a datetime index with default options.
   *
   * @return a new instance of {@link PayloadIndexConfig}
   */
  public static PayloadIndexConfig datetime() {
    return datetime(DatetimeIndex.getDefaultInstance());
  }

  /**
   * Creates a datetime index.
   *
   * @param index The datetime index options.
   * @return a new instance of {@link PayloadIndexConfig}
   */
  public static PayloadIndexConfig datetime(DatetimeIndex index) {
    return PayloadIndexConfig.newBuilder().setDatetime(index).build();
  }

  /**
   * Creates a full-text index with default options.
   *
   * @return a new instance of {@link PayloadIndexConfig}
   */
  public static PayloadIndexConfig text() {
    return text(TextIndex.getDefaultInstance());
  }

  /**
   * Creates a full-text index.
   *
   * @param index The full-text index options.
   * @return a new instance of {@link PayloadIndexConfig}
   */
  public static PayloadIndexConfig text(TextIndex index) {
    return PayloadIndexConfig.newBuilder().setText(index).build();
  }

  /**
   * Creates a geo index with default options.
   *
   * @return a new instance of {@link PayloadIndexConfig}
   */
  public static PayloadIndexConfig geo() {
    return geo(GeoIndex.getDefaultInstance());
  }

  /**
   * Creates a geo index.
   *
   * @param index The geo index options.
   * @return a new instance of {@link PayloadIndexConfig}
   */
  public static PayloadIndexConfig geo(GeoIndex index) {
    return PayloadIndexConfig.newBuilder().setGeo(index).build();
  }

  /**
   * Creates a bool index with default options.
   *
   * @return a new instance of {@link PayloadIndexConfig}
   */
  public static PayloadIndexConfig bool() {
    return bool(BoolIndex.getDefaultInstance());
  }

  /**
   * Creates a bool index.
   *
   * @param index The bool index options.
   * @return a new instance of {@link PayloadIndexConfig}
   */
  public static PayloadIndexConfig bool(BoolIndex index) {
    return PayloadIndexConfig.newBuilder().setBool(index).build();
  }

  /**
   * Creates a Snowball stemmer for a full-text index.
   *
   * @param language The language whose words should be stemmed, e.g. "english".
   * @return a new instance of {@link StemmingAlgorithm}
   */
  public static StemmingAlgorithm snowballStemmer(String language) {
    return StemmingAlgorithm.newBuilder()
        .setSnowball(SnowballParams.newBuilder().setLanguage(language).build())
        .build();
  }

  /**
   * Explicitly disables stemming for a full-text index.
   *
   * @return a new instance of {@link StemmingAlgorithm}
   */
  public static StemmingAlgorithm disabledStemmer() {
    return StemmingAlgorithm.newBuilder().setDisabled(DisabledStemmer.getDefaultInstance()).build();
  }
}
