package io.qdrant.client;

import io.grpc.*;

/** An Interceptor that handles Resource Exhausted errors */
public class ResourceExhaustedInterceptor implements ClientInterceptor {
  /** Default constructor for {@link ResourceExhaustedInterceptor} */
  public ResourceExhaustedInterceptor() {}

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
      MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
    return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
        next.newCall(method, callOptions)) {

      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        super.start(
            new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(
                responseListener) {
              @Override
              public void onClose(Status status, Metadata trailers) {
                if (status.getCode() == Status.Code.RESOURCE_EXHAUSTED) {
                  String retryAfter =
                      trailers.get(
                          Metadata.Key.of("Retry-After", Metadata.ASCII_STRING_MARSHALLER));
                  if (retryAfter != null) {
                    try {
                      int retryAfterSeconds = Integer.parseInt(retryAfter);
                      status =
                          status.withCause(
                              new ResourceExhaustedResponse(
                                  "Too many requests", retryAfterSeconds));
                    } catch (NumberFormatException e) {
                      throw new QdrantException(
                          "Retry-After header value is not a valid integer: " + retryAfter);
                    }
                  } else {
                    super.onClose(status, trailers);
                  }
                }
                super.onClose(status, trailers);
              }
            },
            headers);
      }
    };
  }
}
