package org.pockito.xcp.config.domain;

import java.util.Collection;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "parameters" )
@XmlAccessorType(XmlAccessType.FIELD)
public class XcpParameters {

	public XcpParameters() {
		super();
	}

	public XcpParameters(Collection<XcpParameter> parameters) {
		super();
		this.parameters = parameters;
	}

	@XmlElement( name = "parameter" )
	Collection<XcpParameter> parameters;

	public Collection<XcpParameter> getParameters() {
		return parameters;
	}

	public void setParameters(Collection<XcpParameter> parameters) {
		this.parameters = parameters;
	}
	
	public void normalizeProperties() {
		for (Iterator<XcpParameter> iterator = parameters.iterator(); iterator.hasNext();) {
			XcpParameter xcpParameter = iterator.next();
			xcpParameter.normalizeProperty();
		}
	}
	public void denormalizeProperties() {
		for (Iterator<XcpParameter> iterator = parameters.iterator(); iterator.hasNext();) {
			XcpParameter xcpParameter = iterator.next();
			xcpParameter.denormalizeProperty();
		}
	}
}
