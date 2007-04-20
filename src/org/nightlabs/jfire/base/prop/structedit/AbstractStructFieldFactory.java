package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.nightlabs.base.wizard.DynamicPathWizardPage;


public abstract class AbstractStructFieldFactory implements StructFieldFactory
{
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException
	{		
	}
	
	/**
	 * Extendors should overwrite this method if the creation of the new struct field requires additional user input.
	 * The creation wizard displays this page after the selection of the struct field types.
	 * Return null if no additional information is required.
	 */
	public DynamicPathWizardPage createWizardPage() {
		return null;
	}
}
