package org.nightlabs.jfire.crossorganisationregistrationinit;

import org.nightlabs.jfire.init.InitException;

/**
 * @author unascribed
 * @deprecated Should use InitException directly.
 */
@Deprecated
public class CrossOrganisationRegistrationInitException extends InitException {
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	public CrossOrganisationRegistrationInitException(String msg) {
		super(msg);
	}
}
