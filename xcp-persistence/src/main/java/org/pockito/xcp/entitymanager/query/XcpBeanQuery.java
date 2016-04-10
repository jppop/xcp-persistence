package org.pockito.xcp.entitymanager.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pockito.xcp.entitymanager.NotYetImplemented;
import org.pockito.xcp.entitymanager.XcpEntityManager;
import org.pockito.xcp.entitymanager.api.DctmDriver;
import org.pockito.xcp.entitymanager.api.DmsBeanQuery;
import org.pockito.xcp.entitymanager.api.DmsTypedQuery;
import org.pockito.xcp.entitymanager.api.MetaData;
import org.pockito.xcp.entitymanager.api.PersistentProperty;
import org.pockito.xcp.exception.XcpPersistenceException;
import org.pockito.xcp.message.Message;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;

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
			throw new XcpPersistenceException(Message.E_UNKNOWN_FIELD.get(fieldName));
		}
		Expression<B> expr = new Expression<B>(prop, rightOp);
		remember(expr);
		return this;
	}

	@Override
	public DmsBeanQuery<T> setHint(String hintName, Object value) {
		return (DmsBeanQuery<T>) super.setHint(hintName, value);
	}

	@Override
	public DmsBeanQuery<T> setMaxResults(int maxResult) {
		return (DmsBeanQuery<T>) super.setMaxResults(maxResult);
	}

	@Override
	public DmsBeanQuery<T> setOrder(String fieldName, OrderDirection direction) {
		final PersistentProperty prop = meta.getPersistentProperty(fieldName);
		if (prop == null) {
			throw new XcpPersistenceException(Message.E_UNKNOWN_FIELD.get(fieldName));
		}
		String property = prop.getAttributeName();
		return (DmsBeanQuery<T>) super.setOrder(property, direction);
	}
	
	@Override
	public List<T> getResultList() {
		if (getQueryType() != QueryType.select) {
			throw new XcpPersistenceException(Message.E_NOT_SELECT_QUERY.get());
		}
		RowHandler rowHandler = new RowHandler();
		IDfSession session = dctmDriver().getSession();
		try {
			dctmDriver().getObjectsByQuery(session, asDql(), rowHandler);
		} finally {
			dctmDriver().releaseSession(session);
		}
		return rowHandler.getResultList();
	}

	class RowHandler implements DctmDriver.RowHandler {
		
		final List<T> resultList = new ArrayList<T>();
		
		@Override
		public void handleRow(IDfSession session, IDfCollection rs) throws DfException {
			
			IDfTypedObject rawObject = rs.getTypedObject();
			T newInstance = (T) em().load(getEntityClass(), rawObject);
			resultList.add(newInstance);
		}

		public List<T> getResultList() {
			return resultList;
		}

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
			buffer.append("select ").append(getColums(meta)).append(" from ")
					.append(meta.getDmsType());
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
		return addHints(addOrders(buffer.toString()));
	}

	public String getColums(MetaData meta) {
		StringBuffer buffer = new StringBuffer();
		Collection<PersistentProperty> props = meta.getPersistentProperties();
		String comma = "";
		for (PersistentProperty property : props) {
			buffer.append(comma).append(property.getAttributeName());
			comma = ",";
		}
		return buffer.toString();
	}

	@Override
	public int executeUpdate() {
		if (getQueryType() == QueryType.update) {
			throw new NotYetImplemented();
		}
		if ((getQueryType() != QueryType.select) && this.expressions.isEmpty()) {
			throw new XcpPersistenceException(Message.E_CONFIRM_DELETE_ALL.get());
		}
		setQuery(asDql());
		return super.executeUpdate();
	}

	@Override
	public int executeUpdate(boolean yesIReallyWantDeleteAll) {
		setQuery(asDql());
		return super.executeUpdate();
	}

	@Override
	public DmsTypedQuery<T> setParameter(int position, Object value) {
		throw new IllegalAccessError(Message.E_NOT_SUPPORTED.get());
	}

	@Override
	public DmsTypedQuery<T> setParameter(String name, Object value) {
		throw new IllegalAccessError(Message.E_NOT_SUPPORTED.get());
	}

	private void valueAsDql(StringBuffer buffer, Expression<?> expr) {
		if ( expr.prop.isRepeating() ) {
			buffer.append(" any");
		}
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
		} else if (expr.rightOpt.op() == Operator.is) {
			String attributeValue = String.class.cast(expr.rightOpt.value()).toString();
			buffer.append(" ").append(expr.prop.getAttributeName()).append(" ")
					.append(expr.rightOpt.op().dqlOperator()).append(" ").append(attributeValue);
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
