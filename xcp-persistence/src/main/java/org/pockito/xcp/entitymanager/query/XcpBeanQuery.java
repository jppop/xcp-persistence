package org.pockito.xcp.entitymanager.query;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pockito.xcp.entitymanager.NotYetImplemented;
import org.pockito.xcp.entitymanager.XcpEntityManager;
import org.pockito.xcp.entitymanager.api.DmsBeanQuery;
import org.pockito.xcp.entitymanager.api.MetaData;
import org.pockito.xcp.entitymanager.api.PersistentProperty;
import org.pockito.xcp.exception.XcpPersistenceException;

public class XcpBeanQuery<T> extends AbstractTypedQuery<T> implements DmsBeanQuery<T> {

	private final MetaData meta;

	private final Map<String, Expression<?>> expressions = new LinkedHashMap<String, Expression<?>>();

	private QueryType queryType = QueryType.select;

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
		if (getQueryType() != QueryType.select) {
			throw new XcpPersistenceException("Not a select query");
		}
		@SuppressWarnings("unchecked")
		final List<T> resultList = (List<T>) executeNativeQuery(getEntityClass(), asDql());
		return resultList;
	}

	@Override
	public void setQueryType(QueryType queryType) {
		this.queryType = queryType;
	}

	@Override
	public String asDql() {

		StringBuffer buffer = new StringBuffer();

		MetaData meta = em.getMetaData(entityClass);

		if (getQueryType() == QueryType.select) {
			buffer.append("select r_object_id from ").append(meta.getDmsType());
		} else if (getQueryType() == QueryType.delete) {
			buffer.append("delete ").append(meta.getDmsType()).append(" objects");
		} else {
			throw new NotYetImplemented();
		}
		if (this.expressions.isEmpty()) {
			return buffer.toString();
		}

		buffer.append(" where");
		String andOp = "";

		for (Entry<String, Expression<?>> exprEntry : this.expressions.entrySet()) {

			final Expression<?> expr = exprEntry.getValue();

			buffer.append(andOp);
			valueAsDql(buffer, expr);
			andOp = " and";
		}
		return buffer.toString();
	}
	
	@Override
	public int executeUpdate() {
		if (getQueryType() == QueryType.update) {
			throw new NotYetImplemented();
		}
		if ((getQueryType() != QueryType.select) && this.expressions.isEmpty()) {
			throw new XcpPersistenceException("Please confirm you really want to delete all objects");
		}
		setQuery(asDql());
		return super.executeUpdate();
	}

	@Override
	public int executeUpdate(boolean yesIReallyWantDeleteAll) {
		setQuery(asDql());
		return super.executeUpdate();
	}

	private void valueAsDql(StringBuffer buffer, Expression<?> expr) {
		if (expr.rightOpt.op() == Operator.in) {
			buffer.append(" ").append(expr.prop.getAttributeName()).append(" in (");
			List<?> values = expr.rightOpt.values();
			String comma = " ";
			for (Object value : values) {
				buffer.append(comma);
				String attributeValue = PersistentProperty.asDqlValue(value);
				buffer.append(attributeValue);
				comma = ", ";
			}
			buffer.append(" )");
		} else {
			String attributeValue = PersistentProperty.asDqlValue(expr.rightOpt.value());
			buffer.append(" ").append(expr.prop.getAttributeName()).append(" ")
					.append(expr.rightOpt.op().dqlOperator()).append(" ").append(attributeValue);
		}
	}

	private <B> void remember(Expression<B> expr) {
		expressions.put(expr.prop.getFieldName(), expr);
	}

	public QueryType getQueryType() {
		return queryType;
	}

}
