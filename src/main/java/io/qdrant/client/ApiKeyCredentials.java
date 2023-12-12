package io.qdrant.client;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;

import java.util.concurrent.Executor;

/**
 * API key authentication credentials
 */
public class ApiKeyCredentials extends CallCredentials {
    private final String apiKey;

	/**
	 * Instantiates a new instance of {@link ApiKeyCredentials}
	 * @param apiKey The API key to use for authentication
	 */
    public ApiKeyCredentials(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
        appExecutor.execute(() -> {
            try {
                Metadata headers = new Metadata();
                headers.put(Metadata.Key.of("api-key", Metadata.ASCII_STRING_MARSHALLER), apiKey);
                applier.apply(headers);
            } catch (Throwable e) {
                applier.fail(Status.UNAUTHENTICATED.withCause(e));
            }
        });
    }
}
