package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.jface.wizard.WizardPage;
import org.nightlabs.jfire.prop.AbstractStructField;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.structfield.RegexStructField;

public class RegexStructFieldFactory extends AbstractStructFieldFactory {

	public AbstractStructField createStructField(StructBlock block, WizardPage wizardPage) {
		return new RegexStructField(block);
	}
}
