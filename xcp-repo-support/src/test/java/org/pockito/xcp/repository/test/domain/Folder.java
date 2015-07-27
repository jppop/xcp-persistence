package org.pockito.xcp.repository.test.domain;

import java.util.Date;

import org.pockito.xcp.annotations.Attribute;
import org.pockito.xcp.annotations.ParentFolder;
import org.pockito.xcp.annotations.XcpEntity;
import org.pockito.xcp.annotations.XcpType;
import org.pockito.xcp.annotations.XcpTypeCategory;
import org.pockito.xcp.repository.PersistedObject;

@XcpEntity(namespace = "dm")
@XcpType(type = XcpTypeCategory.CONTENT, name = "folder")
public class Folder extends PersistedObject {

	@Attribute(name = "object_name")
	private String name;

	@Attribute(name = "subject")
	private String subject;

	@Attribute(name = "title")
	private String title;

	@Attribute(name = "r_creation_date", readonly = true)
	private Date creationDate;

	@ParentFolder
	private String parentFolder;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getParentFolder() {
		return parentFolder;
	}

	public void setParentFolder(String parentFolder) {
		this.parentFolder = parentFolder;
	}

	@Override
	public String toString() {
		return String.format(
				"Document[id=%s, name=%s, creation date=%tc, parent folder=%s, vstamp=%d]",
				getId(), getName(),
				creationDate, getParentFolder(), getvStamp()
				);
	}

}
