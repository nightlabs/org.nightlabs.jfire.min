package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.jface.wizard.WizardPage;
import org.nightlabs.base.wizard.DynamicPathWizardPage;
import org.nightlabs.jfire.base.DuplicateKeyException;
import org.nightlabs.jfire.prop.AbstractStructField;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.structfield.NumberStructField;

public class NumberStructFieldFactory extends AbstractStructFieldFactory {

	public AbstractStructField createStructField(StructBlock block, String organisationID, String fieldID, WizardPage wizardPage) {
		NumberStructField field = new NumberStructField(block, organisationID, fieldID);
		try {
			block.addStructField(field);
		} catch (DuplicateKeyException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return field;
	}

	public DynamicPathWizardPage createWizardPage() {
		// no additional information needed
		return null;
	}
}
