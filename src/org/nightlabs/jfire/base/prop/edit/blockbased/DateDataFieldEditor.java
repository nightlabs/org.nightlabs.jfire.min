/**
 * 
 */
package org.nightlabs.jfire.base.prop.edit.blockbased;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.composite.DateTimeEdit;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.prop.edit.AbstractInlineDataFieldComposite;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.prop.edit.fieldbased.FieldBasedEditor;
import org.nightlabs.jfire.prop.datafield.DateDataField;
import org.nightlabs.jfire.prop.structfield.DateStructField;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class DateDataFieldEditor extends AbstractDataFieldEditor<DateDataField> {
	
	public static class Factory extends AbstractDataFieldEditorFactory<DateDataField> {

		@Override
		public String[] getEditorTypes() {
			return new String[] {ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE, FieldBasedEditor.EDITORTYPE_FIELD_BASED};
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
	
	
	private DateDataFieldComposite comp;
	
	public DateDataFieldEditor() {
		super();
	}
	
//	@Override
//	protected void setDataField(DateDataField dataField) {
//		super.setDataField(dataField);
//	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createControl(Composite parent) {
		if (comp == null)
			comp = new DateDataFieldComposite(parent, this);
		
		return comp;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor#doRefresh()
	 */
	@Override
	public void doRefresh() {
		if (comp != null)
			comp.refresh();
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
	public void updatePropertySet() {
		if (!isChanged())
			return;
		
		getDataField().setDate(comp.getDate());
	}
}

class DateDataFieldComposite extends AbstractInlineDataFieldComposite<DateDataFieldEditor, DateDataField> {

	private DateTimeEdit dateTimeEdit;
	
	public DateDataFieldComposite(Composite parent, DateDataFieldEditor editor) {
		super(parent, SWT.NONE, editor);
	}

	@Override
	public void _refresh() {
		DateStructField dateStructField = (DateStructField) getEditor().getStructField();
		if (dateTimeEdit != null)
			dateTimeEdit.dispose();
		
		dateTimeEdit = new DateTimeEdit(this, dateStructField.getDateTimeEditFlags(), (Date) null);
		XComposite.configureLayout(LayoutMode.TIGHT_WRAPPER, dateTimeEdit.getGridLayout());
		dateTimeEdit.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		dateTimeEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getEditor().setChanged(true);
			}
		});
		dateTimeEdit.getParent().layout();
		dateTimeEdit.setDate(getEditor().getDataField().getDate());
	}
	
	public Date getDate() {
		return dateTimeEdit.getDate();
	}
}


