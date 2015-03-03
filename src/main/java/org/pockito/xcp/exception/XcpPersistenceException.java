package org.pockito.xcp.exception;

public class XcpPersistenceException extends RuntimeException {

	public XcpPersistenceException() {
		super();
	}

	public XcpPersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

	public XcpPersistenceException(String message) {
		super(message);
	}

	public XcpPersistenceException(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
