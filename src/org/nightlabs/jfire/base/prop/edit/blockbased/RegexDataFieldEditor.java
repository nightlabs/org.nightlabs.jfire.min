/**
 * 
 */
package org.nightlabs.jfire.base.prop.edit.blockbased;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.base.util.RCPUtil;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.prop.datafield.RegexDataField;
import org.nightlabs.jfire.prop.structfield.RegexStructField;
import org.nightlabs.language.LanguageCf;

/**
 * @author Tobias Langner <!-- tobias[DOT]langner[AT]nightlabs[DOT]de -->
 */
public class RegexDataFieldEditor extends AbstractDataFieldEditor<RegexDataField> {
	
	public static class Factory extends AbstractDataFieldEditorFactory<RegexDataField> {

		@Override
		public String getEditorType() {
			return ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE;
		}

		@Override
		public Class<? extends DataFieldEditor<RegexDataField>> getDataFieldEditorClass() {
			return RegexDataFieldEditor.class;
		}

		@Override
		public Class<RegexDataField> getPropDataFieldType() {
			return RegexDataField.class;
		}
	}
	
	private static Logger LOGGER = Logger.getLogger(RegexDataFieldEditor.class);
	
	private LanguageCf language;
	private XComposite comp;
	private Label title;
	private Text valueText;
	private boolean modified = false;
	private boolean ignoreModify = false;
	
	private RegexDataField regexDataField;
	private RegexStructField regexStructField;
	
	public RegexDataFieldEditor() {
		super();
		language = new LanguageCf(Locale.getDefault().getLanguage());
	}
	
	@Override
	protected void setDataField(RegexDataField dataField) {
		super.setDataField(dataField);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createControl(Composite parent) {
		comp = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
		comp.getGridLayout().horizontalSpacing = 0;
// TODO: this is a quickfix for the Formtoolkit Boarderpainter, which paints to the 
// 	outside of the elements -> there needs to be space in the enclosing composite for the borders
		comp.getGridLayout().verticalSpacing = 2;
		comp.getGridLayout().marginHeight = 2;
		comp.getGridLayout().marginWidth = 2;
		title = new Label(comp, SWT.NONE);
		valueText = new Text(comp, comp.getBorderStyle());
		valueText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!ignoreModify)
					modified = true;
			}
		});
		
		valueText.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {}
			public void focusLost(FocusEvent e) {
				if (regexStructField != null) {
					String text = valueText.getText();
					if (!regexStructField.validateValue(text)) {
						MessageBox box = new MessageBox(RCPUtil.getActiveWorkbenchShell(), SWT.OK);
						box.setMessage("The given input does not match the regular expression assigned to the field.\n\n" +
								"The regular expression can be seen in the tool tip of the text box.");
						box.setText("Validation error");
						box.open();
					} else if (modified) {
						setChanged(true);
					}
				}
			}
		});
		
		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA_HORIZONTAL, valueText);
		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA_HORIZONTAL, title);
		return comp;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor#doRefresh()
	 */
	@Override
	public void doRefresh() {
		regexDataField = getDataField();
		regexStructField = (RegexStructField) getStructField();
		title.setText(regexStructField.getName().getText(language.getLanguageID()));
		valueText.setToolTipText("Regex: " + regexStructField.getRegex());
		ignoreModify = true;
		if (!regexDataField.isEmpty())
			valueText.setText(regexDataField.getText());
		ignoreModify = false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#getControl()
	 */
	public Control getControl() {
		return comp;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#updateProp()
	 */
	public void updateProperty() {
		if (!isChanged())
			return;
		
		String text = valueText.getText();
		if (regexStructField.validateValue(text)) {
				regexDataField.setText(text);
		}
		
		modified = false;
	}
	
	public LanguageCf getLanguage() {
		return language;
	}
}


