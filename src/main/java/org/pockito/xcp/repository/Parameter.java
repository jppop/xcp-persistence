package org.pockito.xcp.repository;

public interface Parameter<T> {

	String getName();
	
	Class<T> getParameterType();
}
