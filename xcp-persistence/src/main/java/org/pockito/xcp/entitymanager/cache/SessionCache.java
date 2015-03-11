package org.pockito.xcp.entitymanager.cache;

import org.pockito.xcp.entitymanager.AnnotationInfo;
import org.pockito.xcp.entitymanager.AnnotationManager;
import org.pockito.xcp.entitymanager.PersistentProperty;
import org.pockito.xcp.entitymanager.XcpEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfTypedObject;

public class SessionCache implements SessionCacheWrapper {

	private final Logger logger = LoggerFactory.getLogger(SessionCache.class);
	
	private final XcpEntityManager em;
	private final AnnotationManager aiMgr;
	private final CacheWrapper<String, CacheElement> cacheMap;
	
	public SessionCache(XcpEntityManager em, CacheWrapper<String, CacheElement> cacheMap) {
		this.em = em;
		this.aiMgr = em.factory().getAnnotationManager();
		this.cacheMap = cacheMap;
	}
	
	/* (non-Javadoc)
	 * @see org.pockito.xcp.entitymanager.SessionCacheWrapper#size()
	 */
	@Override
	public int size() {
		return cacheMap.size();
	}

	/* (non-Javadoc)
	 * @see org.pockito.xcp.entitymanager.SessionCacheWrapper#get(java.lang.String)
	 */
	@Override
	public Object get(String key) {
		Object value = null;
		final CacheElement cacheElement = cacheMap.get(key);
		if (cacheElement != null) {
			logger.trace("retieve cached object: {}", key);
			boolean isUpToDate = isUpToDate(key, cacheElement);
			if (isUpToDate) {
				value = cacheElement.value;
			} else {
				logger.trace("cached object {} is not up-to-date.", key);
				remove(key);
				value = null;
			}
		}
		return value;
	}

	private boolean isUpToDate(String key, CacheElement cacheElement) {
		boolean upToDate = true;
		if (cacheElement.vstamp >= 0) {
			IDfSession session = null;
			try {
				session = em().getSession();
				AnnotationInfo ai = aiMgr().getAnnotationInfo(cacheElement.value.getClass());
				IDfTypedObject dmsObj = em().getDmsObj(session, ai, key, cacheElement.vstamp);
				upToDate = dmsObj != null;
			} catch (Exception e) {
				upToDate = false;
				if (session != null) {
					em().releaseSession(session);;
				}
			}
		}
		return upToDate;
	}

	/* (non-Javadoc)
	 * @see org.pockito.xcp.entitymanager.SessionCacheWrapper#put(java.lang.Object, org.pockito.xcp.entitymanager.AnnotationInfo)
	 */
	@Override
	public void put(Object value, AnnotationInfo ai) {
		final PersistentProperty keyProperty = ai.getIdMethod();
		final String key = (String) keyProperty.getProperty(value);
		final PersistentProperty vstampMethod = ai.getVStampMethod();
		int vstamp;
		if (vstampMethod == null) {
			vstamp = -1;
		} else {
			vstamp = (int) vstampMethod.getProperty(value);
		}
		CacheElement cacheElement = new CacheElement(value, vstamp);
		logger.trace("Puting object in cache: {}", key);
		cacheMap.put(key, cacheElement);
	}

	/* (non-Javadoc)
	 * @see org.pockito.xcp.entitymanager.SessionCacheWrapper#put(java.lang.Object)
	 */
	@Override
	public void put(Object value) {
		AnnotationInfo ai = aiMgr().getAnnotationInfo(value.getClass());
		put(value, ai);
	}

	/* (non-Javadoc)
	 * @see org.pockito.xcp.entitymanager.SessionCacheWrapper#remove(java.lang.String)
	 */
	@Override
	public CacheElement remove(String key) {
		return cacheMap.remove(key);
	}

	/* (non-Javadoc)
	 * @see org.pockito.xcp.entitymanager.SessionCacheWrapper#clear()
	 */
	@Override
	public void clear() {
		cacheMap.clear();
	}

	public XcpEntityManager em() {
		return em;
	}

	public AnnotationManager aiMgr() {
		return aiMgr;
	}

}
