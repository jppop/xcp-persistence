package org.pockito.xcp.message;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public enum Message {
	E_OOPS_ERROR,
	E_ENTITYMGR_CREATION_FAILED, E_REMOVE_FAILED, E_NO_ID_KEY, E_FIND_FAILED, E_PERSIST_FAILED, E_NOT_PRIMITIVE_TYPE,
	E_ILLEGAL_RELATION_USE, E_REPO_OBJ_NOT_FOUND, E_ADD_ATTACHMENT_FAILED, E_ILLEGAL_ADD_RENDITION, E_CONTENTLESS_OBJ, 
	E_GET_ATTACHMENT_FAILED, E_PARENT_MISSING, E_CHILD_MISSING, E_UNKNOWN_FIELD, E_NOT_SELECT_QUERY, E_CONFIRM_DELETE_ALL,
	E_NOT_SUPPORTED, E_NO_ENTITY, E_NO_ATTRIBUTE_ID, E_NO_ENTITY_ANNOTATION, E_NO_TYPE_ANNOTATION,
	E_NO_ID_ANNOTATION, E_NO_JAVABEAN, E_REFLECTION_FIELD, E_NO_SETTER, E_COLLECTION_FIELD,
	
	E_DFC_SESSMGR_FAILED, E_DFC_SESSION_FAILED, E_DFC_QUERY_FAILED, E_DFC_NO_SESSIONMGR, E_TX_ALREADY_STARTED,
	E_TX_START_FAILED, E_TX_COMMIT_FAILED, E_TX_NOT_ACTIVE, E_TX_CANNNOT_COMMIT, E_TX_ROLLBACK_FAILED,
	
	E_NO_REPOSITORY_CONTEXT, E_NO_NAMES, E_NOT_PERSISTENT_OBJECT,
	;

	public static final String COMPONENT_CODE = "XDP"; // Xcp Data Persistence
	
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
