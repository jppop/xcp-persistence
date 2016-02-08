package org.pockito.xcp.entitymanager.api;

import java.util.Collection;

import org.pockito.xcp.annotations.XcpTypeCategory;

public interface MetaData {

	public abstract String getDmsType();

	public abstract PersistentProperty getIdProperty();

	public abstract Collection<PersistentProperty> getPersistentProperties();

	public abstract PersistentProperty getPersistentProperty(String property);

	public abstract XcpTypeCategory getTypeCategory();

	public abstract PersistentProperty getVStampMethod();

	public abstract PersistentProperty getParentMethod();

	public abstract PersistentProperty getChildMethod();

	public abstract String getLabel();

}