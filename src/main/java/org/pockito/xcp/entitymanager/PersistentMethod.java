package org.pockito.xcp.entitymanager;

import java.lang.reflect.Method;

import com.google.common.base.CaseFormat;

/**
 * PersistentProperty that supports access to data through getter/setter pair.
 * Used to support persistence annotations on methods
 * 
 * from SimpleJPA https://github.com/appoxy/simplejpa (Kerry Wright)
 * 
 * @author jpfi
 */
public class PersistentMethod extends PersistentProperty {
	private final Method getter;
	private final Method setter;

	public PersistentMethod(Method method) {
		super(method);
		this.getter = method;
		this.getter.setAccessible(true);
		String setterName = getter.getName().replaceFirst("get", "set");
		try {
			this.setter = method.getDeclaringClass().getDeclaredMethod(setterName, getter.getReturnType());
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("No setter found for method provided: " + getter.getName() + " in class: "
					+ method.getDeclaringClass().getName());
		}
		this.setter.setAccessible(true);
	}

	public Method getGetter() {
		return getter;
	}

	public Method getSetter() {
		return setter;
	}

	public String getFieldName() {
		return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, getGetter().getName().replaceFirst("get", ""));
	}

}
