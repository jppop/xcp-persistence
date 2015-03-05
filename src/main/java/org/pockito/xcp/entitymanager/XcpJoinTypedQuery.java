package org.pockito.xcp.entitymanager;

import java.util.List;

import org.pockito.xcp.annotations.XcpTypeCategory;
import org.pockito.xcp.exception.XcpPersistenceException;
import org.pockito.xcp.repository.DmsJoinTypedQuery;
import org.pockito.xcp.repository.JoinType;

public class XcpJoinTypedQuery<T, B> extends AbstractTypedQuery<T> implements DmsJoinTypedQuery<T, B> {

	private final Class<?> beanClass;
	private JoinType joinType;
	private String joindId;
	private String filter = null;
	
	public XcpJoinTypedQuery(XcpEntityManager em, Class<T> relationClass, Class<B> beanClass) {
		super(em, null, relationClass, false);
		this.beanClass = beanClass;
	}

	@Override
	public DmsJoinTypedQuery<T, B> select() {
		return this;
	}

	@Override
	public DmsJoinTypedQuery<T, B> join(Class<T> relation) {
		AnnotationInfo ai = em.getAnnotationInfo(relation);
		if (ai.getTypeCategory() != XcpTypeCategory.RELATION) {
			throw new XcpPersistenceException("the entity is not a relation entity");
		}
		if ((ai.getParentRelation() == null) || (ai.getChildRelation() == null)) {
			throw new XcpPersistenceException("the entity does not have Parent and Child fields");
		}
		return this;
	}

	@Override
	public DmsJoinTypedQuery<T, B> on(JoinType joinType) {
		setJoinType(joinType);
		return this;
	}

	@Override
	public DmsJoinTypedQuery<T, B> where(String filter) {
		setFilter(filter);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DmsJoinTypedQuery<T, B> setParameter(JoinType name, Object value) {
		setJoindId(value.toString());
		return (DmsJoinTypedQuery<T, B>) setParameter(name.toString(), value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<B> getRelatedResultList() {
		String dqlString = replaceQueryParameters(buildDql());
		return (List<B>) executeNativeQuery(getBeanType(), dqlString);
	}

	private String buildDql() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("select b.r_object_id");
		buffer.append(" from ")
			.append(getDmsType(getBeanType())).append(" b")
			.append(",").append(getDmsType(getEntityClass())).append("  r");
		buffer.append(" where");
		if (getJoinType() == JoinType.parentId) {
			buffer.append(" r.parent_id = b.r_object_id")
				.append(" and r.child_id = '").append(getJoindId()).append("'");
		} else {
			buffer.append(" r.child_id = b.r_object_id")
			.append(" and r.parent_id = '").append(getJoindId()).append("'");
		}
		if (getFilter() != null) {
			buffer.append(" and ").append(getFilter());
		}
		return buffer.toString();
	}

	public Class<?> getBeanType() {
		return beanClass;
	}

	public JoinType getJoinType() {
		return joinType;
	}

	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}

	public String getJoindId() {
		return joindId;
	}

	public void setJoindId(String joindId) {
		this.joindId = joindId;
	}

	private String getDmsType(Class<?> bean) {
		AnnotationInfo ai = em.getAnnotationInfo(bean);
		return ai.getDmsType();
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	@Override
	public List<T> getResultList() {
		throw new NotYetImplemented();
	}

}
