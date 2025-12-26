package io.qdrant.client;

import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import javax.net.ssl.SSLException;

/** Represents a builder for a channel that skips TLS verification. */
public class TlsSkipVerifyChannelBuilder {

  private TlsSkipVerifyChannelBuilder() {}

  /**
   * build a channel that skips TLS verification
   *
   * @param builder the builder to configure
   */
  public static void build(ManagedChannelBuilder<?> builder) {
    if (!(builder instanceof NettyChannelBuilder)) {
      return;
    }
    try {
      NettyChannelBuilder nettyChannelBuilder = (NettyChannelBuilder) builder;
      nettyChannelBuilder.sslContext(
          GrpcSslContexts.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build());
    } catch (SSLException e) {
      throw new RuntimeException(e);
    }
  }
}
