package org.pockito.xcp.entitymanager;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;

import org.pockito.xcp.entitymanager.api.PersistentProperty;

/**
 * PersistentProperty that encapsulates access via direct Field accessor rather than getter/setter methods. Used to
 * support persistence annotations directly on fields.
 *
 * FIXME remove getGetter/getSetter from calling code and don't rely on bean descriptors (should allow the persistence
 * of data with or without a getter/setter)
 *
 * from SimpleJPA https://github.com/appoxy/simplejpa (Kerry Wright)
 * 
 * @author jpfi
 */
public class PersistentField extends PersistentProperty {
    private final Field field;
    private Method getter = null;
    private Method setter = null;

    public PersistentField(Field field) {
        super(field);
        this.field = field;
        try {
            BeanInfo info = Introspector.getBeanInfo(field.getDeclaringClass());
            for (PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
                if (descriptor.getName().equals(field.getName())) {
                    getter = descriptor.getReadMethod();
                    setter = descriptor.getWriteMethod();
                    break;
                }
            }
            if (getter == null || setter == null) throw new IllegalArgumentException("Only fields with valid JavaBean accessors can be annotated");
        } catch (IntrospectionException e) {
            throw new IllegalArgumentException("Could not introspect field: "+field);
        }
    }

    public Method getGetter() {
        return getter;
    }

    public Method getSetter() {
        return setter;
    }

    public String getFieldName() {
        return field.getName();
    }
}
