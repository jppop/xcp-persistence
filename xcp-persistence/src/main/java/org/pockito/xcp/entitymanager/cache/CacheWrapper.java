package org.pockito.xcp.entitymanager.cache;

public interface CacheWrapper<K, V> {

    int size();

    V get(K key);

    void put(K key, V value);

    V remove(K key);

    void clear();
}
