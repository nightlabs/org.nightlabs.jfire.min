/*
 * Created on Sep 2, 2005
 */
package org.nightlabs.ipanema.servermanager;

import org.nightlabs.ModuleException;

/**
 * This exception is thrown by
 * {@link org.nightlabs.ipanema.servermanager.JFireServerManager#createOrganisation(String, String, String, String, boolean)}
 * if the user intends to create the first organisation on a server and passes <tt>isServerAdmin = false</tt>.
 * This would result in a server without any server-administrator.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class NoServerAdminException extends ModuleException
{

	public NoServerAdminException()
	{
		super();
	}

	public NoServerAdminException(String message)
	{
		super(message);
	}

	public NoServerAdminException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public NoServerAdminException(Throwable cause)
	{
		super(cause);
	}

}
