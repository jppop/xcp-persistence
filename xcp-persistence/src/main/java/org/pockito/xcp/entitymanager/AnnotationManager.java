package org.pockito.xcp.entitymanager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.pockito.xcp.annotations.Transient;
import org.pockito.xcp.annotations.XcpEntity;
import org.pockito.xcp.annotations.XcpType;
import org.pockito.xcp.annotations.XcpTypeCategory;
import org.pockito.xcp.exception.XcpPersistenceException;
import org.pockito.xcp.message.Message;

import com.google.common.base.Strings;

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
				throw new XcpPersistenceException(Message.E_NO_ENTITY_ANNOTATION.get(c.getName()));
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
            			ai.setTypeCategory(xcpType.type());
	            		String namespace = entity.namespace();
	            		String shortName = xcpType.name();
	            		if (Strings.isNullOrEmpty(namespace) || Strings.isNullOrEmpty(shortName)) {
	            			throw new XcpPersistenceException(Message.E_NO_NAMES.get(c.getName()));
	            		}
            			dmsTypeName = namespace + "_" + shortName;
	            		if (xcpType.type() == XcpTypeCategory.RELATION) {
	            			// set the relation name (equal to the Documentum type)
            				ai.setDmsRelationName(dmsTypeName);
	            			// xCP always creates a derived type for a relation but not Documentum
	            			if (namespace.equals(XcpEntityManager.DOCUMENTUM_NAMESPACE)) {
	            				dmsTypeName = XcpEntityManager.DMS_RELATION_TYPE;
	            			}
	            		}
	                    ai.setLabel(xcpType.label());
            		}
            	}
                addProperties(ai, superClass);
                addMethods(ai, superClass);
                rootClass = superClass;
            }
            superClass = superClass.getSuperclass();
        }
        if (Strings.isNullOrEmpty(dmsTypeName)) {
        	throw new XcpPersistenceException(Message.E_NO_TYPE_ANNOTATION.get(c.getName()));
        }
        if (ai.getIdProperty() == null) {
        	throw new XcpPersistenceException(Message.E_NO_ID_ANNOTATION.get(c.getName()));
        }
        ai.setDmsType(dmsTypeName);
        getAnnotationMap().put(c.getName(), ai);
		return ai;
	}
	
//    private boolean isNativeNameSpace(String namespace) {
//		return namespace.equals(DM_NAMESPACE) || namespace.equals(DMC_NAMESPACE);
//	}
//
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

//	public static String getTypeOfNativeEntity(String entityName) {
//		if ("native.PersistedObject".equals(entityName)) {
//			return "dm_sysobject";
//		} else if ("native.Document".equals(entityName)) {
//			return "dm_sysobject";
//		} else if ("native.Folder".equals(entityName)) {
//			return "dm_folder";
//		}
//		return null;
//		
//	}
//
	protected Map<String, AnnotationInfo> getAnnotationMap() {
		return annotationMap;
	}
}
