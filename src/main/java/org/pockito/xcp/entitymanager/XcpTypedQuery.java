package org.pockito.xcp.entitymanager;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pockito.xcp.repository.DctmDriver;
import org.pockito.xcp.repository.DmsException;
import org.pockito.xcp.repository.DmsTypedQuery;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.IDfId;

public class XcpTypedQuery<T> implements DmsTypedQuery<T> {

	private final XcpEntityManager em;
	private final DctmDriver dctmDriver;
	private final String dqlString;
	private final Class<T> entityClass;
	private final boolean nativeQuery;

	protected final Map<String, Object> parameters = new HashMap<String, Object>();

	public XcpTypedQuery(XcpEntityManager em, String qlString) {
		this(em, qlString, null, false);
	}

	public XcpTypedQuery(XcpEntityManager em, String qlString, Class<T> entityClass) {
		this(em, qlString, entityClass, false);
	}

	public XcpTypedQuery(XcpEntityManager em, String qlString, Class<T> entityClass, boolean nativeQuery) {
		this.em = em;
		this.dqlString = qlString;
		this.dctmDriver = em.getDctmDriver();
		this.entityClass = entityClass;
		this.nativeQuery = nativeQuery;
	}

	@Override
	public List<T> getResultList() {
		final List<T> resultList;
		if (isNativeQuery()) {
			if (getEntityClass() == null)
				throw new DmsException("must provide an entity class");
			resultList = executeNativeQuery(getEntityClass());
		} else {
			resultList = null;
			throw new NotYetImplemented();
		}
		return resultList;
	}

	public XcpEntityManager em() {
		return em;
	}

	public DctmDriver dctmDriver() {
		return dctmDriver;
	}

	public String getDqlString() {
		return dqlString;
	}

	public Class<T> getEntityClass() {
		return entityClass;
	}

	public boolean isNativeQuery() {
		return nativeQuery;
	}

	private List<T> executeNativeQuery(Class<T> entityClass) {
		final List<IDfId> rawResultList;
		final List<T> resultList = new ArrayList<T>();
		IDfSession session = dctmDriver().getSession();
		try {
			// TODO: Not optimized! Should use a query with the entity fields as
			// columns
			String finalQuery = replaceQueryParameters(getDqlString(), getParameters());
			rawResultList = dctmDriver().getObjectsByQuery(session, finalQuery);
			for (IDfId objectId : rawResultList) {
				T newInstance = em().find(entityClass, objectId.toString());
				resultList.add(newInstance);
			}
		} finally {
			dctmDriver().releaseSession(session);
		}
		return resultList;
	}

	@Override
	public XcpTypedQuery<T> setParameter(String name, Object value) {
		parameters.put(name, value);
		return this;
	}

	@Override
	public XcpTypedQuery<T> setParameter(int position, Object value) {
		throw new NotYetImplemented();
	}

	private String replaceQueryParameters(String originalQuery, Map<String, Object> parameters) {
		originalQuery = originalQuery + " "; // pad a space on the end for easier matching
		for (Map.Entry<String, Object> entry : getParameters().entrySet()) {
			String stringVal = convertToSimpleValue(entry.getValue(), entry.getValue().getClass());
			originalQuery = originalQuery.replaceAll(":" + entry.getKey(), stringVal);
		}
		return originalQuery.trim();
	}

	protected String convertToSimpleValue(Object paramOb, @SuppressWarnings("rawtypes") Class retType) {
		String param;
		if (Integer.class.isAssignableFrom(retType)) {
			param = ((Integer) paramOb).toString();
		} else if (Long.class.isAssignableFrom(retType)) {
			param = ((Long) paramOb).toString();
		} else if (Double.class.isAssignableFrom(retType)) {
			param = ((Double) paramOb).toString();
		} else if (BigDecimal.class.isAssignableFrom(retType)) {
			param = ((BigDecimal) paramOb).toString();
		} else if (Date.class.isAssignableFrom(retType)) {
			param = encodeDate((Date) paramOb);
		} else { // string
			param = "'" + escapeQueryParam(paramOb.toString()) + "'";
		}
		return param;
	}

	/**
	 * static value hardcoding date format used for conversation of Date into
	 * String
	 */
	private static String dateFormat = "yyyy-MM-dd HH:mm:ss";
	private static SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);

	private String encodeDate(Date date) {
		return "DATE('"+ dateFormatter.format(date) + "','yyyy/mm/dd hh:mi:ss')";
	}

	protected String paramName(String param) {
		int colon = param.indexOf(":");
		if (colon == -1) {
			return null;
		}
		String paramName = param.substring(colon + 1);
		return paramName;
	}

	public static String escapeQueryParam(String str) {
		if (str == null) {
			return null;
		}
		String s;
		s = str.replace("'", "''");
		return s;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	@Override
	public XcpTypedQuery<T> setHint(String hintName, Object value) {
		throw new NotYetImplemented();
	}

	@Override
	public int setMaxResults(int maxResult) {
		throw new NotYetImplemented();
	}
}
