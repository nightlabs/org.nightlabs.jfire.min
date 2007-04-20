package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.jface.wizard.WizardPage;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.structfield.ImageStructField;

public class ImageStructFieldFactory extends AbstractStructFieldFactory {

	public ImageStructField createStructField(StructBlock block, String organisationID, String fieldID, WizardPage wizardPage) {
		ImageStructField field = new ImageStructField(block, organisationID, fieldID);
		field.setMaxSizeKB(1024);
		field.addImageFormat("*");
		return field;
	}
}
