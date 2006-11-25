package org.nightlabs.jfire.base.prop.structedit;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.language.I18nTextEditor;
import org.nightlabs.base.language.LanguageChooser;
import org.nightlabs.base.language.I18nTextEditor.EditMode;
import org.nightlabs.jfire.prop.AbstractStructField;
import org.nightlabs.jfire.prop.exception.PropertyException;

public class StructFieldEditorComposite extends XComposite
{
	private I18nTextEditor fieldNameEditor;
	private Label fieldType;
	private List<StructFieldMetaData> fieldMetadata;
	private IStructFieldEditor fieldEditor;
	private Composite commonEditComposite;
	private Composite specialsEditComposite;
	private LanguageChooser langChooser;
	private StructEditor structEditor;
	
	public StructFieldEditorComposite(Composite parent, LanguageChooser langChooser, int style, final StructEditor structEditor)
	{		
		super(parent, style, LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
		
		this.structEditor = structEditor;
		
		commonEditComposite = new XComposite(this, style, LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
		commonEditComposite.setLayout(new GridLayout(1, false));
		commonEditComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		this.langChooser = langChooser;
		fieldType = new Label(commonEditComposite, SWT.BOLD);
		
		fieldNameEditor = new I18nTextEditor(commonEditComposite, langChooser, "Field name:");
		fieldNameEditor.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fieldNameEditor.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				structEditor.setChanged(true);
			}
		});
		
		new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
		
	/**
	 * Set the field that is currently display in the editor. Can be null if no field is to be displayed.
	 * @param field
	 */
	public void setCurrentField(AbstractStructField field)
	{
		if (field == null)
		{
			fieldNameEditor.reset();
			fieldNameEditor.setEnabled(false);
			fieldType.setText("");
			if (specialsEditComposite != null)
				specialsEditComposite.dispose();
			return;
		}
		
		fieldNameEditor.setEnabled(true);		
		fieldNameEditor.setI18nText(field.getName(), EditMode.DIRECT);
		if (specialsEditComposite != null)
			specialsEditComposite.dispose();
		
		try
		{
			StructFieldFactoryRegistry reg = StructFieldFactoryRegistry.sharedInstance();
			fieldEditor = reg.getEditorSingleton(field);
			
			fieldType.setText(reg.getFieldMetaDataMap().get(field.getClass().getName()).getFieldName());

			specialsEditComposite = fieldEditor.createComposite(this, this.getStyle(), langChooser, structEditor);
			XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA, specialsEditComposite);
			fieldEditor.setData(field);			
		}
		catch (PropertyException pe)
		{
			throw new RuntimeException(pe.getMessage(), pe);
		}		
	}
	
	public I18nTextEditor getFieldNameEditor()
	{
		return fieldNameEditor;
	}	
}
