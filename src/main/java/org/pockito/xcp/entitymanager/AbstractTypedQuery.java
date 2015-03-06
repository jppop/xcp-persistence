package org.pockito.xcp.entitymanager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.pockito.xcp.repository.DmsTypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.IDfId;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;

public abstract class AbstractTypedQuery<T> extends AbstractQuery implements DmsTypedQuery<T> {

	static final Logger logger = LoggerFactory.getLogger(AbstractTypedQuery.class);

	private static Joiner fieldJoiner = Joiner.on(", ").skipNulls();
	
	protected final Class<T> entityClass;

	public AbstractTypedQuery(XcpEntityManager em, String qlString) {
		this(em, qlString, null, false);
	}

	public AbstractTypedQuery(XcpEntityManager em, String qlString, Class<T> entityClass, boolean nativeQuery) {
		super(em, qlString, nativeQuery);
		this.entityClass = entityClass;
	}

	public AbstractTypedQuery(XcpEntityManager em, String qlString, boolean nativeQuery) {
		super(em, qlString, nativeQuery);
		this.entityClass = null;
	}

	public Class<T> getEntityClass() {
		return entityClass;
	}

	@Override
	public DmsTypedQuery<T> setParameter(String name, Object value) {
		getParameters().put(name, value);
		return this;
	}

	@Override
	public DmsTypedQuery<T> setParameter(int position, Object value) {
		throw new NotYetImplemented();
	}

	@Override
	public DmsTypedQuery<T> setHint(String hintName, Object value) {
		getHints().put(hintName, value);
		return this;
	}

	@Override
	public DmsTypedQuery<T> setMaxResults(int maxResult) {
		getHints().put("RETURN_TOP", maxResult);
		return this;
	}

	protected List<?> executeNativeQuery(Class<?> beanClass, String dqlString) {
		logger.debug("executeNativeQuery: {}", getDqlString());
		Stopwatch stopwatch = Stopwatch.createStarted();
		final List<IDfId> rawResultList;
		final List<T> resultList = new ArrayList<T>();
		IDfSession session = dctmDriver().getSession();
		logger.debug("Got session at {}", stopwatch.elapsed(TimeUnit.MILLISECONDS));
		try {
			// TODO: Not optimized! Should use a query with the entity fields as
			// columns
			logger.debug("Original query: {}", dqlString);
			String finalQuery = replaceQueryParameters(addHints(dqlString));
			logger.debug("Final query: {}", finalQuery);
			rawResultList = dctmDriver().getObjectsByQuery(session, finalQuery);
			for (IDfId objectId : rawResultList) {
				@SuppressWarnings("unchecked")
				T newInstance = (T) em().find(beanClass, objectId.toString());
				resultList.add(newInstance);
			}
		} finally {
			dctmDriver().releaseSession(session);
		}
		stopwatch.stop();
		logger.debug("query executed in: {}", stopwatch);
		return resultList;
	}

	protected String getDmsType(Class<?> bean) {
		AnnotationInfo ai = em.getAnnotationInfo(bean);
		return ai.getDmsType();
	}

	protected String getFields(String prefix) {
			AnnotationInfo ai = em().getAnnotationInfo(getEntityClass());
	//		String sep = "";
	//		StringBuffer buffer = new StringBuffer();
	//		for (PersistentProperty field : ai.getPersistentProperties()) {
	//			buffer.append(field.getAttributeName()).append(sep);
	//			sep = ", ";
	//		}
	//		return buffer.toString();
			ArrayList<String> attributes = new ArrayList<String>();
			for (PersistentProperty field : ai.getPersistentProperties()) {
				if (field.isAttribute()) {
					attributes.add(prefix + field.getAttributeName());
				}
			}
			return fieldJoiner.join(attributes);
		}

}