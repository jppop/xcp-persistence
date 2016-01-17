package org.pockito.xcp.entitymanager.cache;

import java.util.concurrent.ConcurrentHashMap;

public class BuiltInCache<K, V> implements CacheWrapper<K, V> {

	private final ConcurrentHashMap<K, V> cacheMap;

	public BuiltInCache() {
		this.cacheMap = new ConcurrentHashMap<K, V>();
	}
	
	@Override
	public int size() {
		return cacheMap().size();
	}

	@Override
	public V get(K key) {
		return (V) cacheMap().get(key);
	}

	@Override
	public void put(K key, V value) {
		cacheMap().put(key, value);
	}

	@Override
	public V remove(K key) {
		return (V) cacheMap().remove(key);
	}

	@Override
	public void clear() {
		cacheMap().clear();
	}

	public ConcurrentHashMap<K, V> cacheMap() {
		return cacheMap;
	}

}
