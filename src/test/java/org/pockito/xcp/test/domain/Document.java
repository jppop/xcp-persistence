package org.pockito.xcp.test.domain;

import java.util.Date;

import org.pockito.xcp.annotations.Attribute;
import org.pockito.xcp.annotations.XcpEntity;
import org.pockito.xcp.annotations.XcpType;
import org.pockito.xcp.annotations.XcpTypes;

@XcpEntity(namespace = "dm")
@XcpType(type = XcpTypes.CONTENT, name = "document")
public class Document extends PersistedObject {

	@Attribute(name = "object_name")
	private String name;
	
	@Attribute(name = "r_creation_date", readonly = true)
	private Date creationDate;

	@Attribute(name = "a_status")
	private String status;

	@Attribute(name="i_has_folder", readonly=true)
	private Boolean hasFolder;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Boolean getHasFolder() {
		return hasFolder;
	}

	public void setHasFolder(Boolean hasFolder) {
		this.hasFolder = hasFolder;
	}

}
