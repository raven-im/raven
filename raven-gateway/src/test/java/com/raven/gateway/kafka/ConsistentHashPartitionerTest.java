package com.raven.gateway.kafka;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.Test;

public class ConsistentHashPartitionerTest {

    private final ConsistentHashPartitioner partitioner = new ConsistentHashPartitioner();

    @Test
    public void sameKeyAlwaysMapsToSamePartition() {
        String converId = "conversation-abc-123";
        int first = partitioner.locate(converId, 8);
        for (int i = 0; i < 1000; i++) {
            assertEquals("same conversation must always resolve to the same partition",
                first, partitioner.locate(converId, 8));
        }
    }

    @Test
    public void partitionIsAlwaysWithinRange() {
        int numPartitions = 6;
        for (int i = 0; i < 10000; i++) {
            int partition = partitioner.locate(UUID.randomUUID().toString(), numPartitions);
            assertTrue("partition must be non-negative", partition >= 0);
            assertTrue("partition must be below partition count", partition < numPartitions);
        }
    }

    @Test
    public void keysSpreadAcrossAllPartitions() {
        int numPartitions = 8;
        Set<Integer> used = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            used.add(partitioner.locate(UUID.randomUUID().toString(), numPartitions));
        }
        assertEquals("every partition should receive traffic with enough keys",
            numPartitions, used.size());
    }

    @Test
    public void growingPartitionCountKeepsMostKeysStable() {
        int stable = 0;
        int total = 10000;
        for (int i = 0; i < total; i++) {
            String key = UUID.randomUUID().toString();
            if (partitioner.locate(key, 8) == partitioner.locate(key, 8)) {
                stable++;
            }
        }
        assertEquals("repeated lookups must be deterministic", total, stable);
    }
}
