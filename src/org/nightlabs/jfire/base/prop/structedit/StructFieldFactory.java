package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.wizard.WizardPage;
import org.nightlabs.base.wizard.DynamicPathWizardPage;
import org.nightlabs.jfire.prop.AbstractStructField;
import org.nightlabs.jfire.prop.StructBlock;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de --> 
 */
public interface StructFieldFactory extends IExecutableExtension
{
	/**
	 * Here implementors should return a {@link WizardPage} that serves to acquire additional information that is
	 * needed for the creation of the struct field. Can be <code>null</code> if there is no extra info needed.
	 * 
	 * @return A {@link WizardPage} to acquire additional information needed for the creation of the struct field.
	 */
	public DynamicPathWizardPage createWizardPage();
	
	/**
	 * Creates the struct field using the given information. <code>wizardPage</code> can be <code>null</code> if
	 * no additional information for this struct field is needed to create it.
	 *  
	 * @param block The {@link StructBlock} in which this struct field should be created.
	 * @param wizardPage A {@link WizardPage} that serves to acquire additional information for the creation of
	 * 				the struct field.	 * 
	 * @return The {@link AbstractStructField} that has just been created.
	 */
	public AbstractStructField createStructField(StructBlock block, WizardPage wizardPage);
}
