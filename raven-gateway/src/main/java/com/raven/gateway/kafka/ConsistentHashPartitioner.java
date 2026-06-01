package com.raven.gateway.kafka;

import com.google.common.base.Strings;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

/**
 * Routes every record that shares a key to the same partition by placing the
 * partitions on a consistent-hash ring. In Raven the producer key is the
 * conversation id, so all messages of one conversation land on a single
 * partition. Kafka preserves ordering within a partition, which keeps the
 * consumption order identical to the production order for a conversation.
 *
 * <p>A consistent-hash ring (instead of the default {@code hash(key) % count})
 * is used so that adding or removing partitions only re-maps a small fraction
 * of conversations rather than reshuffling all of them.
 */
public class ConsistentHashPartitioner implements Partitioner {

    private static final HashFunction HASH_FUNCTION = Hashing.murmur3_32();

    private static final int VIRTUAL_NODE_SIZE = 160;

    private static final String VIRTUAL_NODE_SUFFIX = "#";

    /** Cache one ring per observed partition count to avoid rebuilding it per send. */
    private final Map<Integer, TreeMap<Integer, Integer>> ringCache = new ConcurrentHashMap<>();

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value,
        byte[] valueBytes, Cluster cluster) {
        int numPartitions = cluster.partitionsForTopic(topic).size();
        if (numPartitions <= 1) {
            return 0;
        }
        String hashKey = key == null ? null : key.toString();
        if (Strings.isNullOrEmpty(hashKey)) {
            // No conversation key: hash the payload so keyless records still spread out.
            hashKey = value == null ? "" : value.toString();
        }
        return locate(hashKey, numPartitions);
    }

    /**
     * Resolve the partition for a key against a ring built for {@code numPartitions}.
     * Package-private so the routing logic can be unit tested without a broker.
     */
    int locate(String hashKey, int numPartitions) {
        TreeMap<Integer, Integer> ring = ringCache.computeIfAbsent(numPartitions, this::buildRing);
        int hash = HASH_FUNCTION.hashString(hashKey, StandardCharsets.UTF_8).asInt();
        Map.Entry<Integer, Integer> entry = ring.ceilingEntry(hash);
        if (entry == null) {
            // Past the end of the ring, wrap around to the first node.
            entry = ring.firstEntry();
        }
        return entry.getValue();
    }

    private TreeMap<Integer, Integer> buildRing(int numPartitions) {
        TreeMap<Integer, Integer> ring = new TreeMap<>();
        for (int partition = 0; partition < numPartitions; partition++) {
            for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
                int hash = HASH_FUNCTION
                    .hashString(partition + VIRTUAL_NODE_SUFFIX + i, StandardCharsets.UTF_8).asInt();
                ring.put(hash, partition);
            }
        }
        return ring;
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }

    @Override
    public void close() {
    }
}
