package org.pockito.xcp.repository;

import java.util.Map;


/**
 * The <code>DmsEntityManagerFactory</code> interface is used 
 * by the application to obtain an application-managed DMS entity 
 * manager. When the application has finished using the entity 
 * manager factory, and/or at application shutdown, the 
 * application should close the entity manager factory. 
 * Once an <code>DmsEntityManagerFactory</code> has been closed, all its entity 
 * managers are considered to be in the closed state.
 *
 */
public interface DmsEntityManagerFactory {

    /**
     * Create a new DmsEntityManager.
     * This method returns a new DmsEntityManager instance each time
     * it is invoked.
     * The isOpen method will return true on the returned instance.
     */
	DmsEntityManager createDmsEntityManager();

    /**
     * Create a new DmsEntityManager with the specified Map of
     * properties.
     * This method returns a new DmsEntityManager instance each time
     * it is invoked.
     * The isOpen method will return true on the returned instance.
     */
	DmsEntityManager createDmsEntityManager(Map<String, ?> map);

    /**
     * Close the factory, releasing any resources that it holds.
     * After a factory instance is closed, all methods invoked on
     * it will throw an IllegalStateException, except for isOpen,
     * which will return false. Once an EntityManagerFactory has
     * been closed, all its entity managers are considered to be
     * in the closed state.
     */
    void close();

    /**
    * Indicates whether or not this factory is open. Returns true
    * until a call to close has been made.
    */
    public boolean isOpen();
}
