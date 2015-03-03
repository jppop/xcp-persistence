package org.pockito.xcp.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


@Target(TYPE) 
@Retention(RUNTIME)
public @interface XcpType {

	String name();
	XcpTypes type() default XcpTypes.BUSINESS_OBJECT;
	String label() default "";
}
