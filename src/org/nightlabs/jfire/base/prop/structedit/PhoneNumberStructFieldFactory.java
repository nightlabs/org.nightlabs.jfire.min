package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.jface.wizard.WizardPage;
import org.nightlabs.base.wizard.DynamicPathWizardPage;
import org.nightlabs.jfire.prop.AbstractStructField;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.structfield.PhoneNumberStructField;

public class PhoneNumberStructFieldFactory extends AbstractStructFieldFactory {

	public AbstractStructField createStructField(StructBlock block, String organisationID, String fieldID,
			WizardPage wizardPage) {
		PhoneNumberStructField structField = new PhoneNumberStructField(block, organisationID, fieldID);
		return structField;
	}

	public DynamicPathWizardPage createWizardPage() {
		return null;
	}

}
