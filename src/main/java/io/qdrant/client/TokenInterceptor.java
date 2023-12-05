package io.qdrant.client;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

/** Interceptor for adding an API key to the headers of gRPC requests. */
final class TokenInterceptor implements ClientInterceptor {
  private final String apiKey;
  private final Metadata.Key<String> API_KEY =
      Metadata.Key.of("api-key", Metadata.ASCII_STRING_MARSHALLER);

  /**
   * Constructs a new TokenInterceptor with the specified API key.
   *
   * @param apiKey the API key to be added to the headers
   */
  TokenInterceptor(String apiKey) {
    this.apiKey = apiKey;
  }

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
      MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
    return new SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        headers.put(API_KEY, apiKey);
        super.start(responseListener, headers);
      }
    };
  }
}
