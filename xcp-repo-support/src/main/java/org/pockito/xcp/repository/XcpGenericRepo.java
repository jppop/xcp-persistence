package org.pockito.xcp.repository;

import java.util.List;

import org.pockito.xcp.repository.command.XcpRepoCommand;

public interface XcpGenericRepo<T> {

	/**
	 * Adds a new object to the repository.
	 * 
	 * @param object
	 */
	void add(T object);

	String getAttachment(T entity, String filename);

	String getAttachment(T entity, String folder, String filename);

	/**
	 * Adds a list of objects
	 * @param objects
	 */
	void add(List<T> objects);

	/**
	 * Updates the object.
	 * 
	 * @param object
	 */
	void update(T object);

	/**
	 * Updates the list of objects.
	 * 
	 * @param objects
	 */
	void update(List<T> objects);

	/**
	 * Removes the object from the repository.
	 * 
	 * @param object
	 */
	void remove(T object);

	/**
	 * Removes the list of objects from the repository.
	 * 
	 * @param objects
	 */
	void remove(List<T> objects);

	/**
	 * Finds an object by its primary key (repository system id).
	 * 
	 * @param entityClass
	 * @param primaryKey
	 * @return
	 */
	T find(Class<T> entityClass, Object primaryKey);

	/**
	 * Finds an object by its primary key (repository system id).
	 * 
	 * @param primaryKey
	 * @return
	 */
	T find(Object primaryKey);

	/**
	 * Finds the child relative objects of a given parent.
	 * 
	 * Ie, finds all objects involved in a relation as a child.
	 * 
	 * @param parent
	 * @param relationClass
	 * @param optionalDqlFilter
	 * @return
	 */
	<R> List<T> findByParent(Object parent, Class<R> relationClass, String optionalDqlFilter);

	/**
	 * Finds the parent relative objects of a given parent.
	 * 
	 * Ie, finds all objects involved in a relation as a parent.
	 * 
	 * @param child
	 * @param relationClass
	 * @param optionalDqlFilter
	 * @return
	 */
	<R> List<T> findByChild(Object child, Class<R> relationClass, String optionalDqlFilter);

	/**
	 * Creates a command shared with all repositories.
	 * 
	 * The repository that creates the shared command is responsible for commit.
	 * 
	 * @return
	 */
	XcpRepoCommand createSharedCmd();

	/**
	 * @return the current command
	 */
	XcpRepoCommand getCurrentCmd();

	/**
	 * Commits the shared command.
	 * 
	 * Note that the Command will be reseted (ie, a new command will be created).
	 * 
	 */
	void commitSharedCmd();

	/**
	 * Rolls back a shared command.
	 */
	void rollbackSharedCmd();

	
	/**
	 * Uses a shared command if a command has been registered, create a new one if none.
	 * @return
	 */
	XcpRepoCommand cmd();

}
