package org.pockito.xcp.entitymanager.cache;

import org.pockito.xcp.entitymanager.AnnotationInfo;

public class NoopSessionCache implements SessionCacheWrapper {

	@Override
	public int size() {
		return 0;
	}

	@Override
	public Object get(String key) {
		return null;
	}

	@Override
	public void put(Object value, AnnotationInfo ai) {
	}

	@Override
	public void put(Object value) {
	}

	@Override
	public CacheElement remove(String key) {
		return null;
	}

	@Override
	public void clear() {
	}

}
