package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.jface.wizard.WizardPage;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.structfield.ImageStructField;

public class ImageStructFieldFactory extends AbstractStructFieldFactory {

	public ImageStructField createStructField(StructBlock block, WizardPage wizardPage) {
		ImageStructField field = new ImageStructField(block);
		field.setMaxSizeKB(1024);
		field.addImageFormat("*"); //$NON-NLS-1$
		return field;
	}
}
