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

package org.nightlabs.jfire.base.person.edit.fieldbased;

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
import org.nightlabs.jfire.base.person.PersonStructProvider;
import org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor;
import org.nightlabs.jfire.base.person.edit.PersonDataFieldEditorFactoryRegistry;
import org.nightlabs.jfire.base.person.edit.PersonDataFieldEditorNotFoundException;
import org.nightlabs.jfire.base.person.edit.PersonEditor;
import org.nightlabs.jfire.base.person.edit.blockbased.PersonDataBlockEditorChangedListener;
import org.nightlabs.jfire.person.AbstractPersonDataField;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonDataNotFoundException;
import org.nightlabs.jfire.person.id.PersonStructFieldID;

/**
 * A field based PersonEditor that will set its look depending
 * on the editorType and the PersonDataFieldEditors registered
 * by the personDataField-extension-point.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class FieldBasedPersonEditor implements PersonEditor {

	public static final Logger LOGGER = Logger.getLogger(FieldBasedPersonEditor.class);
	
	public static final String EDITORTYPE_FIELD_BASED = "field-based";

	/**
	 * 
	 */
	public FieldBasedPersonEditor() {
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
	private Person person;
	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonEditor#setPerson(org.nightlabs.jfire.base.person.Person)
	 */
	public void setPerson(Person person) {
		this.person = person;
		this.selectionObject = person;
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonEditor#setPerson(org.nightlabs.jfire.base.person.Person, boolean)
	 */
	public void setPerson(Person person, boolean refresh) {
		setPerson(person);
		if (refresh)
			refreshControl();
	}
	
	private boolean showEmptyFields = true;
	/**
	 * 
	 * @return Wheather empty fields of the associated person should be displayed.
	 */
	public boolean isShowEmptyFields() {
		return showEmptyFields;
	}
	/**
	 * Defines weather empty fields of the associated person should be displayed.
	 * @param showEmptyFields
	 */
	public void setShowEmptyFields(boolean showEmptyFields) {
		this.showEmptyFields = showEmptyFields;
	}
	
	private Label personTitleLabel;
	
	public Label getPersonTitleLabel() {
		return personTitleLabel;
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
	protected GridData getGridDataForField(AbstractPersonDataField field) {
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
			PersonDataFieldEditor editor = (PersonDataFieldEditor) iter.next();
			editor.getControl().dispose();
			iter.remove();
		}
		if (titleComposite != null) {
			titleComposite.dispose();
			titleComposite = null;
			personTitleLabel = null;
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
	 * @see org.nightlabs.jfire.base.person.edit.PersonEditor#createControl(org.eclipse.swt.widgets.Composite, boolean)
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
	public SelectableComposite getComposite(Composite parent, PersonDataBlockEditorChangedListener changeListener, boolean refresh) {
		return (SelectableComposite)createControl(parent,refresh);
	}
	
	/**
	 * Map holding all fieldEditors.<br/>
	 * key: PersonStructFieldID structFieldID<br/>
	 * value: PersonDataFieldEditor fieldEditor
	 */
	private Map fieldEditors = new HashMap();
	
	
	private void createTitleLabel() {
		if (personTitleLabel == null) {
			personTitleLabel = new Label(titleComposite,SWT.NONE);
			GridData gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			personTitleLabel.setLayoutData(gd);
			personTitleLabel.addMouseListener(titleMouseAdapter);
		}
	}
	
	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonEditor#refreshControl()
	 */
	public void refreshControl() {
		Display.getDefault().syncExec( 
			new Runnable() {
				public void run() {
					if (person == null)
						return;
					
					createTitleLabel();
					
					if (person.getPersonDisplayName() != null)
						personTitleLabel.setText(person.getPersonDisplayName());
					else 
						personTitleLabel.setText("");
					
					personTitleLabel.setBackground(new Color(Display.getDefault(), 155, 155, 155));
					
					PersonStructProvider.getPersonStructure().explodePerson(person);
					
					for (Iterator iter = PersonEditorStructFieldRegistry.sharedInstance().getStructFieldList(getEditorType()).iterator(); iter.hasNext();) {
						PersonStructFieldID structFieldID = (PersonStructFieldID) iter.next();
						AbstractPersonDataField field = null;
						try {
							field = person.getPersonDataField(structFieldID);
						} catch (PersonDataNotFoundException e) {
							LOGGER.error("Could not find PersonDataField for "+structFieldID,e);
							continue;
						}
						if (field.isEmpty()) {
							if (!showEmptyFields) {
								if (fieldEditors.containsKey(structFieldID)) {
									((PersonDataFieldEditor)fieldEditors.get(structFieldID)).getControl().dispose();
									fieldEditors.remove(structFieldID);
								}
								continue;
							}
						}
						PersonDataFieldEditor editor = null;
						if (!fieldEditors.containsKey(structFieldID)) {
							try {
								editor = PersonDataFieldEditorFactoryRegistry.sharedInstance().getNewEditorInstance(field, getEditorType(), false);
							} catch (PersonDataFieldEditorNotFoundException e) {
								LOGGER.error("Could not find PersonDataFieldEditor for "+field.getClass().getName(),e);
								continue;
							}
							editor.setData(field);
							Control editorControl = editor.createControl(editorComposite);
							GridData editorGD = FieldBasedPersonEditor.this.getGridDataForField(field);
							if (editorGD != null)
								editorControl.setLayoutData(editorGD);
							fieldEditors.put(structFieldID,editor);
						} else {
							editor = (PersonDataFieldEditor)fieldEditors.get(structFieldID);
						}
						editor.setData(field);
						editor.refresh();
					}
//					editorWrapper.setSize(editorComposite.computeSize(SWT.DEFAULT,fieldEditors.size()*35+35));
					editorWrapper.pack();
				}
			}
		);
	}
	
	

	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonEditor#updatePerson()
	 */
	public void updatePerson() {
		for (Iterator iter = fieldEditors.values().iterator(); iter.hasNext();) {
			PersonDataFieldEditor editor = (PersonDataFieldEditor) iter.next();
			editor.updatePerson();
		}
	}
}
