// Copyright (C) 2011 Pockito
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.pockito.xcp.entitymanager.api;


public interface DmsEntityManager {

	<T> T find(Class<T> entityClass, Object primaryKey);

	void persist(Object entity);

	void remove(Object entity);

	<T> DmsTypedQuery<T> createNamedQuery(String qlString);

	DmsQuery createNativeQuery(String dqlString);

	<T> DmsTypedQuery<T> createNativeQuery(String qlString, Class<T> entityClass);

	void addAttachment(Object entity, String filename, String contentType);

	String getAttachment(Object entity, String folder, String filename);

	Transaction getTransaction();

	public <T> MetaData getMetaData(Class<T> entityClass);

	void close();

	/**
	 * Creates a query to find children of a given parent.
	 * 
	 * @param parent
	 * @param relationClass
	 * @param childClass
	 * @param optionalDqlFilter
	 * @return
	 */
	<T,R> DmsTypedQuery<T> createChildRelativesQuery(Object parent, Class<R> relationClass, Class<T> childClass,
			String optionalDqlFilter);

	/**
	 * Creates a query to find parents of a given child.
	 * 
	 * @param child
	 * @param relationClass
	 * @param parentClass
	 * @param optionalDqlFilter
	 * @return
	 */
	<T,R> DmsTypedQuery<T> createParentRelativesQuery(Object child, Class<R> relationClass, Class<T> parentClass,
			String optionalDqlFilter);

	<T> DmsBeanQuery<T> createBeanQuery(Class<T> entityClass);

}
