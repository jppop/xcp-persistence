package org.pockito.xcp.repository;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public enum Message {
	E_OOPS_ERROR,
	E_CMD_NO_STARTED, E_NO_PARENT, E_NO_CHILD, E_INSTANCIATION_FAILED, E_NOT_RELATION_TYPE, E_NO_PARENT_SETTER,
	E_NO_CHILD_SETTER, E_CMD_CREATION_FAILED, E_CMD_INPROGRESS,
	;

	public static final String COMPONENT_CODE = "XRY"; // xCP Repository Support
	
	private static final ResourceBundle resource = Utf8ResourceBundle.getBundle(Message.class.getName());

	public String get() {
		return get(resource, this, null);
	}
	
	public String get(Object... args) {
		return getMessage(resource, this, args);
	}

	public static String getMessage(ResourceBundle resource, @SuppressWarnings("rawtypes") Enum messageCode,
			Object... args) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[").append(COMPONENT_CODE).append(String.format("%04d", messageCode.ordinal())).append("] ")
			.append(messageCode.toString()).append(" - ");
		try {
			final String format = resource.getString(messageCode.toString());
			final String msg = MessageFormat.format(format, args);
			buffer.append(msg);
		} catch (MissingResourceException e) {
			buffer.append("!! message not found in the resource bundle !!");
		}
		return buffer.toString();
	}

}
