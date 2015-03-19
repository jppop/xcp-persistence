package org.pockito.xcp.entitymanager.query;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.pockito.xcp.entitymanager.XcpEntityManager;
import org.pockito.xcp.entitymanager.api.DmsBeanQuery;
import org.pockito.xcp.entitymanager.api.MetaData;
import org.pockito.xcp.entitymanager.api.PersistentProperty;

public class XcpBeanQuery<T> extends AbstractTypedQuery<T> implements DmsBeanQuery<T> {

	private final T beanEnhancer;

	private final Map<String, ArrayList<Object>> setFields = new HashMap<String, ArrayList<Object>>();

	public XcpBeanQuery(XcpEntityManager em, Class<T> entityClass) {
		super(em, entityClass);
		this.beanEnhancer = enhancerOf(entityClass);
	}

	@Override
	public T bean() {
		return beanEnhancer;
	}

	@Override
	public List<T> getResultList() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	private T enhancerOf(Class<T> beanType) {

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(beanType);
		enhancer.setCallback(new MethodInterceptor() {

			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				if (method.getName().startsWith("set") && (args.length > 0)) {
					remember(method.getName(), args[0]);
				}
				return proxy.invokeSuper(obj, args);
			}

			private void remember(String setterName, Object value) {
				ArrayList<Object> setField = setFields.get(setterName);
				if (setField == null) {
					ArrayList<Object> values = new ArrayList<Object>();
					values.add(value);
					setFields.put(setterName, values);
				} else {
					setField.add(value);
				}
			}
		});
		return (T) enhancer.create();
	}

	public String asDql() {
		
		StringBuffer buffer = new StringBuffer();
		
		MetaData meta = em.getMetaData(entityClass);
		
		buffer.append("select r_object_id from ").append(meta.getDmsType());
		if (this.setFields.size() > 0) {
			buffer.append(" where");
		}
		
		String andOp = "";
		
		for (Entry<String, ArrayList<Object>> setField : this.setFields.entrySet()) {
			
			final String fieldName = PersistentProperty.getFieldName(setField.getKey());
			final PersistentProperty prop = meta.getPersistentProperty(fieldName);
			final ArrayList<Object> values = setField.getValue();
			buffer.append(andOp);
			
			if (values.size() == 1) {
				valueAsDql(buffer, meta, prop, values.get(0));
			} else {
				buffer.append(prop.getAttributeName()).append(" IN (");
				for (Object v : values) {
					valueAsDql(buffer, meta, prop, v);
				}
				buffer.append(")");
			}
			andOp = " and ";
		}
		return buffer.toString();
	}

	private void valueAsDql(StringBuffer buffer, MetaData meta, PersistentProperty prop, Object value) {
		if (prop != null) {
			String attributeValue = PersistentProperty.asDqlValue(value);
			buffer.append(" ").append(prop.getAttributeName()).append(" = ").append(attributeValue);
		}
	}
}
