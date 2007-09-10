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

package org.nightlabs.jfire.base.prop.edit.fieldbased;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.composite.CompositeSelectionEvent;
import org.nightlabs.base.composite.SelectableComposite;
import org.nightlabs.base.composite.SelectableCompositeListener;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditorFactoryRegistry;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditorNotFoundException;
import org.nightlabs.jfire.base.prop.edit.PropertySetEditor;
import org.nightlabs.jfire.base.prop.edit.blockbased.DataBlockEditorChangedListener;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.prop.AbstractDataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.exception.DataNotFoundException;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.progress.ProgressMonitor;

/**
 * A field based PropertySetEditor that will set its look depending
 * on the editorType and the PropDataFieldEditors registered
 * by the propDataField-extension-point.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class FieldBasedEditor implements PropertySetEditor {

	public static final Logger LOGGER = Logger.getLogger(FieldBasedEditor.class);
	
	public static final String EDITORTYPE_FIELD_BASED = "field-based"; //$NON-NLS-1$

	/**
	 * 
	 */
	public FieldBasedEditor() {
		super();
	}
	
	
	private String editorType;
	/**
	 * Get the editorType.
	 * @return The editorType.
	 */
	public String getEditorType() {
		return editorType;
	}
	/**
	 * Set the editorType.
	 * Use the static finals. 
	 * @param editorType The editorType to set.
	 */
	public void setEditorType(String editorType) {
		this.editorType = editorType;
	}

	private PropertySet propertySet;
	
	/**
	 * @see org.nightlabs.jfire.base.prop.edit.PropertySetEditor#setProp(org.nightlabs.jfire.base.prop.Property)
	 */
	public void setPropertySet(PropertySet propSet) {
		this.propertySet = propSet;
	}

	/**
	 * @see org.nightlabs.jfire.base.prop.edit.PropertySetEditor#setPropertySet(org.nightlabs.jfire.base.prop.Property)
	 */
	public void setPropertySet(PropertySet prop, boolean refresh) {
		setPropertySet(prop);
		if (refresh)
			refreshControl();
	}
	
	private boolean showEmptyFields = true;
	/**
	 * 
	 * @return Wheather empty fields of the associated propertySet should be displayed.
	 */
	public boolean isShowEmptyFields() {
		return showEmptyFields;
	}
	/**
	 * Defines weather empty fields of the associated propertySet should be displayed.
	 * @param showEmptyFields
	 */
	public void setShowEmptyFields(boolean showEmptyFields) {
		this.showEmptyFields = showEmptyFields;
	}
	
	private XComposite editorWrapper;
	private Color wrapperSelectedColor = new Color(Display.getDefault(), 155, 155, 155);
	private Color wrapperNormalColor;
	
	private XComposite editorComposite;
	
	private boolean selectionCallback = false;
	
	
	/**
	 * Creates a new GridLayout wich will be applied to the Editor.
	 * Intended to be overridden to apply a custom layout (more columns etc.)
	 * @return
	 */
	protected GridLayout createGridLayout() {
		GridLayout wrapperLayout = new GridLayout();
		XComposite.configureLayout(LayoutMode.ORDINARY_WRAPPER, wrapperLayout);
		return wrapperLayout;
	}
	
	/**
	 * Returns null. Can be overridden to return a custom GridData 
	 * for the given field, or null to use the LayoutData provided 
	 * by the field-editor itself. 
	 *  
	 * @param field
	 * @return A new GridData or null.
	 */
	protected GridData getGridDataForField(AbstractDataField field) {
		return null;
	}
	
	protected GridData wrapperGridData;
	
	/**
	 * Determines weather a LayoutData in form of
	 * a new GridData(GridData.FILL_BOTH) should 
	 * be set to the editorWrapper
	 *  
	 * @return Weather to setLayoutData on editorWrapper
	 */
	protected boolean setLayoutDataForWrapper() {
		return false;
	}
	/**
	 * 
	 * @return The editorWrapper's GridData or null;
	 */
	protected GridData getWrapperGridData() {
		return wrapperGridData;
	}
	
	public void disposeControl() {
		for (Iterator iter = fieldEditors.values().iterator(); iter.hasNext();) {
			DataFieldEditor editor = (DataFieldEditor) iter.next();
			if (editor != null) {
				if (editor.getControl() != null && !editor.getControl().isDisposed())
					editor.getControl().dispose();
				iter.remove();
			}
		}
		if (editorComposite != null) {
			editorComposite = null;
		}
		if (editorWrapper != null) {
			editorWrapper.dispose();
			editorWrapper = null;
		}
	}
	
	/**
	 * @see org.nightlabs.jfire.base.prop.edit.PropertySetEditor#createControl(org.eclipse.swt.widgets.Composite, boolean)
	 */
	public Control createControl(Composite parent, boolean refresh) {
		if (editorWrapper == null) {
			
			editorWrapper = new XComposite(parent, SWT.NONE);
			wrapperNormalColor = editorWrapper.getBackground();
			
			if (setLayoutDataForWrapper()) {
				wrapperGridData = new GridData();
				wrapperGridData.horizontalAlignment = GridData.FILL;
				wrapperGridData.grabExcessHorizontalSpace = true;
				wrapperGridData.verticalAlignment = GridData.BEGINNING;
				
				editorWrapper.setLayoutData(wrapperGridData);
			}
			
			GridLayout gridLayout = createGridLayout();
			
			if (gridLayout == null)
				throw new IllegalStateException("createGridLayout() returned null!!"); //$NON-NLS-1$
			
			editorWrapper.setLayout(gridLayout);			
			
			editorComposite = new XComposite(editorWrapper, SWT.NONE);
//			editorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
//			GridLayout layout = new GridLayout();
////			layout.horizontalSpacing = 0;
//			layout.verticalSpacing = 0;
//			layout.marginHeight = 0;
//			layout.marginWidth = 0;
//			layout.horizontalSpacing = 3;
////			layout.marginWidth = 0;
//			layout.numColumns = 2;
//			editorComposite.setLayout(layout);
		}
		if (refresh)
			refreshControl();
		
		return editorWrapper;
	}
	
	/**
	 * Calls createControl but returns as SelectableComposite.
	 *  
	 * @param parent
	 * @param changeListener
	 * @param refresh
	 * @return
	 */
	public SelectableComposite getComposite(Composite parent, DataBlockEditorChangedListener changeListener, boolean refresh) {
		return (SelectableComposite)createControl(parent,refresh);
	}
	
	/**
	 * Map holding all fieldEditors.<br/>
	 * key: StructFieldID structFieldID<br/>
	 * value: DataFieldEditor fieldEditor
	 */
	private Map<StructFieldID, DataFieldEditor<AbstractDataField>> fieldEditors = new HashMap<StructFieldID, DataFieldEditor<AbstractDataField>>();
	
	
	/**
	 * @see org.nightlabs.jfire.base.prop.edit.PropertySetEditor#refreshControl()
	 */
	public void refreshControl() {
		Display.getDefault().syncExec( 
			new Runnable() {
				public void run() {
					if (propertySet == null)
						return;
					
					if (!propertySet.isExploded())
						getPropStructure(new NullProgressMonitor()).explodePropertySet(propertySet);
					
					for (Iterator iter = EditorStructFieldRegistry.sharedInstance().getStructFieldList(getEditorType()).iterator(); iter.hasNext();) {
						StructFieldID structFieldID = (StructFieldID) iter.next();
						AbstractDataField field = null;
						try {
							field = propertySet.getDataField(structFieldID);
						} catch (DataNotFoundException e) {
							LOGGER.error("Could not find PropDataField for "+structFieldID,e); //$NON-NLS-1$
							continue;
						}
						if (field.isEmpty()) {
							if (!showEmptyFields) {
								if (fieldEditors.containsKey(structFieldID)) {
									fieldEditors.get(structFieldID).getControl().dispose();
									fieldEditors.remove(structFieldID);
								}
								continue;
							}
						}
						DataFieldEditor<AbstractDataField> editor = null;
						if (!fieldEditors.containsKey(structFieldID)) {
							try {
								editor = DataFieldEditorFactoryRegistry.sharedInstance().getNewEditorInstance(
										propertySet.getStructure(), getEditorType(), null, 
										field, false
									);
							} catch (DataFieldEditorNotFoundException e) {
								LOGGER.error("Could not find DataFieldEditor for "+field.getClass().getName(),e); //$NON-NLS-1$
								continue;
							}
							editor.setData(propertySet.getStructure(), field);
							Control editorControl = editor.createControl(editorComposite);
							GridData editorGD = FieldBasedEditor.this.getGridDataForField(field);
							if (editorGD != null)
								editorControl.setLayoutData(editorGD);
							fieldEditors.put(structFieldID, editor);
						} else {
							editor = fieldEditors.get(structFieldID);
						}
						editor.setData(propertySet.getStructure(), field);
						editor.refresh();
					}
//					editorWrapper.setSize(editorComposite.computeSize(SWT.DEFAULT,fieldEditors.size()*35+35));
					editorWrapper.pack();
				}
			}
		);
	}
	
	protected IStruct getPropStructure(ProgressMonitor monitor) {
		if (propertySet.isExploded())
			return propertySet.getStructure();
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.prop.edit.fieldbased.FieldBasedEditor.getPropStructure.monitor.taskName"), 1); //$NON-NLS-1$
		IStruct structure = StructLocalDAO.sharedInstance().getStructLocal(
				propertySet.getStructLocalLinkClass(), propertySet.getStructLocalScope(), monitor
		);
		monitor.worked(1);
		return structure;
	}
	

	/**
	 * @see org.nightlabs.jfire.base.prop.edit.PropertySetEditor#updatePropertySet()
	 */
	public void updatePropertySet() {
		for (Iterator iter = fieldEditors.values().iterator(); iter.hasNext();) {
			DataFieldEditor editor = (DataFieldEditor) iter.next();
			editor.updatePropertySet();
		}
	}
}
