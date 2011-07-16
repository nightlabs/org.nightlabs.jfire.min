package org.nightlabs.jfire.organisationinit;

import java.util.Comparator;

public class OrganisationInitComparator implements Comparator<OrganisationInit> {
	public int compare(OrganisationInit o1, OrganisationInit o2) {
		int prioDiff = o1.getPriority() - o2.getPriority();
		if (prioDiff != 0)
			return prioDiff;
		else
			return o1.getName().compareTo(o2.getName());
	}
}