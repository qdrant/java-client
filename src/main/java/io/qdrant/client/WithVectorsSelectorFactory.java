package io.qdrant.client;

import java.util.List;

import io.qdrant.client.grpc.Points;
import io.qdrant.client.grpc.Points.WithVectorsSelector;

/**
 * Convenience methods for constructing {@link WithVectorsSelector}
 */
public final class WithVectorsSelectorFactory {
	private WithVectorsSelectorFactory() {
	}

	/**
	 * Whether to include vectors in response.
	 * @param enable if <code>true</code>, to include vectors, if <code>false</code>, none.
	 * @return a new instance of {@link WithVectorsSelector}
	 */
	public static WithVectorsSelector enable(boolean enable) {
		return WithVectorsSelector.newBuilder().setEnable(enable).build();
	}

	/**
	 * List of named vectors to include in response.
	 * @param names The names of vectors.
	 * @return a new instance of {@link WithVectorsSelector}
	 */
	public static WithVectorsSelector include(List<String> names) {
		return WithVectorsSelector.newBuilder()
			.setInclude(Points.VectorsSelector.newBuilder().addAllNames(names))
			.build();
	}
}
