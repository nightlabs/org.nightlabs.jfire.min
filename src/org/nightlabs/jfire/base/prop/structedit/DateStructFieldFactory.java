package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.jface.wizard.WizardPage;
import org.nightlabs.base.wizard.DynamicPathWizardPage;
import org.nightlabs.jfire.base.DuplicateKeyException;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.structfield.DateStructField;
import org.nightlabs.l10n.DateFormatter;

public class DateStructFieldFactory extends AbstractStructFieldFactory {

	public DateStructField createStructField(StructBlock block, String organisationID, String fieldID, WizardPage wizardPage) {
		DateStructField field = new DateStructField(block, organisationID, fieldID);
		field.setDateTimeEditFlags(DateFormatter.FLAGS_DATE_LONG);
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
