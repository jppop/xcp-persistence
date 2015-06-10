package org.pockito.xcp.entitymanager.api;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.pockito.xcp.annotations.Attribute;
import org.pockito.xcp.annotations.Child;
import org.pockito.xcp.annotations.GeneratedValue;
import org.pockito.xcp.annotations.Id;
import org.pockito.xcp.annotations.Parent;
import org.pockito.xcp.annotations.ParentFolder;
import org.pockito.xcp.annotations.VStamp;
import org.pockito.xcp.message.Message;

import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.DfValue;
import com.documentum.fc.common.IDfValue;
import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.google.common.escape.CharEscaperBuilder;
import com.google.common.escape.Escaper;

/**
 * from SimpleJPA https://github.com/appoxy/simplejpa (Kerry Wright)
 * 
 * @author jpfi
 * 
 */
public abstract class PersistentProperty {
	
	public static final String DMS_ATTR_OBJECT_ID = "r_object_id";
	public static final String DMS_ATTR_PARENT_ID = "parent_id";
	public static final String DMS_ATTR_CHILD_ID = "child_id";
	public static final String DMS_ATTR_FOLDER_ID = "i_folder_id";
	
	protected final AnnotatedElement element;

	public AnnotatedElement getElement() {
		return element;
	}

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
				throw new IllegalAccessException(Message.E_COLLECTION_FIELD.get(getFieldName()));
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
	
	// TODO: what about Enum ?
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
			Class<?> rawClass = getPropertyClass();
			if (Collection.class.isAssignableFrom(rawClass)) {
				throw new IllegalAccessException(Message.E_COLLECTION_FIELD.get(getFieldName()));
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

	private static final CharEscaperBuilder dqlEscaperBuilder = new CharEscaperBuilder().addEscape('\'', "''");
	private static final Escaper dqlEscaper = dqlEscaperBuilder.toEscaper();
	private static final SimpleDateFormat dqlDateFmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	public static String asDqlValue(Object value) {
		String dqlValue = null;
		try {
			Class<?> rawClass = value.getClass();
			if (Collection.class.isAssignableFrom(rawClass)) {
				throw new IllegalAccessException(Message.E_COLLECTION_FIELD.get(rawClass.getName()));
			}
			if (value != null) {
				if (rawClass.isAssignableFrom(boolean.class)) {
					dqlValue = Boolean.class.cast(value).booleanValue() ? "T" : "F";
				} else if (rawClass.isAssignableFrom(int.class)) {
					dqlValue = Integer.class.cast(value).toString();
				} else if (rawClass.isAssignableFrom(double.class)) {
					dqlValue = Double.class.cast(value).toString();
				} else if (rawClass.isAssignableFrom(Date.class)) {
					String stringDate = dqlDateFmt.format(Date.class.cast(value));
					dqlValue = "DATE('" + stringDate + "', 'yyyy/mm/dd hh:mi:ss')";
				} else {
					dqlValue = "'" + dqlEscaper.escape(String.class.cast(value).toString()) + "'";
				}
			}
//		} catch (ClassCastException e) {
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
		return dqlValue;
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

	public boolean isAssignableFrom(Class<?> c) {
		Class<?> rawClass = getRawClass();
		return rawClass.isAssignableFrom(c);
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
	
	public boolean isParent() {
		return element.isAnnotationPresent(Parent.class);
	}
	
	public boolean isChild() {
		return element.isAnnotationPresent(Child.class);
	}
	
	public boolean isParentFolder() {
		return element.isAnnotationPresent(ParentFolder.class);
	}
	
	public boolean isAttribute() {
		return element.isAnnotationPresent(Attribute.class);
	}
	
	public boolean isReadonly() {
		Attribute annotation = element.getAnnotation(Attribute.class);
		if (annotation == null) {
			return false;
		} else {
			return annotation.readonly();
		}
	}

	public boolean isVStamp() {
		return element.isAnnotationPresent(VStamp.class);
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
			if (!Strings.isNullOrEmpty(attribute.name())) {
				 attributeName = attribute.name();
			}
		} else if (element.isAnnotationPresent(Child.class)){
			attributeName = DMS_ATTR_CHILD_ID;
		} else if (element.isAnnotationPresent(Parent.class)){
			attributeName = DMS_ATTR_PARENT_ID;
		} else if (element.isAnnotationPresent(ParentFolder.class)){
			attributeName = DMS_ATTR_FOLDER_ID;
		} else {
			attributeName = getSystemAttributeName();
		}
		if (attributeName == null) {
			attributeName = getFieldName().toLowerCase();
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
	
	public static String getFieldName(String methodName) {
		String prefix = "get";
		if (methodName.startsWith("set")) {
			prefix = "set";
		}
		return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, methodName.replaceFirst(prefix, ""));
	}

	@Override
	public String toString() {
		return getFieldName();
	}

}
