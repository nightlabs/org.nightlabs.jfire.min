package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.jface.wizard.WizardPage;
import org.nightlabs.base.wizard.DynamicPathWizardPage;
import org.nightlabs.jfire.base.DuplicateKeyException;
import org.nightlabs.jfire.prop.AbstractStructField;
import org.nightlabs.jfire.prop.SelectionStructField;
import org.nightlabs.jfire.prop.StructBlock;

public class SelectionStructFieldFactory extends AbstractStructFieldFactory
{
	public AbstractStructField createStructField(StructBlock block, String organisationID, String fieldID, WizardPage wizardPage)
	{
		SelectionStructField selField;
		try
		{
			selField = new SelectionStructField(block, organisationID, fieldID);
			block.addStructField(selField);
		}
		catch (DuplicateKeyException e)
		{
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
		return selField;		
	}

	public DynamicPathWizardPage createWizardPage()
	{
		// no additional information needed
		return null;
	}
}
