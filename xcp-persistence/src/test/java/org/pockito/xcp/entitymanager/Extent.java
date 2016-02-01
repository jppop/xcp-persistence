package org.pockito.xcp.entitymanager;

import org.pockito.xcp.annotations.Attribute;
import org.pockito.xcp.annotations.Id;
import org.pockito.xcp.annotations.XcpEntity;
import org.pockito.xcp.annotations.XcpType;
import org.pockito.xcp.annotations.XcpTypeCategory;

@XcpEntity(namespace = "dm")
@XcpType(type = XcpTypeCategory.BUSINESS_OBJECT, name = "extents")
public class Extent {

	@Id
	@Attribute(name = "segment_name")
	private String segmentName;
	
	@Attribute(name = "segment_type", readonly = true)
	private String segmentType;

	public String getSegmentName() {
		return segmentName;
	}

	public void setSegmentName(String segmentName) {
		this.segmentName = segmentName;
	}

	public String getSegmentType() {
		return segmentType;
	}

	public void setSegmentType(String segmentType) {
		this.segmentType = segmentType;
	}
	
}