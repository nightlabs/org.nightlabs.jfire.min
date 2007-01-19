package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;


public abstract class AbstractStructFieldFactory implements StructFieldFactory
{
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException
	{		
	}	
}
