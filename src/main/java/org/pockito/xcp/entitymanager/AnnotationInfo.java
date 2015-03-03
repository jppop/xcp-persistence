package org.pockito.xcp.entitymanager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AnnotationInfo {

	private String dmsType;
	private PersistentProperty idMethod;
	private Map<String, PersistentProperty> persistentProperties = new HashMap<String, PersistentProperty>();

	public AnnotationInfo(String type) {
		this.dmsType = type;
	}

	public AnnotationInfo() {
	}

	public String getDmsType() {
		return dmsType;
	}

	public void setDmsType(String dmsType) {
		this.dmsType = dmsType;
	}

	protected PersistentProperty getIdMethod() {
		return idMethod;
	}

	protected void setIdProperty(PersistentProperty idMethod) {
		this.idMethod = idMethod;
	}

	public Collection<PersistentProperty> getPersistentProperties() {
		return persistentProperties.values();
	}

	protected PersistentProperty getPersistentProperty(String property) {
		return persistentProperties.get(property);
	}

    public PersistentProperty addGetter(Method method) {
        PersistentMethod persistentMethod = new PersistentMethod(method);
        // if we already have an accessor in the list, don't overwrite it
        if (persistentProperties.containsKey(persistentMethod.getFieldName())) {
        	return persistentProperties.get(persistentMethod.getFieldName());
        }
        persistentProperties.put(persistentMethod.getFieldName(), persistentMethod);
        if (persistentMethod.isId()) {
        	setIdProperty(persistentMethod);
        }
        return persistentMethod;
    }

	public PersistentProperty addField(Field field) {
		PersistentField persistentField = new PersistentField(field);
		// if we already have an accessor in the list, don't overwrite it
		if (persistentProperties.containsKey(persistentField.getFieldName())) {
			return persistentProperties.get(persistentField.getFieldName());
		}
		persistentProperties.put(persistentField.getFieldName(), persistentField);
		if (persistentField.isId()) {
			setIdProperty(persistentField);
		}
		return persistentField;
	}

}
