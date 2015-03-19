package org.pockito.xcp.entitymanager.query;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pockito.xcp.entitymanager.XcpEntityManager;
import org.pockito.xcp.entitymanager.api.DmsBeanQuery;
import org.pockito.xcp.entitymanager.api.MetaData;
import org.pockito.xcp.entitymanager.api.PersistentProperty;
import org.pockito.xcp.exception.XcpPersistenceException;

public class XcpBeanQuery<T> extends AbstractTypedQuery<T> implements DmsBeanQuery<T> {

	private final MetaData meta;

	private final Map<String, Expression<?>> expressions = new LinkedHashMap<String, Expression<?>>();

	public XcpBeanQuery(XcpEntityManager em, Class<T> entityClass) {
		super(em, entityClass);
		meta = em.getMetaData(entityClass);
	}

	public <B> DmsBeanQuery<T> setParameter(String fieldName, RightExpression<B> rightOp) {
		final PersistentProperty prop = meta.getPersistentProperty(fieldName);
		if (prop == null) {
			throw new XcpPersistenceException("unknown field");
		}
		Expression<B> expr = new Expression<B>(prop, rightOp);
		remember(expr);
		return this;
	}

	@Override
	public List<T> getResultList() {
		@SuppressWarnings("unchecked")
		final List<T> resultList = (List<T>) executeNativeQuery(getEntityClass(), asDql());
		return resultList;
	}

	@Override
	public String asDql() {

		StringBuffer buffer = new StringBuffer();

		MetaData meta = em.getMetaData(entityClass);

		buffer.append("select r_object_id from ").append(meta.getDmsType());
		if (this.expressions.size() > 0) {
			buffer.append(" where");
		}

		String andOp = "";

		for (Entry<String, Expression<?>> exprEntry : this.expressions.entrySet()) {

			final Expression<?> expr = exprEntry.getValue();

			buffer.append(andOp);
			if (expr.rightOpt.isMultipleValue()) {

			} else {
				valueAsDql(buffer, expr);
			}
			andOp = " and";
		}
		return buffer.toString();
	}

	private void valueAsDql(StringBuffer buffer, Expression<?> expr) {
		String attributeValue = PersistentProperty.asDqlValue(expr.rightOpt.value());
		buffer.append(" ").append(expr.prop.getAttributeName()).append(" ")
				.append(expr.rightOpt.op().dqlOperator()).append(" ").append(attributeValue);
	}

	private <B> void remember(Expression<B> expr) {
		expressions.put(expr.prop.getFieldName(), expr);
	}
}
