package io.qdrant.client;

import java.util.List;

import io.qdrant.client.grpc.Points.PayloadExcludeSelector;
import io.qdrant.client.grpc.Points.PayloadIncludeSelector;
import io.qdrant.client.grpc.Points.WithPayloadSelector;

/**
 * Convenience methods for constructing {@link WithPayloadSelector}
 */
public final class WithPayloadSelectorFactory {
	private WithPayloadSelectorFactory() {
	}

	/**
	 * Whether to include all payload in response.
	 * @param enable if <code>true</code>, to include all payload, if <code>false</code>, none.
	 * @return a new instance of {@link WithPayloadSelector}
	 */
	public static WithPayloadSelector enable(boolean enable) {
		return WithPayloadSelector.newBuilder().setEnable(enable).build();
	}

	/**
	 * Which payload fields to include in response.
	 * @param fields the list of fields to include.
	 * @return a new instance of {@link WithPayloadSelector}
	 */
	public static WithPayloadSelector include(List<String> fields) {
		return WithPayloadSelector.newBuilder()
			.setInclude(PayloadIncludeSelector.newBuilder().addAllFields(fields).build())
			.build();
	}

	/**
	 * Which payload fields to exclude in response.
	 * @param fields the list of fields to exclude.
	 * @return a new instance of {@link WithPayloadSelector}
	 */
	public static WithPayloadSelector exclude(List<String> fields) {
		return WithPayloadSelector.newBuilder()
			.setExclude(PayloadExcludeSelector.newBuilder().addAllFields(fields).build())
			.build();
	}
}
