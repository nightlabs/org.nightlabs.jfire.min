package org.nightlabs.jfire.base;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class InitBean
extends BaseSessionBeanImpl
implements InitRemote
{
	@RolesAllowed("_System_")
	@Override
	public void initialise()
	{
		// do nothing
	}
}
