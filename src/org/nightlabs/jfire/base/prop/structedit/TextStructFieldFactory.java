package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.jface.wizard.WizardPage;
import org.nightlabs.jfire.prop.AbstractStructField;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.structfield.TextStructField;

public class TextStructFieldFactory extends AbstractStructFieldFactory {
	public AbstractStructField createStructField(StructBlock block, String organisationID, String fieldID, WizardPage wizardPage) {
		return new TextStructField(block, organisationID, fieldID);
	}
}