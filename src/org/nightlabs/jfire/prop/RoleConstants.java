package org.nightlabs.jfire.prop;

import org.nightlabs.jfire.security.id.RoleID;

/**
 * Class used for defining constants for certain security roles.
 * @author Frederik Loeser - frederik[at]nightlabs[dot]de
 */
public class RoleConstants {

	public static final RoleID seePropertySet = RoleID.create("org.nightlabs.jfire.prop.seePropertySet");
	public static final RoleID editPropertySet = RoleID.create("org.nightlabs.jfire.prop.editPropertySet");
	
}
