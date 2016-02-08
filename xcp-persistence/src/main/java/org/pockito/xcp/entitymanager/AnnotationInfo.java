package org.pockito.xcp.entitymanager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.pockito.xcp.annotations.Attribute;
import org.pockito.xcp.annotations.XcpTypeCategory;
import org.pockito.xcp.entitymanager.api.MetaData;
import org.pockito.xcp.entitymanager.api.PersistentProperty;
import org.pockito.xcp.exception.XcpPersistenceException;
import org.pockito.xcp.message.Message;

import com.google.common.primitives.Primitives;

public class AnnotationInfo implements MetaData {

	private String dmsType;
	private String dmsRelationName;
	private String label;
	private XcpTypeCategory typeCategory;
	private PersistentProperty idMethod;
	private PersistentProperty vstampMethod;
	private PersistentProperty parentMethod;
	private PersistentProperty childMethod;
	private Map<String, PersistentProperty> persistentProperties = new HashMap<String, PersistentProperty>();

	public AnnotationInfo(String type) {
		this.dmsType = type;
	}

	public AnnotationInfo() {
	}

	/* (non-Javadoc)
	 * @see org.pockito.xcp.entitymanager.MetaData#getDmsType()
	 */
	@Override
	public String getDmsType() {
		return dmsType;
	}

	public void setDmsType(String dmsType) {
		this.dmsType = dmsType;
	}

	/* (non-Javadoc)
	 * @see org.pockito.xcp.entitymanager.MetaData#getIdMethod()
	 */
	@Override
	public PersistentProperty getIdProperty() {
		return idMethod;
	}

	protected void setIdProperty(PersistentProperty idMethod) {
		this.idMethod = idMethod;
	}

	/* (non-Javadoc)
	 * @see org.pockito.xcp.entitymanager.MetaData#getPersistentProperties()
	 */
	@Override
	public Collection<PersistentProperty> getPersistentProperties() {
		return persistentProperties.values();
	}

	/* (non-Javadoc)
	 * @see org.pockito.xcp.entitymanager.MetaData#getPersistentProperty(java.lang.String)
	 */
	@Override
	public PersistentProperty getPersistentProperty(String property) {
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
		} else if (persistentMethod.isParent()) {
			setParentMethod(persistentMethod);
		} else if (persistentMethod.isChild()) {
			setChildMethod(persistentMethod);
        } else if (persistentMethod.isVStamp()) {
        	Class<?> fieldType = persistentMethod.getRawClass();
        	if (Primitives.unwrap(fieldType) == int.class) {
            	setVStampProperty(persistentMethod);
        	}
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
			final Attribute attr = field.getAnnotation(Attribute.class);
			if (attr == null) {
				throw new XcpPersistenceException(Message.E_NO_ATTRIBUTE_ID.get());
			}
			setIdProperty(persistentField);
		} else if (persistentField.isParent()) {
			setParentMethod(persistentField);
		} else if (persistentField.isChild()) {
			setChildMethod(persistentField);
		} else if (persistentField.isVStamp()) {
        	Class<?> fieldType = persistentField.getRawClass();
        	if (Primitives.unwrap(fieldType) == int.class) {
        		setVStampProperty(persistentField);
        	}
        }
		return persistentField;
	}

	/* (non-Javadoc)
	 * @see org.pockito.xcp.entitymanager.MetaData#getTypeCategory()
	 */
	@Override
	public XcpTypeCategory getTypeCategory() {
		return typeCategory;
	}

	public void setTypeCategory(XcpTypeCategory typeCategory) {
		this.typeCategory = typeCategory;
	}

	/* (non-Javadoc)
	 * @see org.pockito.xcp.entitymanager.MetaData#getVStampMethod()
	 */
	@Override
	public PersistentProperty getVStampMethod() {
		return vstampMethod;
	}

	public void setVStampProperty(PersistentProperty vstampMethod) {
		this.vstampMethod = vstampMethod;
	}

	/* (non-Javadoc)
	 * @see org.pockito.xcp.entitymanager.MetaData#getParentMethod()
	 */
	@Override
	public PersistentProperty getParentMethod() {
		return parentMethod;
	}

	public void setParentMethod(PersistentProperty parentMethod) {
		this.parentMethod = parentMethod;
	}

	/* (non-Javadoc)
	 * @see org.pockito.xcp.entitymanager.MetaData#getChildMethod()
	 */
	@Override
	public PersistentProperty getChildMethod() {
		return childMethod;
	}

	public void setChildMethod(PersistentProperty childMethod) {
		this.childMethod = childMethod;
	}

	/* (non-Javadoc)
	 * @see org.pockito.xcp.entitymanager.MetaData#getLabel()
	 */
	@Override
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDmsRelationName() {
		return dmsRelationName;
	}

	public void setDmsRelationName(String dmsRelationName) {
		this.dmsRelationName = dmsRelationName;
	}

}
