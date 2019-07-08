package com.raven.common.loadbalance;


import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ConsistentHashLoadBalancer implements LoadBalancer {

    // 使用murmur哈希算法
    private final static HashFunction hashFunction = Hashing.murmur3_32();

    private final static Charset charset = Charset.forName("utf-8");

    private final static int VIRTUAL_NODE_SIZE = 10;

    private final static String VIRTUAL_NODE_SUFFIX = "#@";

    @Override
    public GatewayServerInfo select(List<GatewayServerInfo> servers, String hashKey) {
        HashCode hashCode = hashFunction.hashString(hashKey, charset);
        TreeMap<Integer, GatewayServerInfo> ring = buildConsistentHashRing(servers);
        return locate(ring, hashCode.asInt());
    }

    private GatewayServerInfo locate(TreeMap<Integer, GatewayServerInfo> ring, int invocationHashCode) {
        // 向右找到第一个 key
        Map.Entry<Integer, GatewayServerInfo> locateEntry = ring.ceilingEntry(invocationHashCode);
        if (locateEntry == null) {
            // 想象成一个环，超过尾部则取第一个 key
            locateEntry = ring.firstEntry();
        }
        return locateEntry.getValue();
    }

    private TreeMap<Integer, GatewayServerInfo> buildConsistentHashRing(List<GatewayServerInfo> servers) {
        TreeMap<Integer, GatewayServerInfo> virtualNodeRing = new TreeMap<>();
        for (GatewayServerInfo server : servers) {
            for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
                // 映射虚拟节点与物理节点
                HashCode hashCode = hashFunction
                    .hashString(server.hashCode() + VIRTUAL_NODE_SUFFIX + i, charset);
                virtualNodeRing.put(hashCode.asInt(), server);
            }
        }
        return virtualNodeRing;
    }

}
