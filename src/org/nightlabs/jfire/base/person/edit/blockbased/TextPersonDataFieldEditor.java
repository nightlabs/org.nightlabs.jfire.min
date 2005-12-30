/*
 * Created 	on Nov 26, 2004
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.edit.blockbased;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.nightlabs.jfire.base.person.edit.AbstractPersonDataFieldEditor;
import org.nightlabs.jfire.base.person.edit.AbstractPersonDataFieldEditorFactory;
import org.nightlabs.jfire.person.AbstractPersonDataField;
import org.nightlabs.jfire.person.TextPersonDataField;

/**
 * Represents an editor for {@link TextPersonDataField} within a
 * block based ExpandableBlocksPersonEditor.
 *  
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class TextPersonDataFieldEditor extends AbstractPersonDataFieldEditor {
	private static Logger LOGGER = Logger.getLogger(TextPersonDataFieldEditor.class);
	
	public static class Factory extends AbstractPersonDataFieldEditorFactory {
		/**
		 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditorFactory#getTargetPersonDataFieldType()
		 */
		public Class getTargetPersonDataFieldType() {
			return TextPersonDataField.class;
		}
		/**
		 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditorFactory#getEditorType()
		 */
		public String getEditorType() {
			return ExpandableBlocksPersonEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE;
		}
		/**
		 * @see org.nightlabs.jfire.base.person.edit.AbstractPersonDataFieldEditorFactory#getPersonDataFieldEditorClass()
		 */
		public Class getPersonDataFieldEditorClass() {
			return TextPersonDataFieldEditor.class;
		}
	}
	
	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#getTargetPersonDataType()
	 */
	public Class getTargetPersonDataType() {
		return TextPersonDataField.class;
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#getEditorType()
	 */
	public String getEditorType() {
		return ExpandableBlocksPersonEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE;
	}

	
	private TextPersonDataFieldComposite composite;
	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {
		if (composite == null) {
			composite = new TextPersonDataFieldComposite(this,parent,SWT.NONE,this);
		}
		composite.refresh();
		return composite;
	}
	
	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#getControl()
	 */
	public Control getControl() {
		return composite;
	}

	private TextPersonDataField data;
	/**
	 * @see org.nightlabs.jfire.base.person.edit.AbstractPersonDataFieldEditor#setData(org.nightlabs.jfire.base.person.AbstractPersonDataField)
	 */
	public void doSetData(AbstractPersonDataField data) {
		if (! (data instanceof TextPersonDataField))
			throw new IllegalArgumentException("Argument data should be of type "+TextPersonDataField.class.getName()+" but was "+data.getClass().getName());
		this.data = (TextPersonDataField)data;
		setChanged(false);
		// TODO: refesh called twice ??
//		if (composite != null)
//			composite.refresh();
	}
	
	public TextPersonDataField getData() {
		return data;
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#updatePerson()
	 */
	public void updatePerson() {
		data.setText(composite.getText());
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.AbstractPersonDataFieldEditor#doRefresh(org.nightlabs.jfire.base.person.AbstractPersonDataField)
	 */
	public void doRefresh(AbstractPersonDataField data) {
		setData(data);
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#disposeControl()
	 */
	public void disposeControl() {
		composite.dispose();
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#refresh()
	 */
	public void doRefresh() {
		if (composite != null)
			composite.refresh();
	}

}
