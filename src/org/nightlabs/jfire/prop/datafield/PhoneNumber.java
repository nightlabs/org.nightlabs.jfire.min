package org.nightlabs.jfire.prop.datafield;

/**
 * Immutable class to represent phone numbers.
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class PhoneNumber {
	private String countryCode;
	private String areaCode;
	private String localNumber;

	public PhoneNumber(String countryCode, String areaCode, String localNumber) {
		super();
		this.countryCode = countryCode;
		this.areaCode = areaCode;
		this.localNumber = localNumber;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public String getLocalNumber() {
		return localNumber;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		if (countryCode != null && countryCode.trim().length() > 0)
			sb.append("+").append(countryCode);

		if (areaCode != null && areaCode.trim().length() > 0) {
			if (countryCode != null && !countryCode.trim().isEmpty())
				sb.append("-");
			sb.append(areaCode);
		}

		if (localNumber != null && localNumber.trim().length() > 0) {
			if (areaCode != null && !areaCode.trim().isEmpty())
				sb.append("-");
			sb.append(localNumber);
		}

		return sb.toString();
	}
}