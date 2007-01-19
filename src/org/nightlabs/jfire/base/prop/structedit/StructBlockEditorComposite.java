package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.composite.LabeledCheckboxComposite;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.language.I18nTextEditor;
import org.nightlabs.base.language.LanguageChooser;
import org.nightlabs.base.language.I18nTextEditor.EditMode;
import org.nightlabs.jfire.prop.StructBlock;

public class StructBlockEditorComposite extends XComposite {
	private I18nTextEditor blockNameEditor;
	LabeledCheckboxComposite checkComp;
	private StructBlock block;

	public StructBlockEditorComposite(Composite parent, int style, LanguageChooser languageChooser) {
		super(parent, style, LayoutMode.TOP_BOTTOM_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);

		this.setVisible(false);

		blockNameEditor = new I18nTextEditor(this, languageChooser, "Block name:");
		
		new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		checkComp = new LabeledCheckboxComposite(this, style, true);
		checkComp.setLayoutData((new GridData(GridData.HORIZONTAL_ALIGN_CENTER)));
		checkComp.getLabel().setText("Is unique?");
	}

	public void setStructBlock(StructBlock psb) {
		block = psb;
		blockNameEditor.setI18nText(psb.getName(), EditMode.DIRECT);
		checkComp.getCheckbox().setSelection(block.isUnique());
		this.setVisible(true);
	}

	public I18nTextEditor getBlockNameEditor() {
		return blockNameEditor;
	}
}
