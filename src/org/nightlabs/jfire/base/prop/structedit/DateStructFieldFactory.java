package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.jface.wizard.WizardPage;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.structfield.DateStructField;
import org.nightlabs.l10n.DateFormatter;

public class DateStructFieldFactory extends AbstractStructFieldFactory {

	public DateStructField createStructField(StructBlock block, WizardPage wizardPage) {
		DateStructField field = new DateStructField(block);
		field.setDateTimeEditFlags(DateFormatter.FLAGS_DATE_LONG);
		return field;
	}
}
