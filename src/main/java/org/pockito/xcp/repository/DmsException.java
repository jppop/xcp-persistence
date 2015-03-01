package org.pockito.xcp.repository;

public class DmsException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DmsException() {
		super();
	}

	public DmsException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public DmsException(final String message) {
		super(message);
	}

	public DmsException(final Throwable cause) {
		super(cause);
	}
	
}