package org.pockito.xcp.entitymanager;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.pockito.xcp.annotations.GeneratedValue;
import org.pockito.xcp.annotations.Id;
import org.pockito.xcp.annotations.Attribute;

import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.DfValue;
import com.documentum.fc.common.IDfValue;

/**
 * from SimpleJPA https://github.com/appoxy/simplejpa (Kerry Wright)
 * 
 * @author jpfi
 * 
 */
public abstract class PersistentProperty {
	protected final AnnotatedElement element;

	protected PersistentProperty(final AnnotatedElement annotatedElement) {
		this.element = annotatedElement;
	}

	public Object getProperty(final Object target) {
		try {
			return getGetter().invoke(target);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}

	public void setProperty(final Object target, final Object value) {
		try {
			getSetter().invoke(target, value);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}

	public void setProperty(final Object target, final IDfValue value) {
		try {
			Class<?> rawClass = getRawClass();
			if (Collection.class.isAssignableFrom(rawClass)) {
				throw new IllegalAccessException("property is a collection");
			}
			if (rawClass.isAssignableFrom(boolean.class)) {
				getSetter().invoke(target, value == null ? false : value.asBoolean());
			} else if (rawClass.isAssignableFrom(int.class)) {
				getSetter().invoke(target, value == null ? -1 : value.asInteger());
			} else if (rawClass.isAssignableFrom(double.class)) {
				getSetter().invoke(target, value == null ? 0L : value.asDouble());
			} else if (rawClass.isAssignableFrom(Date.class)) {
				getSetter().invoke(target, value == null ? null : value.asTime().getDate());
			} else {
				getSetter().invoke(target, value == null ? null : value.asString());
			}
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public Object dfValueToObject(final IDfValue value) {
		Class<?> rawClass = getPropertyClass();
		Object obj = null;
		if (value != null) {
			if (rawClass.isAssignableFrom(boolean.class)) {
				obj = value.asBoolean();
			} else if (rawClass.isAssignableFrom(int.class)) {
				obj = value.asInteger();
			} else if (rawClass.isAssignableFrom(double.class)) {
				obj = value.asDouble();
			} else if (rawClass.isAssignableFrom(Date.class)) {
				obj = value.asTime().getDate();
			} else {
				obj = value.asString();
			}
		}
		return obj;
	}
	
	public IDfValue objToDfValue(final Object anObject) {
		IDfValue dfValue = null;
		try {
			Class<?> rawClass = getRawClass();
			if (Collection.class.isAssignableFrom(rawClass)) {
				throw new IllegalAccessException("property is a collection");
			}
			if (anObject != null) {
				if (rawClass.isAssignableFrom(boolean.class)) {
					dfValue = new DfValue(anObject, IDfValue.DF_BOOLEAN);
				} else if (rawClass.isAssignableFrom(int.class)) {
					dfValue = new DfValue(anObject, IDfValue.DF_INTEGER);
				} else if (rawClass.isAssignableFrom(double.class)) {
					dfValue = new DfValue(anObject, IDfValue.DF_DOUBLE);
				} else if (rawClass.isAssignableFrom(Date.class)) {
					dfValue = new DfValue(new DfTime((Date) anObject));
				} else {
					dfValue = new DfValue(anObject, IDfValue.DF_STRING);
				}
			}
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
		return dfValue;
	}

	public Class<?> getPropertyClass() {
		Class<?> clazz = getGetter().getReturnType();
		if (Collection.class.isAssignableFrom(clazz)) {
			return (Class<?>) ((ParameterizedType) getGetter()
					.getGenericReturnType()).getActualTypeArguments()[0];
		}
		return clazz;
	}

	public Class<?> getRawClass() {
		return getGetter().getReturnType();
	}

	public abstract Method getGetter();

	public abstract Method getSetter();

	public abstract String getFieldName();

	public boolean isRepeating() {
		Class<?> rawClass = getRawClass();
		return Collection.class.isAssignableFrom(rawClass);
//		return element.isAnnotationPresent(Repeating.class);
	}

	public boolean isId() {
		return element.isAnnotationPresent(Id.class);
	}

	public boolean isGeneratedValue() {
		return element.isAnnotationPresent(GeneratedValue.class);
	}
	
	public boolean isReadonly() {
		Attribute annotation = element.getAnnotation(Attribute.class);
		if (annotation == null) {
			return false;
		} else {
			return annotation.readonly();
		}
	}

//	public boolean isParentFolder() {
//		return element.isAnnotationPresent(ParentFolder.class);
//	}
//
//	public boolean isSystem() {
//		Attribute annotation = element.getAnnotation(Attribute.class);
//		if (annotation == null) {
//			return false;
//		} else {
//			return annotation.system();
//		}
//	}
//
//	public EnumType getEnumType() {
//		if (element.isAnnotationPresent(Enumerated.class)) {
//			if (element.getAnnotation(Enumerated.class).value() == EnumType.STRING)
//				return EnumType.STRING;
//			else
//				return EnumType.ORDINAL;
//		}
//		return null;
//	}
//
	public String getAttributeName() {
		String attributeName = null;
		if (element.isAnnotationPresent(Attribute.class)) {
			Attribute attribute = element.getAnnotation(Attribute.class);
			if (!StringUtils.isBlank(attribute.name())) {
				 attributeName = attribute.name();
			}
		} else {
			attributeName = getSystemAttributeName();
		}
		if (attributeName == null) {
			attributeName = StringUtils.uncapitalize(getFieldName());
		}
		return attributeName;
	}

	protected String getSystemAttributeName() {
		String attributeName = null;
//		if (element.isAnnotationPresent(ParentFolder.class)) {
//			attributeName = "i_folder_id";
//		}
		return attributeName;
	}

	@Override
	public String toString() {
		return getFieldName();
	}

}
