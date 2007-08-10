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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
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
import org.nightlabs.jfire.base.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditorFactoryRegistry;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditorNotFoundException;
import org.nightlabs.jfire.base.prop.edit.PropertySetEditor;
import org.nightlabs.jfire.base.prop.edit.blockbased.DataBlockEditorChangedListener;
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
	
	public static final String EDITORTYPE_FIELD_BASED = "field-based";

	/**
	 * 
	 */
	public FieldBasedEditor() {
		super();
	}
	
	
	private String editorType;
	/**
	 * Get the editorType.
	 * @return
	 */
	public String getEditorType() {
		return editorType;
	}
	/**
	 * Set the editorType.
	 * Use the static finals. 
	 * @param editorType
	 */
	public void setEditorType(String editorType) {
		this.editorType = editorType;
	}

	protected Object selectionObject;
	private PropertySet propertySet;
	
	/**
	 * @see org.nightlabs.jfire.base.prop.edit.PropertySetEditor#setProp(org.nightlabs.jfire.base.prop.Property)
	 */
	public void setPropertySet(PropertySet propSet) {
		this.propertySet = propSet;
		this.selectionObject = propSet;
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
	
	private Label propTitleLabel;
	
	public Label getPropTitleLabel() {
		return propTitleLabel;
	}
	

	private SelectableComposite editorWrapper;
	private Color wrapperSelectedColor = new Color(Display.getDefault(), 155, 155, 155);
	private Color wrapperNormalColor;
	
	private SelectableComposite editorComposite;
	private SelectableComposite titleComposite;
	
	private boolean selectionCallback = false;
	
	/**
	 * Delegates selections to the underlying Composite
	 */
	private SelectableCompositeListener editorCompositeListener = new SelectableCompositeListener() {
		public void selectionStateChanged(CompositeSelectionEvent evt) {
			if (evt.getSource() == editorComposite) {
				selectionCallback = true;
				try {
					editorWrapper.setSelected(evt.isSelected());
				}
				finally {
					selectionCallback = false;
				}
			}
		}
	};
	
	private MouseListener titleMouseAdapter = new MouseAdapter() {
		public void mouseUp(MouseEvent evt) {
				selectionCallback = true;
				if (evt.button != 1)
					return;
				try {
					editorWrapper.setSelected(true,evt.stateMask);
				}
				finally {
					selectionCallback = false;
				}
		}
	};
	
	/**
	 * Changes the background of the editor Wrapper for a dynamic Border
	 */
	private SelectableCompositeListener editorWrapperListener = new SelectableCompositeListener() {		
		public void selectionStateChanged(CompositeSelectionEvent evt) {
			if (!selectionCallback)
				editorComposite.setSelected(evt.isSelected());
			if (evt.isSelected())
				evt.getSource().setBackground(wrapperSelectedColor);
			else
				evt.getSource().setBackground(wrapperNormalColor);
		}
	};
	
	
	private GridLayout gridLayout;
	
	public GridLayout getGridLayout() {
		return gridLayout;
	}
	
	/**
	 * Creates a new GridLayout wich will be applied to the Editor.
	 * Intended to be overridden to apply a custom layout (more columns etc.)
	 * @return
	 */
	protected GridLayout createGridLayout() {
		GridLayout wrapperLayout = new GridLayout();
		wrapperLayout.marginHeight = 1;
		wrapperLayout.marginWidth = 1;
		wrapperLayout.verticalSpacing = 0;
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
			editor.getControl().dispose();
			iter.remove();
		}
		if (titleComposite != null) {
			titleComposite.dispose();
			titleComposite = null;
			propTitleLabel = null;
		}
		if (editorComposite != null) {
			editorComposite.removeSelectionChangeListener(editorCompositeListener);
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
			
			editorWrapper = new SelectableComposite(parent,SWT.NONE);
			editorWrapper.setSelectionObject(selectionObject);
			wrapperNormalColor = editorWrapper.getBackground();
			
			if (setLayoutDataForWrapper()) {
				wrapperGridData = new GridData();
				wrapperGridData.horizontalAlignment = GridData.FILL;
				wrapperGridData.grabExcessHorizontalSpace = true;
				wrapperGridData.verticalAlignment = GridData.BEGINNING;
				
				editorWrapper.setLayoutData(wrapperGridData);
			}
			
			gridLayout = createGridLayout();
			
			if (gridLayout == null)
				throw new IllegalStateException("createGridLayout() returned null!!");
			
			editorWrapper.setLayout(gridLayout);			
			editorWrapper.addSelectionChangeListener(editorWrapperListener);
			
			titleComposite = new SelectableComposite(editorWrapper,SWT.NONE);
			titleComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			GridLayout titleLayout = new GridLayout();
			titleLayout.verticalSpacing = 0;
			titleLayout.marginHeight = 0;
			titleLayout.marginWidth = 0;
			titleLayout.horizontalSpacing = 0;
			titleComposite.setLayout(titleLayout);
			
			editorComposite = new SelectableComposite(editorWrapper,SWT.NONE);
			editorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			GridLayout layout = new GridLayout();
//			layout.horizontalSpacing = 0;
			layout.verticalSpacing = 0;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.horizontalSpacing = 3;
//			layout.marginWidth = 0;
			layout.numColumns = 3;
			editorComposite.setLayout(layout);
			editorComposite.addSelectionChangeListener(editorCompositeListener);
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
	private Map fieldEditors = new HashMap();
	
	
	private void createTitleLabel() {
		if (propTitleLabel == null) {
			if (titleComposite == null)
				return;
			propTitleLabel = new Label(titleComposite,SWT.NONE);
			GridData gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			propTitleLabel.setLayoutData(gd);
			propTitleLabel.addMouseListener(titleMouseAdapter);
		}
	}
	
	/**
	 * @see org.nightlabs.jfire.base.prop.edit.PropertySetEditor#refreshControl()
	 */
	public void refreshControl() {
		Display.getDefault().syncExec( 
			new Runnable() {
				public void run() {
					if (propertySet == null)
						return;
					
					createTitleLabel();
					
					if (propTitleLabel != null) {
						if (propertySet.getDisplayName() != null)
							propTitleLabel.setText(propertySet.getDisplayName());
						else 
							propTitleLabel.setText("");
						propTitleLabel.setBackground(new Color(Display.getDefault(), 155, 155, 155));
					}
					
					if (!propertySet.isExploded())
						getPropStructure(new NullProgressMonitor()).explodePropertySet(propertySet);
					
					for (Iterator iter = EditorStructFieldRegistry.sharedInstance().getStructFieldList(getEditorType()).iterator(); iter.hasNext();) {
						StructFieldID structFieldID = (StructFieldID) iter.next();
						AbstractDataField field = null;
						try {
							field = propertySet.getDataField(structFieldID);
						} catch (DataNotFoundException e) {
							LOGGER.error("Could not find PropDataField for "+structFieldID,e);
							continue;
						}
						if (field.isEmpty()) {
							if (!showEmptyFields) {
								if (fieldEditors.containsKey(structFieldID)) {
									((DataFieldEditor)fieldEditors.get(structFieldID)).getControl().dispose();
									fieldEditors.remove(structFieldID);
								}
								continue;
							}
						}
						DataFieldEditor editor = null;
						if (!fieldEditors.containsKey(structFieldID)) {
							try {
								editor = DataFieldEditorFactoryRegistry.sharedInstance().getNewEditorInstance(
										propertySet.getStructure(), getEditorType(), null, 
										field, false
									);
							} catch (DataFieldEditorNotFoundException e) {
								LOGGER.error("Could not find DataFieldEditor for "+field.getClass().getName(),e);
								continue;
							}
							editor.setData(propertySet.getStructure(), field);
							Control editorControl = editor.createControl(editorComposite);
							GridData editorGD = FieldBasedEditor.this.getGridDataForField(field);
							if (editorGD != null)
								editorControl.setLayoutData(editorGD);
							fieldEditors.put(structFieldID,editor);
						} else {
							editor = (DataFieldEditor)fieldEditors.get(structFieldID);
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
		monitor.beginTask("Loading propertySet structure", 1);
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
