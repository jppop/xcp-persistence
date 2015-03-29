package org.pockito.xcp.sample.todo.olddomain;

public class SerializedAddress {

	private String Street;
	private String postalcode;
	private String city;
	private String country;
	public String getStreet() {
		return Street;
	}
	public void setStreet(String street) {
		Street = street;
	}
	public String getPostalcode() {
		return postalcode;
	}
	public void setPostalcode(String postalcode) {
		this.postalcode = postalcode;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
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
				"SerializedAddress [%s - %s %s (%s)]",
				getStreet(), getPostalcode(), getCity(), getCountry()
				);
	}

}
