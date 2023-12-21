package io.qdrant.client;

import io.qdrant.client.grpc.Collections.ShardKey;

/**
 * Convenience methods for constructing {@link ShardKey}
 */
public final class ShardKeyFactory {
    private ShardKeyFactory() {
    }

    /**
     * Creates a {@link ShardKey} based on a keyword.
     *
     * @param keyword The keyword to create the shard key from
     * @return The {@link ShardKey} object
     */
    public static ShardKey shardKey(String keyword) {
        return ShardKey.newBuilder().setKeyword(keyword).build();
    }

    /**
     * Creates a {@link ShardKey} based on a number.
     *
     * @param number The number to create the shard key from
     * @return The {@link ShardKey} object
     */
    public static ShardKey shardKey(long number) {
        return ShardKey.newBuilder().setNumber(number).build();
    }
}
