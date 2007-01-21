package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.jface.wizard.WizardPage;
import org.nightlabs.base.wizard.DynamicPathWizardPage;
import org.nightlabs.jfire.base.DuplicateKeyException;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.structfield.ImageStructField;

public class ImageStructFieldFactory extends AbstractStructFieldFactory {

	public ImageStructField createStructField(StructBlock block, String organisationID, String fieldID, WizardPage wizardPage) {
		ImageStructField field = new ImageStructField(block, organisationID, fieldID);
		field.setMaxHeight(50);
		field.setMaxWidth(50);
		field.addImageFormat("*");
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
