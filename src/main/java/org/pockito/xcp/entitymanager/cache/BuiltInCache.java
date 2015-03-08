package org.pockito.xcp.entitymanager.cache;

import java.util.concurrent.ConcurrentHashMap;

public class BuiltInCache<K, V> implements CacheWrapper<K, V> {

	private final ConcurrentHashMap<String, CacheElement> cacheMap;

	public BuiltInCache() {
		this.cacheMap = new ConcurrentHashMap<String, CacheElement>();
	}
	
	@Override
	public int size() {
		return cacheMap().size();
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(K key) {
		return (V) cacheMap().get(key);
	}

	@Override
	public void put(K key, V value) {
		cacheMap().put((String)key, (CacheElement)value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public V remove(K key) {
		return (V) cacheMap().remove((String)key);
	}

	@Override
	public void clear() {
		cacheMap().clear();
	}

	public ConcurrentHashMap<String, CacheElement> cacheMap() {
		return cacheMap;
	}

}
