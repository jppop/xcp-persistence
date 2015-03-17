package org.pockito.xcp.repository.test.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.pockito.xcp.annotations.Attribute;
import org.pockito.xcp.annotations.ParentFolder;
import org.pockito.xcp.annotations.XcpEntity;
import org.pockito.xcp.annotations.XcpType;
import org.pockito.xcp.annotations.XcpTypeCategory;
import org.pockito.xcp.repository.PersistedObject;

@XcpEntity(namespace = "dm")
@XcpType(type = XcpTypeCategory.CONTENT, name = "document")
public class Workflow extends PersistedObject {

	@Attribute(name = "object_name")
	private String name;

	@Attribute(name = "subject")
	private String subject;

	@Attribute(name = "title")
	private String title;

	@Attribute(name = "r_creation_date", readonly = true)
	private Date creationDate;

	@Attribute(name = "a_status")
	private String status;

	@Attribute(name = "i_has_folder", readonly = true)
	private Boolean hasFolder;

	@Attribute(name = "a_content_type", readonly = true)
	private String contentType;

	@Attribute(name = "r_content_size", readonly = true)
	private int contentSize;

	@ParentFolder
	private String parentFolder;
	
	// FIXME: r_version_label is readonly but can be modified (mark api)
	@Attribute(name = "r_version_label", readonly = true)
	protected Collection<String> versionLabels = new ArrayList<String>();

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

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public int getContentSize() {
		return contentSize;
	}

	public void setContentSize(int contentSize) {
		this.contentSize = contentSize;
	}

	public Collection<String> getVersionLabels() {
		return versionLabels;
	}

	public void setVersionLabels(Collection<String> versionLabels) {
		this.versionLabels = versionLabels;
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
				"Document[id=%s, name=%s, versions=%s, creation date=%tc, size=%d, parent folder=%s, vstamp=%d]",
				getId(), name, Arrays.toString(versionLabels.toArray()),
				creationDate, contentSize, getParentFolder(), getvStamp()
				);
	}

}
