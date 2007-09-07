/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.prop.edit.blockbased;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditorChangeListener;
import org.nightlabs.jfire.prop.AbstractDataField;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructField;

/**
 * A Composite presenting all fields a propertySet has within a DataBlock to
 * the user for editing.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class AbstractDataBlockEditor extends Composite implements DataFieldEditorChangeListener {
	
	private IStruct struct;
	
	protected AbstractDataBlockEditor(IStruct struct, DataBlock dataBlock, Composite parent, int style) {
		super(parent,style);
		this.dataBlock = dataBlock;
		this.struct = struct;
	}
	
	public abstract void refresh(IStruct struct, DataBlock block);

	protected DataBlock dataBlock;
	
	/**
	 * key: String AbstractDataField.getPropRelativePK<br/>
	 * value: DataFieldEditor fieldEditor
	 */
	private Map<String, DataFieldEditor<? extends AbstractDataField>> fieldEditors = new HashMap<String, DataFieldEditor<? extends AbstractDataField>>();
	
	
	protected void addFieldEditor(AbstractDataField dataField, DataFieldEditor<? extends AbstractDataField> fieldEditor) {
		addFieldEditor(dataField, fieldEditor, true);
	}
	
	protected void addFieldEditor(AbstractDataField dataField, DataFieldEditor<? extends AbstractDataField> fieldEditor, boolean addListener) {		
		fieldEditors.put(dataField.getPropRelativePK(),fieldEditor);
		fieldEditor.addDataFieldEditorChangedListener(this);
	}
	
	protected DataFieldEditor<? extends AbstractDataField> getFieldEditor(AbstractDataField dataField) {
		return (DataFieldEditor<? extends AbstractDataField>)fieldEditors.get(dataField.getPropRelativePK());
	}
	
	protected boolean hasFieldEditorFor(AbstractDataField dataField) {
		return fieldEditors.containsKey(dataField.getPropRelativePK());
	}
	
	private Collection<DataBlockEditorChangedListener> changeListener = new LinkedList<DataBlockEditorChangedListener>();	
	public synchronized void addPropDataBlockEditorChangedListener(DataBlockEditorChangedListener listener) {
		changeListener.add(listener);
	}
	public synchronized void removePropDataBlockEditorChangedListener(DataBlockEditorChangedListener listener) {
		changeListener.add(listener);
	}
	protected synchronized void notifyChangeListeners(DataFieldEditor<? extends AbstractDataField> dataFieldEditor) {
		for (DataBlockEditorChangedListener listener : new ArrayList<DataBlockEditorChangedListener>(changeListener)) {
			listener.propDataBlockEditorChanged(this,dataFieldEditor);
		}
	}

	public Map<String, Integer> getStructFieldDisplayOrder() {
		// TODO re-enable this
		//return AbstractPropStructOrderConfigModule.sharedInstance().structFieldDisplayOrder();
		List<StructField> fields = struct.getStructBlock(dataBlock.getDataBlockGroup()).getStructFields();
		Map<String, Integer> fieldOrdering = new HashMap<String, Integer>(fields.size());
		int index = 0;
		for (StructField field : fields) {
			fieldOrdering.put(field.getPrimaryKey(), index);
			index++;
		}
		
		return fieldOrdering;
	}
	
	/**
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditorChangeListener#dataFieldEditorChanged(org.nightlabs.jfire.base.admin.widgets.prop.edit.AbstractPropDataFieldEditor)
	 */
	public void dataFieldEditorChanged(DataFieldEditor<? extends AbstractDataField> editor) {
		notifyChangeListeners(editor);
	}
	
	@SuppressWarnings("unchecked")
	public Iterator<AbstractDataField> getOrderedPropDataFieldsIterator() {
		List<AbstractDataField> result = new LinkedList<AbstractDataField>();
		Map<String, Integer> structFieldOrder = getStructFieldDisplayOrder();
		for (Iterator<AbstractDataField> it = dataBlock.getDataFields().iterator(); it.hasNext(); ) {
			AbstractDataField dataField = (AbstractDataField)it.next();
			if (structFieldOrder.containsKey(dataField.getStructFieldPK())) {
				Integer index = structFieldOrder.get(dataField.getStructFieldPK());
				dataField.setPriority(index.intValue());
			}
			result.add(dataField);
		}
		Collections.sort(result);
		return result.iterator();
	}

	protected IStruct getStruct() {
		return struct;
	}	
	
	protected void setStruct(IStruct struct) {
		this.struct = struct;
	}
	
	public void dispose() {
		for (DataFieldEditor<? extends AbstractDataField> editor : fieldEditors.values()) {
			editor.removeDataFieldEditorChangedListener(this);
		}
		fieldEditors.clear();
		super.dispose();
	}
	
	/**
	 * Default implementation of updateProp() iterates through all
	 * DataFieldEditor s added by {@link #addFieldEditor(AbstractDataField, DataFieldEditor)}
	 * and calls their updateProp method.<br/>
	 * Implementors might override if no registered PropDataFieldEditors are used.
	 */
	public void updatePropertySet() {
		for (DataFieldEditor<? extends AbstractDataField> editor : fieldEditors.values()) {
			editor.updatePropertySet();
		}
	}	
}