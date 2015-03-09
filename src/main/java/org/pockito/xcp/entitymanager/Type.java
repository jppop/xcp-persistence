package org.pockito.xcp.entitymanager;

import com.documentum.fc.client.IDfType;

public enum Type {
	Boolean(IDfType.DF_BOOLEAN), Integer(IDfType.DF_INTEGER), String(IDfType.DF_STRING),
	Id(IDfType.DF_ID), Time(IDfType.DF_TIME), Double(IDfType.DF_DOUBLE),
	Undefined(IDfType.DF_UNDEFINED);
	
	private int code;

	Type(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

}