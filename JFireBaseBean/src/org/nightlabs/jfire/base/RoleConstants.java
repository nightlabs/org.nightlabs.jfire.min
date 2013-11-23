package org.nightlabs.jfire.base;

import org.nightlabs.jfire.security.id.RoleID;

/**
 * This class holds {@link RoleID} constants used within the JFireBase module and not fitting into
 * one of the specific <code>RoleConstants</code> classes (like {@link org.nightlabs.jfire.security.RoleConstants} for example).
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public class RoleConstants
{
	private RoleConstants() { }

	public static final RoleID attachScreenShotToErrorReport_default = RoleID.create("org.nightlabs.jfire.base.attachScreenShotToErrorReport#default");
	public static final RoleID attachScreenShotToErrorReport_decide = RoleID.create("org.nightlabs.jfire.base.attachScreenShotToErrorReport#decide");
}
