package io.qdrant.client;

import static io.qdrant.client.ShardKeyFactory.shardKey;

import java.util.Arrays;

import io.qdrant.client.grpc.Collections.ShardKey;
import io.qdrant.client.grpc.Points.ShardKeySelector;

/**
 * Convenience methods for constructing {@link ShardKeySelector}
 */
public class ShardKeySelectorFactory {
    private ShardKeySelectorFactory() {
    }

    /**
     * Creates a {@link ShardKeySelector} with the given shard keys.
     *
     * @param shardKeys The shard keys to include in the selector.
     * @return The created {@link ShardKeySelector} object.
     */
    public static ShardKeySelector shardKeySelector(ShardKey... shardKeys) {
        return ShardKeySelector.newBuilder().addAllShardKeys(Arrays.asList(shardKeys)).build();
    }

    /**
     * Creates a {@link ShardKeySelector} with the given shard key keywords.
     *
     * @param keywords The shard key keywords to include in the selector.
     * @return The created {@link ShardKeySelector} object.
     */
    public static ShardKeySelector shardKeySelector(String... keywords) {
        ShardKeySelector.Builder builder = ShardKeySelector.newBuilder();
        for (String keyword : keywords) {
            builder.addShardKeys(shardKey(keyword));
        }
        return builder.build();
    }

    /**
     * Creates a {@link ShardKeySelector} with the given shard key numbers.
     *
     * @param numbers The shard key numbers to include in the selector.
     * @return The created {@link ShardKeySelector} object.
     */
    public static ShardKeySelector shardKeySelector(long... numbers) {
        ShardKeySelector.Builder builder = ShardKeySelector.newBuilder();
        for (long number : numbers) {
            builder.addShardKeys(shardKey(number));
        }
        return builder.build();
    }
}
