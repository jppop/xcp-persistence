package org.pockito.xcp.repository.test.domain;

import org.pockito.xcp.annotations.Attribute;
import org.pockito.xcp.annotations.GeneratedValue;
import org.pockito.xcp.annotations.Id;
import org.pockito.xcp.annotations.VStamp;
import org.pockito.xcp.annotations.XcpEntity;

@XcpEntity(namespace = "dm")
public class PersistedObject {

	@Id @GeneratedValue
	@Attribute(name = "r_object_id")
	private String id;
	
	@GeneratedValue @VStamp
	@Attribute(name = "i_vstamp")
	private int vStamp;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getvStamp() {
		return vStamp;
	}

	public void setvStamp(int vStamp) {
		this.vStamp = vStamp;
	}
}
