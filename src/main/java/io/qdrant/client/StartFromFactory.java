package io.qdrant.client;

import java.time.Instant;

import com.google.protobuf.Timestamp;

import io.qdrant.client.grpc.Points.StartFrom;

/**
 * Convenience methods for constructing {@link StartFrom}
 */
public final class StartFromFactory {
	private StartFromFactory() {
	}

	/**
	 * Creates a {@link StartFrom} value from a {@link float}
	 * 
	 * @param value The value
	 * @return a new instance of {@link StartFrom}
	 */
	public static StartFrom startFrom(float value) {
		return StartFrom.newBuilder().setFloat(value).build();
	}

	/**
	 * Creates a {@link StartFrom} value from a {@link int}
	 * 
	 * @param value The value
	 * @return a new instance of {@link StartFrom}
	 */
	public static StartFrom startFrom(int value) {
		return StartFrom.newBuilder().setInteger(value).build();
	}

	/**
	 * Creates a {@link StartFrom} value from a {@link String} timestamp
	 * 
	 * @param value The value
	 * @return a new instance of {@link StartFrom}
	 */
	public static StartFrom startFrom(String value) {
		return StartFrom.newBuilder().setDatetime(value).build();
	}

	/**
	 * Creates a {@link StartFrom} value from a {@link Timestamp}
	 * 
	 * @param value The value
	 * @return a new instance of {@link StartFrom}
	 */
	public static StartFrom startFrom(Timestamp value) {
		return StartFrom.newBuilder().setTimestamp(value).build();
	}

	/**
	 * Creates a {@link StartFrom} value from a {@link Timestamp}
	 * 
	 * @param value The value
	 * @return a new instance of {@link StartFrom}
	 */
	public static StartFrom startFrom(Instant value) {
		return StartFrom.newBuilder().setTimestamp(Timestamp.newBuilder().setSeconds(value.getEpochSecond())).build();
	}
}
