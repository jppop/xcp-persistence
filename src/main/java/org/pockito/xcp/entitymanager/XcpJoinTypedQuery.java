package org.pockito.xcp.entitymanager;

import java.util.ArrayList;
import java.util.List;

import org.pockito.xcp.annotations.XcpTypeCategory;
import org.pockito.xcp.exception.XcpPersistenceException;
import org.pockito.xcp.repository.DctmDriver.RowHandler;
import org.pockito.xcp.repository.DmsJoinTypedQuery;
import org.pockito.xcp.repository.JoinType;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;

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
		AnnotationInfo ai = em().getAnnotationInfo(relation);
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
	public List<B> getJoinedResultList() {
		String dqlString = buildJoinDql();
		return (List<B>) executeNativeQuery(getBeanType(), dqlString);
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

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> getResultList() {
		final List<T> resultList = new ArrayList<T>();
		final String dqlString = replaceQueryParameters(buildDql());
		final String beanIdAttribute;
		final PersistentProperty beanField;
		AnnotationInfo ai = em().getAnnotationInfo(getEntityClass());
		if (getJoinType() == JoinType.childId) {
			beanIdAttribute = "child_id";
			beanField = ai.getChildRelation();
		} else {
			beanIdAttribute = "parent_id";
			beanField = ai.getParentRelation();
		}
		IDfSession session = dctmDriver().getSession();
		try {
			dctmDriver().getObjectsByQuery(session, dqlString, new RowHandler() {
				
				@Override
				public void handleRow(IDfSession session, IDfCollection rs) throws DfException {
					IDfId relationId = rs.getId("r_object_id");
					T beanRelation = em().find(getEntityClass(), relationId);
					IDfId beanId = rs.getId(beanIdAttribute);
					B bean = (B) em().find(getBeanType(), beanId);
					if (bean != null) {
						beanField.setProperty(beanField, bean);
						resultList.add(beanRelation);
					}
				}
			});
		} finally {
			dctmDriver().releaseSession(session);
		}
		return resultList;
	}

	private String buildJoinDql() {
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

	private String buildDql() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("select r.r_object_id, r.parent_id, r.child_id");
//		String relationAttributes = getFields("r.");
//		if (!Strings.isNullOrEmpty(relationAttributes)) {
//			buffer.append(",").append(relationAttributes);
//		}
		buffer.append(" from ")
			.append(getDmsType(getEntityClass())).append("  r");
		buffer.append(" where");
		if (getJoinType() == JoinType.childId) {
			buffer.append(" r.child_id = '").append(getJoindId()).append("'");
		} else {
			buffer.append(" r.parent_id = '").append(getJoindId()).append("'");
		}
		if (getFilter() != null) {
			buffer.append(" and ").append(getFilter());
		}
		return buffer.toString();
	}
}
