package io.qdrant.client;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Context;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for attaching per-request headers to gRPC calls.
 *
 * <pre>{@code
 * Context ctx = RequestHeaders.withHeader(Context.current(), "x-request-id", "abc-123");
 * ctx.run(() -> client.listCollectionsAsync());
 * }</pre>
 */
public final class RequestHeaders {

  static final Context.Key<Map<String, String>> HEADERS_KEY = Context.key("qdrant-request-headers");

  private RequestHeaders() {}

  /**
   * Returns a new {@link Context} that carries key/value as a gRPC metadata header on every request
   * started within that context.
   *
   * @param ctx the parent context
   * @param key the header name
   * @param value the header value
   * @return a child context with the header attached
   */
  public static Context withHeader(Context ctx, String key, String value) {
    return withHeaders(ctx, Collections.singletonMap(key, value));
  }

  /**
   * Returns a new {@link Context} that carries all entries of headers as gRPC metadata on every
   * request started within that context.
   *
   * @param ctx the parent context
   * @param headers the headers to attach
   * @return a child context with the headers attached
   */
  public static Context withHeaders(Context ctx, Map<String, String> headers) {
    if (headers == null || headers.isEmpty()) {
      return ctx;
    }
    Map<String, String> merged = new HashMap<>();
    Map<String, String> current = HEADERS_KEY.get(ctx);
    if (current != null) merged.putAll(current);
    merged.putAll(headers);
    return ctx.withValue(HEADERS_KEY, merged);
  }

  /** Returns a {@link ClientInterceptor} that injects per-request headers from the context. */
  static ClientInterceptor newInterceptor() {
    return new ClientInterceptor() {
      @Override
      public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
          MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            next.newCall(method, callOptions)) {
          @Override
          public void start(Listener<RespT> responseListener, Metadata headers) {
            Map<String, String> extra = HEADERS_KEY.get();
            if (extra != null) {
              for (Map.Entry<String, String> entry : extra.entrySet()) {
                headers.put(
                    Metadata.Key.of(entry.getKey(), Metadata.ASCII_STRING_MARSHALLER),
                    entry.getValue());
              }
            }
            super.start(responseListener, headers);
          }
        };
      }
    };
  }
}
