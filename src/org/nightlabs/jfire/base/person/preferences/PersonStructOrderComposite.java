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

package org.nightlabs.jfire.base.person.preferences;

import java.util.Map;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.jfire.base.resource.Messages;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonStructOrderComposite extends Composite {
	private Composite titleComposite;
	private Label titleLabel;
	private Group structBlocksGroup;
	private Button fieldDownButton;
	private Button fieldUpButton;
	private Composite fieldsTableComposite;
	private Composite fieldsButtonsComposite;
	private Button blockDownButton;
	private Button blockUpButton;
	private Composite blocksButtonsComposite;
	private Composite blocksTableComposite;
	private Group structFieldsGroup;

	private TableViewer blocksTableViewer;
	private TableViewer fieldsTableViewer;

	public static void showGUI() {
		Display display = Display.getDefault();
		Shell shell = new Shell(display);
		PersonStructOrderComposite inst = new PersonStructOrderComposite(shell, SWT.NULL);
		Point size = inst.getSize();
		shell.setLayout(new FillLayout());
		shell.layout();
		if(size.x == 0 && size.y == 0) {
			inst.pack();
			shell.pack();
		} else {
			Rectangle shellBounds = shell.computeTrim(0, 0, size.x, size.y);
			int MENU_HEIGHT = 22;
			if (shell.getMenuBar() != null)
				shellBounds.height -= MENU_HEIGHT;
			shell.setSize(shellBounds.width, shellBounds.height);
		}
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
	
	
	/**
	 * 
	 * @param parent
	 * @param style
	 */
	public PersonStructOrderComposite(Composite parent, int style) {
		super(parent, style);
		GridLayout thisLayout = new GridLayout();
		thisLayout.numColumns = 2;
		thisLayout.makeColumnsEqualWidth = true;
		this.setLayout(thisLayout);
		
		GridData thisData = new GridData(GridData.FILL_BOTH);
		this.setLayoutData(thisData);

		titleComposite = new Composite(this, SWT.NONE);
		RowLayout titleCompositeLayout = new RowLayout(org.eclipse.swt.SWT.HORIZONTAL);
		GridData titleCompositeLData = new GridData();
		titleCompositeLData.grabExcessHorizontalSpace = true;
		titleCompositeLData.horizontalSpan = 2;
		titleCompositeLData.horizontalAlignment = GridData.FILL;
		titleComposite.setLayoutData(titleCompositeLData);
		titleComposite.setLayout(titleCompositeLayout);
		
		titleLabel = new Label(titleComposite, SWT.NONE | SWT.WRAP);
		titleLabel.setText(Messages.getString("org.nightlabs.jfire.base.person.preferences.PersonStructOrderComposite.titleLabel.text")); //$NON-NLS-1$
		
		structBlocksGroup = new Group(this, SWT.NONE);
		GridLayout groupStructBlocksLayout = new GridLayout();
		groupStructBlocksLayout.numColumns = 2;
		groupStructBlocksLayout.horizontalSpacing = 0;
		groupStructBlocksLayout.marginWidth = 0;
		groupStructBlocksLayout.marginHeight = 0;
		GridData groupStructBlocksLData = new GridData();
		groupStructBlocksLData.grabExcessHorizontalSpace = true;
		groupStructBlocksLData.grabExcessVerticalSpace = true;
		groupStructBlocksLData.verticalAlignment = GridData.FILL;
		groupStructBlocksLData.horizontalAlignment = GridData.FILL;
		structBlocksGroup.setLayoutData(groupStructBlocksLData);
		structBlocksGroup.setLayout(groupStructBlocksLayout);
		structBlocksGroup.setText(Messages.getString("org.nightlabs.jfire.base.person.preferences.PersonStructOrderComposite.structBlocksGroup.text")); //$NON-NLS-1$
		
		blocksTableComposite = new Composite(structBlocksGroup, SWT.NONE);
		GridLayout blocksTableCompositeLayout = new GridLayout();
		GridData blocksTableCompositeLData = new GridData();
		blocksTableCompositeLData.grabExcessVerticalSpace = true;
		blocksTableCompositeLData.verticalAlignment = GridData.FILL;
		blocksTableCompositeLData.grabExcessHorizontalSpace = true;
		blocksTableCompositeLData.horizontalAlignment = GridData.FILL;
		blocksTableComposite.setLayoutData(blocksTableCompositeLData);
		blocksTableCompositeLayout.makeColumnsEqualWidth = true;
		blocksTableComposite.setLayout(blocksTableCompositeLayout);
		
		blocksButtonsComposite = new Composite(structBlocksGroup, SWT.NONE);
		GridLayout blocksButtonsCompositeLayout = new GridLayout();
		GridData blocksButtonsCompositeLData = new GridData();
		blocksButtonsCompositeLData.grabExcessVerticalSpace = true;
		blocksButtonsCompositeLData.verticalAlignment = GridData.FILL;
		blocksButtonsCompositeLData.horizontalAlignment = GridData.END;
		blocksButtonsCompositeLData.horizontalIndent = 0;
//		blocksButtonsCompositeLData.widthHint = 40;
		blocksButtonsComposite.setLayoutData(blocksButtonsCompositeLData);
		blocksButtonsCompositeLayout.makeColumnsEqualWidth = true;
		blocksButtonsComposite.setLayout(blocksButtonsCompositeLayout);
		blocksButtonsCompositeLayout.horizontalSpacing = 0;
//		blocksButtonsCompositeLayout.verticalSpacing = 0;
		blocksButtonsCompositeLayout.marginWidth = 0;
		blocksButtonsCompositeLayout.marginHeight = 0;
		
		blockUpButton = new Button(blocksButtonsComposite, SWT.PUSH);
		blockUpButton.setText(Messages.getString("org.nightlabs.jfire.base.person.preferences.PersonStructOrderComposite.blockUpButton.text")); //$NON-NLS-1$
		GridData buttonLData = new GridData(GridData.FILL_HORIZONTAL);
		buttonLData.horizontalIndent = 0;
		buttonLData.horizontalIndent = 0;
		blockUpButton.setLayoutData(buttonLData);
		blockUpButton.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent evt) {
				buttonUpPressed(evt);
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		blockDownButton = new Button(blocksButtonsComposite, SWT.PUSH);
		blockDownButton.setText(Messages.getString("org.nightlabs.jfire.base.person.preferences.PersonStructOrderComposite.blockDownButton.text")); //$NON-NLS-1$
		buttonLData = new GridData(GridData.FILL_HORIZONTAL);
		buttonLData.horizontalIndent = 0;
		buttonLData.horizontalIndent = 0;
		blockDownButton.setLayoutData(buttonLData);
		blockDownButton.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent evt) {
				buttonDownPressed(evt);
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		
		structFieldsGroup = new Group(this, SWT.NONE);
		GridLayout groupStructFieldsLayout = new GridLayout();
		groupStructFieldsLayout.numColumns = 2;
		groupStructFieldsLayout.horizontalSpacing = 0;
		groupStructFieldsLayout.verticalSpacing = 0;
		groupStructFieldsLayout.marginHeight = 0;
		groupStructFieldsLayout.marginWidth = 0;
		GridData groupStructFieldsLData = new GridData();
		groupStructFieldsLData.grabExcessHorizontalSpace = true;
		groupStructFieldsLData.horizontalAlignment = GridData.FILL;
		groupStructFieldsLData.grabExcessVerticalSpace = true;
		groupStructFieldsLData.verticalAlignment = GridData.FILL;
		structFieldsGroup.setLayoutData(groupStructFieldsLData);
		structFieldsGroup.setLayout(groupStructFieldsLayout);
		structFieldsGroup.setText(Messages.getString("org.nightlabs.jfire.base.person.preferences.PersonStructOrderComposite.structFieldsGroup.text")); //$NON-NLS-1$
		
		fieldsTableComposite = new Composite(structFieldsGroup, SWT.NONE);
		GridLayout fieldsTableCompositeLayout = new GridLayout();
		GridData fieldsTableCompositeLData = new GridData();
		fieldsTableCompositeLData.grabExcessVerticalSpace = true;
		fieldsTableCompositeLData.verticalAlignment = GridData.FILL;
		fieldsTableCompositeLData.horizontalAlignment = GridData.FILL;
		fieldsTableCompositeLData.grabExcessHorizontalSpace = true;
		fieldsTableComposite.setLayoutData(fieldsTableCompositeLData);
		fieldsTableCompositeLayout.makeColumnsEqualWidth = true;
		fieldsTableComposite.setLayout(fieldsTableCompositeLayout);
		
		fieldsButtonsComposite = new Composite(structFieldsGroup, SWT.NONE);
		GridLayout fieldsButtonsCompositeLayout = new GridLayout();
		GridData fieldsButtonsCompositeLData = new GridData();
		fieldsButtonsCompositeLData.verticalAlignment = GridData.FILL;
		fieldsButtonsCompositeLData.grabExcessVerticalSpace = true;
		fieldsButtonsCompositeLData.horizontalAlignment = GridData.END;
		fieldsButtonsComposite.setLayoutData(fieldsButtonsCompositeLData);
		fieldsButtonsCompositeLayout.makeColumnsEqualWidth = true;
		fieldsButtonsComposite.setLayout(fieldsButtonsCompositeLayout);
		
		fieldUpButton = new Button(fieldsButtonsComposite, SWT.PUSH);
		fieldUpButton.setText(Messages.getString("org.nightlabs.jfire.base.person.preferences.PersonStructOrderComposite.fieldUpButton.text")); //$NON-NLS-1$
		buttonLData = new GridData(GridData.FILL_HORIZONTAL);
		buttonLData.horizontalIndent = 0;
		buttonLData.horizontalIndent = 0;
		fieldUpButton.setLayoutData(buttonLData);
		
		fieldDownButton = new Button(fieldsButtonsComposite, SWT.PUSH);
		fieldDownButton.setText(Messages.getString("org.nightlabs.jfire.base.person.preferences.PersonStructOrderComposite.fieldDownButton.text")); //$NON-NLS-1$
		buttonLData = new GridData(GridData.FILL_HORIZONTAL);
		buttonLData.horizontalIndent = 0;
		buttonLData.horizontalIndent = 0;
		fieldDownButton.setLayoutData(buttonLData);
		
		// Blocks table
		blocksTableViewer = new TableViewer(blocksTableComposite, SWT.BORDER | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);    
		GridData tgd = new GridData(GridData.FILL_BOTH);    
		Table t = blocksTableViewer.getTable(); 
		t.setHeaderVisible(true);
		t.setLinesVisible(true);
		t.setLayoutData(tgd);
		t.setLayout(new TableLayout());    
		blocksTableViewer.setContentProvider(new BlockOrderListContentProvider());
		blocksTableViewer.setLabelProvider(new BlockOrderListLabelProvider());
		new TableColumn(t, SWT.LEFT, 0).setText(Messages.getString("org.nightlabs.jfire.base.person.preferences.PersonStructOrderComposite.blockTableColumn.text")); //$NON-NLS-1$
		blocksTableViewer.setInput(
				PersonStructOrderConfigModule.sharedInstance()
				.structBlockDisplayOrder()
		);
		
		
		// Fields table
		fieldsTableViewer = new TableViewer(fieldsTableComposite, SWT.BORDER | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		tgd = new GridData(GridData.FILL_BOTH);    
		t = fieldsTableViewer.getTable(); 
		t.setHeaderVisible(true);
		t.setLinesVisible(true);
		t.setLayoutData(tgd);
		t.setLayout(new TableLayout());    
		new TableColumn(t, SWT.LEFT, 0).setText(Messages.getString("org.nightlabs.jfire.base.person.preferences.PersonStructOrderComposite.fieldTableColumn.text")); //$NON-NLS-1$
		
		this.layout();
	}
	
	
	protected void buttonUpPressed(SelectionEvent evt) {
		if (evt.getSource().equals(blockUpButton)) {
			blocksTableViewer.getElementAt(0);
			IStructuredSelection selection = (IStructuredSelection)blocksTableViewer.getSelection();
			if (!selection.isEmpty()) {
				Map.Entry entry = (Map.Entry)selection.getFirstElement();
				int priority = ((Integer)entry.getValue()).intValue();
	//			blocksTableViewer.getSelection();
				((BlockOrderListContentProvider)blocksTableViewer.getContentProvider()).moveUp(priority);
				blocksTableViewer.refresh();
	//			blocksTableViewer.getContentProvider().
	//			blocksTableViewer.
			}
		}
	}
	
	protected void buttonDownPressed(SelectionEvent evt) {
		if (evt.getSource().equals(blockDownButton)) {
			blocksTableViewer.getElementAt(0);
			IStructuredSelection selection = (IStructuredSelection)blocksTableViewer.getSelection();
			if (!selection.isEmpty()) {
				Map.Entry entry = (Map.Entry)selection.getFirstElement();
				int priority = ((Integer)entry.getValue()).intValue();
	//			blocksTableViewer.getSelection();
				((BlockOrderListContentProvider)blocksTableViewer.getContentProvider()).moveDown(priority);
				blocksTableViewer.refresh();
	//			blocksTableViewer.getContentProvider().
	//			blocksTableViewer.
			}
		}
	}
	
	/**
	 * 
	 */
	public Map getStructBlockOrder() {
		return ((BlockOrderListContentProvider)blocksTableViewer.getContentProvider()).getStructBlockOrder();
	}
}
