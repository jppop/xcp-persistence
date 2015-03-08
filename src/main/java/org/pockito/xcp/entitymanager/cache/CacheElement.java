package org.pockito.xcp.entitymanager.cache;

public class CacheElement {
	public final Object value;
	public final int vstamp;
	public CacheElement(Object value, int vstamp) {
		this.value = value;
		this.vstamp = vstamp;
	}
}