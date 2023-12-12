package io.qdrant.client;

import com.google.common.collect.Maps;
import com.google.common.primitives.Floats;

import java.util.List;
import java.util.Map;

import static io.qdrant.client.grpc.Points.NamedVectors;
import static io.qdrant.client.grpc.Points.Vector;
import static io.qdrant.client.grpc.Points.Vectors;

/**
 * Convenience methods for constructing {@link Vectors}
 */
public final class VectorsFactory {
	private VectorsFactory() {
	}

	/**
	 * Creates named vectors
	 * @param values A map of vector names to values
	 * @return a new instance of {@link Vectors}
	 */
	public static Vectors namedVectors(Map<String, List<Float>> values) {
		return Vectors.newBuilder()
			.setVectors(NamedVectors.newBuilder()
				.putAllVectors(Maps.transformValues(values, v -> Vector.newBuilder()
					.addAllData(v)
					.build()))
			)
			.build();
	}

	/**
	 * Creates a vector
	 * @param values A list of values
	 * @return a new instance of {@link Vectors}
	 */
	public static Vectors vector(List<Float> values) {
		return Vectors.newBuilder()
			.setVector(Vector.newBuilder()
				.addAllData(values)
				.build())
			.build();
	}

	/**
	 * Creates a vector
	 * @param values A list of values
	 * @return a new instance of {@link Vectors}
	 */
	public static Vectors vector(float... values) {
		return Vectors.newBuilder()
			.setVector(Vector.newBuilder()
				.addAllData(Floats.asList(values))
				.build())
			.build();
	}
}
