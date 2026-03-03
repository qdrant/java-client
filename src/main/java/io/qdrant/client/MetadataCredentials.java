package io.qdrant.client;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;

/** Used internally by the client to send the API key and any headers as gRPC metadata. */
public class MetadataCredentials extends CallCredentials {
  @Nullable private final String apiKey;
  private final Map<String, String> headers;

  /**
   * Instantiates a new instance of {@link MetadataCredentials}
   *
   * @param apiKey The API key to use for authentication.
   * @param headers Custom headers to send with every request.
   */
  public MetadataCredentials(@Nullable String apiKey, Map<String, String> headers) {
    this.apiKey = apiKey;
    this.headers = headers != null ? headers : Collections.emptyMap();
  }

  @Override
  public void applyRequestMetadata(
      RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
    appExecutor.execute(
        () -> {
          try {
            Metadata metadata = new Metadata();
            if (apiKey != null) {
              metadata.put(Metadata.Key.of("api-key", Metadata.ASCII_STRING_MARSHALLER), apiKey);
            }
            for (Map.Entry<String, String> entry : headers.entrySet()) {
              metadata.put(
                  Metadata.Key.of(entry.getKey(), Metadata.ASCII_STRING_MARSHALLER),
                  entry.getValue());
            }
            applier.apply(metadata);
          } catch (Throwable e) {
            applier.fail(Status.UNAUTHENTICATED.withCause(e));
          }
        });
  }
}
