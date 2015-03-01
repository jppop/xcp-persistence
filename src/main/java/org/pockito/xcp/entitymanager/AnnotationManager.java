package org.pockito.xcp.entitymanager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.pockito.xcp.annotations.Transient;
import org.pockito.xcp.annotations.XcpEntity;
import org.pockito.xcp.annotations.XcpType;
import org.pockito.xcp.exception.XcpPersistenceException;

public class AnnotationManager {

	private Map<String, AnnotationInfo> annotationMap = new HashMap<String, AnnotationInfo>();

	public AnnotationManager() {
	}
	
	public AnnotationInfo getAnnotationInfo(Class<?> entityClass) {
		AnnotationInfo ai =  getAnnotationInfo(entityClass.getName());
		if (ai == null) {
			ai = addAnnotationInfo(entityClass);
		}
		return ai;
	}

	public AnnotationInfo getAnnotationInfo(final Object entity) {
		return getAnnotationInfo(entity.getClass());
	}

	private AnnotationInfo getAnnotationInfo(final String classname) {
		AnnotationInfo ai =  getAnnotationMap().get(classname);
		return ai;
	}

	public AnnotationInfo addAnnotationInfo(final Class<?> c) {

		{
			XcpEntity entity = c.getAnnotation(XcpEntity.class);
			if (entity == null) {
				// TODO implements a better exception management
				throw new XcpPersistenceException("Class not marked as an @Entity: " + c.getName());
			}
		}		
        AnnotationInfo ai = new AnnotationInfo();
        
        String dmsTypeName = null;

        Class<?> superClass = c;
        @SuppressWarnings("unused")
		Class<?> rootClass = null;
        while (superClass != null) {
            XcpEntity entity = (XcpEntity) superClass.getAnnotation(XcpEntity.class);
            if (entity != null) {
            	if (dmsTypeName == null) {
            		XcpType xcpType = superClass.getAnnotation(XcpType.class);
            		if (xcpType != null) {
		                // get the underlying DMS type
	            		String namespace = entity.namespace();
	            		String shortName = xcpType.name();
	            		dmsTypeName = namespace + "_" + shortName;
            		}
            	}
                addProperties(ai, superClass);
                addMethods(ai, superClass);
                rootClass = superClass;
            }
            superClass = superClass.getSuperclass();
        }
        if (StringUtils.isBlank(dmsTypeName)) {
        	throw new XcpPersistenceException("Underlying type name not found. You must mark the entity with XcpType. Class: " + c.getName());
        }
        ai.setDmsType(dmsTypeName);
        getAnnotationMap().put(c.getName(), ai);
		return ai;
	}
	
    private void addMethods(AnnotationInfo ai, Class<?> c) {
        Method[] methods = c.getDeclaredMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (!methodName.startsWith("get")) continue;
            Transient transientM = method.getAnnotation(Transient.class);
            if (transientM != null) continue; // we don't save this one
            ai.addGetter(method);
        }
    }


	private void addProperties(AnnotationInfo ai, Class<?> c) {
        for (Field field : c.getDeclaredFields()) {
            parseProperty(ai, c, field);
        }
	}

	private void parseProperty(AnnotationInfo ai, Class<?> c, Field field) {
		if (!field.isAnnotationPresent(Transient.class)) {
			ai.addField(field);
		}
			
	}

	// TODO: this should be externalized
	public static String getTypeOfNativeEntity(String entityName) {
		if ("native.PersistedObject".equals(entityName)) {
			return "dm_sysobject";
		} else if ("native.Document".equals(entityName)) {
			return "dm_sysobject";
		} else if ("native.Folder".equals(entityName)) {
			return "dm_folder";
		}
		return null;
		
	}

	protected Map<String, AnnotationInfo> getAnnotationMap() {
		return annotationMap;
	}
}
