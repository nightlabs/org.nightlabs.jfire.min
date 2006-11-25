package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.jface.wizard.WizardPage;
import org.nightlabs.base.wizard.DynamicPathWizardPage;
import org.nightlabs.jfire.base.DuplicateKeyException;
import org.nightlabs.jfire.prop.AbstractStructField;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.TextStructField;

public class TextStructFieldFactory extends AbstractStructFieldFactory
{
	public AbstractStructField createStructField(StructBlock block, String organisationID, String fieldID, WizardPage wizardPage)
	{
		TextStructField textField;
		try
		{
			textField = new TextStructField(block, organisationID, fieldID);
			block.addStructField(textField);
		}
		catch (DuplicateKeyException e)
		{
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
		return textField;
	}

	public DynamicPathWizardPage createWizardPage()
	{
		// no additional information needed
		return null;
	}
}
