package com.raven.common.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    public LRUCache(int cacheSize) {
        // 使用构造方法 public LinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder)
        // initialCapacity、loadFactor都不重要
        // accessOrder要设置为true，按访问排序
        super((int) Math.ceil(cacheSize / 0.75) + 1, 0.75f, true);
        capacity = cacheSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        // 超过阈值时返回true，进行LRU淘汰
        return size() > capacity;
    }

}

