package org.pockito.xcp.sample.todo.domain;

import org.pockito.xcp.annotations.Attribute;
import org.pockito.xcp.annotations.XcpEntity;
import org.pockito.xcp.annotations.XcpType;
import org.pockito.xcp.annotations.XcpTypeCategory;
import org.pockito.xcp.repository.PersistedObject;

@XcpEntity(namespace = "todo")
@XcpType(type = XcpTypeCategory.BUSINESS_OBJECT, name = "address")
public class Address extends PersistedObject {

	@Attribute(name = "street_line_1")
	private String streetLine1;
	
	@Attribute(name = "street_line_2")
	private String streetLine2;
	
	@Attribute(name = "city")
	private String city;
	
	@Attribute(name = "zipcode")
	private String zipcode;
	
	@Attribute(name = "country")
	private String country;
	
	public String getStreetLine1() {
		return streetLine1;
	}

	public void setStreetLine1(String streetLine1) {
		this.streetLine1 = streetLine1;
	}

	public String getStreetLine2() {
		return streetLine2;
	}

	public void setStreetLine2(String streetLine2) {
		this.streetLine2 = streetLine2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@Override
	public String toString() {
		return String.format(
				"Address[id=%s, Street Line 1=%s, Street Line 2=%s, zipcode=%s, city=%s], country=%s",
				getId(),
				getStreetLine1(), getStreetLine2(), getZipcode(), getCity(), getCountry()
				);
	}
}
