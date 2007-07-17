/**
 * 
 */
package org.nightlabs.jfire.base.prop.edit.blockbased;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.prop.datafield.NumberDataField;
import org.nightlabs.jfire.prop.structfield.NumberStructField;
import org.nightlabs.language.LanguageCf;

import sun.security.action.GetBooleanAction;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class NumberDataFieldEditor extends AbstractDataFieldEditor<NumberDataField> {
	
	public static class Factory extends AbstractDataFieldEditorFactory<NumberDataField> {

		@Override
		public String getEditorType() {
			return ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE;
		}

		@Override
		public Class<? extends DataFieldEditor<NumberDataField>> getDataFieldEditorClass() {
			return NumberDataFieldEditor.class;
		}

		@Override
		public Class<NumberDataField> getPropDataFieldType() {
			return NumberDataField.class;
		}
	}
	
	private static Logger LOGGER = Logger.getLogger(NumberDataFieldEditor.class);
	
	private LanguageCf language;
	private XComposite comp;
	private Label title;
	private Spinner valueSpinner;
	
	public NumberDataFieldEditor() {
		super();
		language = new LanguageCf(Locale.getDefault().getLanguage());
	}
	
	@Override
	protected void setDataField(NumberDataField dataField) {
		super.setDataField(dataField);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createControl(Composite parent) {
		comp = new XComposite(parent, SWT.NONE, LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);

		title = new Label(comp, SWT.NONE);		
		valueSpinner = new Spinner(comp, comp.getBorderStyle());
		valueSpinner.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setChanged(true);
			}
		});
		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA_HORIZONTAL, valueSpinner);
		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA_HORIZONTAL, title);
		return comp;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor#doRefresh()
	 */
	@Override
	public void doRefresh() {
		NumberDataField numberDataField = getDataField();
		NumberStructField numberStructField = (NumberStructField) getStructField();
		title.setText(numberStructField.getName().getText(language.getLanguageID()));
		
		if (numberStructField.isBounded()) {
			valueSpinner.setMaximum(numberStructField.getSpinnerMax());
			valueSpinner.setMinimum(numberStructField.getSpinnerMin());
		} else {
			valueSpinner.setMaximum(Integer.MAX_VALUE);
			valueSpinner.setMinimum(0);
		}
		valueSpinner.setDigits(numberStructField.getDigits());
		
		valueSpinner.setSelection(numberDataField.getIntValue());		
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
		
		getDataField().setValue(valueSpinner.getSelection());
	}
	
	public LanguageCf getLanguage() {
		return language;
	}
}


