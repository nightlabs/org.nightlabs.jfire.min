package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.jface.wizard.WizardPage;
import org.nightlabs.jfire.prop.AbstractStructField;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.structfield.NumberStructField;

public class NumberStructFieldFactory extends AbstractStructFieldFactory {

	public AbstractStructField createStructField(StructBlock block, String organisationID, String fieldID, WizardPage wizardPage) {
		return new NumberStructField(block, organisationID, fieldID);
	}
}
