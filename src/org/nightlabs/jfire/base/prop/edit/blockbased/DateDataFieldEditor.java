/**
 * 
 */
package org.nightlabs.jfire.base.prop.edit.blockbased;

import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.composite.DateTimeEdit;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.prop.datafield.DateDataField;
import org.nightlabs.jfire.prop.structfield.DateStructField;
import org.nightlabs.language.LanguageCf;

/**
 * @author Tobias Langner <!-- tobias[DOT]langner[AT]nightlabs[DOT]de -->
 */
public class DateDataFieldEditor extends AbstractDataFieldEditor<DateDataField> {
	
	public static class Factory extends AbstractDataFieldEditorFactory<DateDataField> {

		@Override
		public String getEditorType() {
			return ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE;
		}

		@Override
		public Class<? extends DataFieldEditor<DateDataField>> getDataFieldEditorClass() {
			return DateDataFieldEditor.class;
		}

		@Override
		public Class<DateDataField> getPropDataFieldType() {
			return DateDataField.class;
		}
	}
	
	private static Logger LOGGER = Logger.getLogger(DateDataFieldEditor.class);
	
	private LanguageCf language;
	private XComposite comp;
	private Label title;
	
	private DateTimeEdit dateTimeEdit;
	
	public DateDataFieldEditor() {
		super();
		language = new LanguageCf(Locale.getDefault().getLanguage());
	}
	
	@Override
	protected void setDataField(DateDataField dataField) {
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
		comp.getGridLayout().verticalSpacing = 0;
		title = new Label(comp, SWT.NONE);		
		return comp;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor#doRefresh()
	 */
	@Override
	public void doRefresh() {
		DateStructField dateStructField = (DateStructField) getStructField();
		if (dateTimeEdit != null)
			dateTimeEdit.dispose();
		
		title.setText(dateStructField.getName().getText(language.getLanguageID()));		
		dateTimeEdit = new DateTimeEdit(comp, dateStructField.getDateTimeEditFlags());
		dateTimeEdit.getGridData().grabExcessHorizontalSpace = true;
		dateTimeEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setChanged(true);
			}
		});
		dateTimeEdit.getParent().layout();
		if (getDataField().getDate() == null)
			dateTimeEdit.setDate(new Date());
		else
			dateTimeEdit.setDate(getDataField().getDate());
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
		
		getDataField().setDate(dateTimeEdit.getDate());
	}
	
	public LanguageCf getLanguage() {
		return language;
	}
}


