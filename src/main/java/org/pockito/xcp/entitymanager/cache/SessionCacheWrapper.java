package org.pockito.xcp.entitymanager.cache;

import org.pockito.xcp.entitymanager.AnnotationInfo;

public interface SessionCacheWrapper {

	public abstract int size();

	public abstract Object get(String key);

	public abstract void put(Object value, AnnotationInfo ai);

	public abstract void put(Object value);

	public abstract CacheElement remove(String key);

	public abstract void clear();

}