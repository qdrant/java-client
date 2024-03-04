package io.qdrant.client;

import java.util.UUID;

import io.qdrant.client.grpc.Points.PointId;

/**
 * Convenience methods for constructing {@link PointId}
 */
public final class PointIdFactory {
	private PointIdFactory() {
	}

	/**
	 * Creates a point id from a {@link long}
	 * @param id The id
	 * @return a new instance of {@link PointId}
	 */
	public static PointId id(long id) {
		return PointId.newBuilder().setNum(id).build();
	}

	/**
	 * Creates a point id from a {@link UUID}
	 * @param id The id
	 * @return a new instance of {@link PointId}
	 */
	public static PointId id(UUID id) {
		return PointId.newBuilder().setUuid(id.toString()).build();
	}
}
