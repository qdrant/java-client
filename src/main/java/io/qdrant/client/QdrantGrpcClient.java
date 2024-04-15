package io.qdrant.client;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.CallCredentials;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.qdrant.client.grpc.CollectionsGrpc;
import io.qdrant.client.grpc.CollectionsGrpc.CollectionsFutureStub;
import io.qdrant.client.grpc.PointsGrpc;
import io.qdrant.client.grpc.PointsGrpc.PointsFutureStub;
import io.qdrant.client.grpc.QdrantGrpc;
import io.qdrant.client.grpc.QdrantGrpc.QdrantFutureStub;
import io.qdrant.client.grpc.SnapshotsGrpc;
import io.qdrant.client.grpc.SnapshotsGrpc.SnapshotsFutureStub;

/**
 * Low-level gRPC client for qdrant vector database.
 */
public class QdrantGrpcClient implements AutoCloseable {
	private static final Logger logger = LoggerFactory.getLogger(QdrantGrpcClient.class);
	@Nullable
	private final CallCredentials callCredentials;
	private final ManagedChannel channel;
	private final boolean shutdownChannelOnClose;
	@Nullable
	private final Duration timeout;

	QdrantGrpcClient(
		ManagedChannel channel,
		boolean shutdownChannelOnClose,
		@Nullable CallCredentials callCredentials,
		@Nullable Duration timeout) {
		this.callCredentials = callCredentials;
		this.channel = channel;
		this.shutdownChannelOnClose = shutdownChannelOnClose;
		this.timeout = timeout;
	}

	/**
	 * Creates a new builder to build a client.
	 * @param channel The channel for communication. This channel is not shutdown by the client and must be managed
	 * by the caller.
	 * @return a new instance of {@link Builder}
	 */
	public static Builder newBuilder(ManagedChannel channel) {
		return new Builder(channel, false);
	}

	/**
	 * Creates a new builder to build a client.
	 * @param channel The channel for communication.
	 * @param shutdownChannelOnClose Whether the channel is shutdown on client close.
	 * @return a new instance of {@link Builder}
	 */
	public static Builder newBuilder(ManagedChannel channel, boolean shutdownChannelOnClose) {
		return new Builder(channel, shutdownChannelOnClose);
	}

	/**
	 * Creates a new builder to build a client.
	 * @param host The host to connect to. The default gRPC port 6334 is used.
	 * @return a new instance of {@link Builder}
	 */
	public static Builder newBuilder(String host) {
		return new Builder(host, 6334, true);
	}

	/**
	 * Creates a new builder to build a client. The client uses Transport Layer Security by default.
	 * @param host The host to connect to.
	 * @param port The port to connect to.
	 * @return a new instance of {@link Builder}
	 */
	public static Builder newBuilder(String host, int port) {
		return new Builder(host, port, true);
	}

	/**
	 * Creates a new builder to build a client.
	 * @param host The host to connect to.
	 * @param port The port to connect to.
	 * @param useTransportLayerSecurity Whether the client uses Transport Layer Security (TLS) to secure communications.
	 *  Running without TLS should only be used for testing purposes.
	 * @return a new instance of {@link Builder}
	 */
	public static Builder newBuilder(String host, int port, boolean useTransportLayerSecurity) {
		return new Builder(host, port, useTransportLayerSecurity);
	}

	/**
	 * Gets the channel
	 * @return the channel
	 */
	public ManagedChannel channel() {
		return channel;
	}

	/**
	 * Gets the client for qdrant services
	 * @return a new instance of {@link QdrantFutureStub}
	 */
	public QdrantGrpc.QdrantFutureStub qdrant() {
		return QdrantGrpc.newFutureStub(channel)
			.withCallCredentials(callCredentials)
			.withDeadline(timeout != null ? Deadline.after(timeout.toMillis(), TimeUnit.MILLISECONDS) : null);
	}

	/**
	 * Gets the client for points
	 * @return a new instance of {@link PointsFutureStub}
	 */
	public PointsFutureStub points() {
		return PointsGrpc.newFutureStub(channel)
			.withCallCredentials(callCredentials)
			.withDeadline(timeout != null ? Deadline.after(timeout.toMillis(), TimeUnit.MILLISECONDS) : null);
	}

	/**
	 * Gets the client for collections
	 * @return a new instance of {@link CollectionsFutureStub}
	 */
	public CollectionsFutureStub collections() {
		return CollectionsGrpc.newFutureStub(channel)
			.withCallCredentials(callCredentials)
			.withDeadline(timeout != null ? Deadline.after(timeout.toMillis(), TimeUnit.MILLISECONDS) : null);
	}

	/**
	 * Gets the client for snapshots
	 * @return a new instance of {@link SnapshotsFutureStub}
	 */
	public SnapshotsFutureStub snapshots() {
		return SnapshotsGrpc.newFutureStub(channel)
			.withCallCredentials(callCredentials)
			.withDeadline(timeout != null ? Deadline.after(timeout.toMillis(), TimeUnit.MILLISECONDS) : null);
	}

	@Override
	public void close() {
		if (shutdownChannelOnClose && !channel.isShutdown() && !channel.isTerminated()) {
			try {
				channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.warn("exception thrown when shutting down channel", e);
			}
		}
	}

	/**
	 * builder for {@link QdrantGrpcClient}
	 */
	public static class Builder {
		private final ManagedChannel channel;
		private final boolean shutdownChannelOnClose;
		@Nullable
		private CallCredentials callCredentials;
		@Nullable
		private Duration timeout;

		Builder(ManagedChannel channel, boolean shutdownChannelOnClose) {
			this.channel = channel;
			this.shutdownChannelOnClose = shutdownChannelOnClose;
		}

		Builder(String host, int port, boolean useTransportLayerSecurity) {
			this.channel = createChannel(host, port, useTransportLayerSecurity);
			this.shutdownChannelOnClose = true;
		}

		/**
		 * Sets the API key to use for authentication
		 * @param apiKey The API key to use.
		 * @return this
		 */
		public Builder withApiKey(String apiKey) {
			this.callCredentials = new ApiKeyCredentials(apiKey);
			return this;
		}

		/**
		 * Sets a default timeout for all requests.
		 * @param timeout The timeout.
		 * @return this
		 */
		public Builder withTimeout(@Nullable Duration timeout) {
			this.timeout = timeout;
			return this;
		}

		/**
		 * Sets the credential data that will be propagated to the server via request metadata for each RPC.
		 * @param callCredentials The call credentials to use.
		 * @return this
		 */
		public Builder withCallCredentials(@Nullable CallCredentials callCredentials) {
			this.callCredentials = callCredentials;
			return this;
		}

		/**
		 * Builds a new instance of {@link QdrantGrpcClient}
		 * @return a new instance of {@link QdrantGrpcClient}
		 */
		public QdrantGrpcClient build() {
			return new QdrantGrpcClient(channel, shutdownChannelOnClose, callCredentials, timeout);
		}

		private static ManagedChannel createChannel(String host, int port, boolean useTransportLayerSecurity) {
			ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forAddress(host, port);

			if (useTransportLayerSecurity) {
				channelBuilder.useTransportSecurity();
			} else {
				channelBuilder.usePlaintext();
			}

			return channelBuilder.build();
		}
	}
}
