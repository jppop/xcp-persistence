package org.pockito.xcp.entitymanager;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.pockito.xcp.repository.DctmDriver;

import com.documentum.fc.client.IDfSession;

public abstract class AbstractQuery {

	protected final XcpEntityManager em;
	protected final DctmDriver dctmDriver;
	protected final String dqlString;
	protected final boolean nativeQuery;
	protected final Map<String, Object> parameters = new HashMap<String, Object>();
	protected final Map<String, Object> hints = new HashMap<String, Object>();

	public AbstractQuery(XcpEntityManager em, String qlString, boolean nativeQuery) {
		this.em = em;
		this.dqlString = qlString;
		this.dctmDriver = em.getDctmDriver();
		this.nativeQuery = nativeQuery;
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

	public boolean isNativeQuery() {
		return nativeQuery;
	}

	public int executeUpdate() {
		final int count;
		IDfSession session = dctmDriver().getSession();
		try {
			count = dctmDriver().executeQuery(session, getDqlString());
		} finally {
			dctmDriver().releaseSession(session);
		}
		return count;
	}
	
	protected String replaceQueryParameters(final String originalQuery) {
		String finalQuery = originalQuery;
		for (Map.Entry<String, Object> entry : getParameters().entrySet()) {
			String stringVal = convertToSimpleValue(entry.getValue(), entry.getValue().getClass());
			finalQuery = finalQuery.replaceAll(":" + entry.getKey(), stringVal);
		}
		return finalQuery;
	}

	protected String addHints(final String originalQuery) {
		if (getHints().size() > 0) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(originalQuery).append(" ENABLE (");
			String sep = " ";
			for (Map.Entry<String, Object> entry : getHints().entrySet()) {
				buffer.append(sep).append(entry.getKey()).append(" ").append(entry.getValue().toString());
				sep = ", ";
			}
			buffer.append(")");
			return buffer.toString();
		} else {
			return originalQuery;
		}
	}

	protected String convertToSimpleValue(final Object paramOb, final @SuppressWarnings("rawtypes") Class retType) {
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
	
//	protected void clearParams() {
//		this.parameters.clear();
//		this.hints.clear();
//	}
//
	/**
	 * static value hardcoding date format used for conversation of Date into
	 * String
	 */
	public static String dateFormat = "yyyy-MM-dd HH:mm:ss";
	private static SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);

	private String encodeDate(Date date) {
		return "DATE('"+ dateFormatter.format(date) + "','yyyy/mm/dd hh:mi:ss')";
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

	public Map<String, Object> getHints() {
		return hints;
	}

}
