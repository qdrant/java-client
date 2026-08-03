package io.qdrant.client;

import io.qdrant.client.grpc.Collections.DisabledStemmer;
import io.qdrant.client.grpc.Collections.SnowballParams;
import io.qdrant.client.grpc.Collections.StemmingAlgorithm;

/** Convenience methods for constructing {@link StemmingAlgorithm}. */
public final class StemmingAlgorithmFactory {
  private StemmingAlgorithmFactory() {}

  /**
   * Creates a Snowball stemming algorithm for the given language.
   *
   * @param language The language whose words should be stemmed
   * @return a new instance of {@link StemmingAlgorithm}
   */
  public static StemmingAlgorithm snowball(String language) {
    return StemmingAlgorithm.newBuilder()
        .setSnowball(SnowballParams.newBuilder().setLanguage(language).build())
        .build();
  }

  /**
   * Explicitly disables stemming, overriding the language default.
   *
   * @return a new instance of {@link StemmingAlgorithm}
   */
  public static StemmingAlgorithm disabled() {
    return StemmingAlgorithm.newBuilder().setDisabled(DisabledStemmer.getDefaultInstance()).build();
  }
}
