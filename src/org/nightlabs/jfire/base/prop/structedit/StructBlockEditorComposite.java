package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.composite.LabeledCheckboxComposite;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.language.I18nTextEditor;
import org.nightlabs.base.language.LanguageChooser;
import org.nightlabs.base.language.I18nTextEditor.EditMode;
import org.nightlabs.jfire.prop.StructBlock;

public class StructBlockEditorComposite extends XComposite
{
	private I18nTextEditor blockNameEditor;
	LabeledCheckboxComposite checkComp;
	private StructBlock block;	
	
	public StructBlockEditorComposite(Composite parent, LanguageChooser langChooser, int style, final StructEditor structEditor)
	{
		super(parent, style);
		
		this.setVisible(false);		
		this.setLayout(new GridLayout(1, false));
		
		Composite comp = new XComposite(this, style);
		
		blockNameEditor = new I18nTextEditor(comp, langChooser, "Block name");
		blockNameEditor.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				structEditor.setChanged(true);
			}			
		});
		checkComp = new LabeledCheckboxComposite(comp, style, true);
		checkComp.getLabel().setText("Is unique?");				
	}
	
	public void setCurrentStructBlock(StructBlock psb)
	{
		block = psb;
		blockNameEditor.setI18nText(psb.getName(), EditMode.DIRECT);				
		checkComp.getCheckbox().setSelection(block.isUnique());		
		this.setVisible(true);		
	}
	
	public I18nTextEditor getBlockNameEditor()
	{
		return blockNameEditor;
	}
}
