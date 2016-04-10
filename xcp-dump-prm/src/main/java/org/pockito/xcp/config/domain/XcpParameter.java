package org.pockito.xcp.config.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.pockito.xcp.annotations.Attribute;
import org.pockito.xcp.annotations.Transient;
import org.pockito.xcp.annotations.XcpEntity;
import org.pockito.xcp.annotations.XcpType;
import org.pockito.xcp.annotations.XcpTypeCategory;

@XcpEntity(namespace = "dmc")
@XcpType(type = XcpTypeCategory.BUSINESS_OBJECT, name = "xcp_app_config")
@XmlRootElement( name = "parameter" )
//@XmlType( propOrder = { "namespace", "config-name", "config-type", "properties" })
@XmlAccessorType(XmlAccessType.FIELD)
public class XcpParameter extends PersistedObject {

	public XcpParameter() {
		super();
	}

	@Attribute(name = "namespace")
	@XmlElement(name = "namespace")
	protected String namespace;

	@Attribute(name = "config_name")
	@XmlElement(name = "config-name")
	protected String configName;

	@Attribute(name = "config_type")
	@XmlElement(name = "config-type")
	protected String configType;

	@Attribute(name = "property_name")
	@XmlTransient
	protected Collection<String> propertyName;

	@Attribute(name = "property_value")
	@XmlTransient
	protected Collection<String> propertyValue;

	static public class Property {
		public Property() {}
		public Property(String name, String value) {
			super();
			this.name = name;
			this.value = value;
		}
		@XmlAttribute(name = "name")
		public String name;
		@XmlElement(name = "value")
		public String value;
		
		@Override
		public String toString() {
			return String.format(
					"XcpFormat.Property [name=%s, value=%s]",
					this.name, this.value
					);
		}
	}
	
	@Transient
	@XmlElementWrapper(name = "properties")
	@XmlElement(name = "property")
	Collection<Property> properties;
	
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getConfigType() {
		return configType;
	}

	public void setConfigType(String configType) {
		this.configType = configType;
	}

	public String getConfigName() {
		return configName;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public Collection<String> getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(Collection<String> propertyName) {
		this.propertyName = propertyName;
	}

	public Collection<String> getPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(Collection<String> propertyValue) {
		this.propertyValue = propertyValue;
	}
	
	@Override
	public String toString() {
		return String.format(
				"XcpFormat [namespace=%s, name=%s, type=%s, config-names=%s, config-type=%s]",
				getNamespace(), getConfigName(), getConfigType(), getPropertyName().toString(), getPropertyValue().toString()
				);
	}

	public void normalizeProperty() {
		
		this.properties = new ArrayList<Property>();
		
		if ( (this.propertyName != null && this.propertyValue != null)
				&& (this.propertyName.size() == this.propertyValue.size())) {
			int n = this.propertyName.size();
			String[] names = new String[n];
			String[] values = new String[n];
			this.propertyName.toArray(names);
			this.propertyValue.toArray(values);
			for (int i = 0; i < names.length; i++) {
				Property prop = new Property(names[i], values[i]);
				this.properties.add(prop);
			}
		}
	}
	
	public void denormalizeProperty() {
		this.propertyName = new ArrayList<String>();
		this.propertyValue = new ArrayList<String>();
		for (Iterator<Property> iterator = this.properties.iterator(); iterator.hasNext();) {
			Property prop = iterator.next();
			propertyName.add(prop.name);
			propertyValue.add(prop.value);
		}
	}

}