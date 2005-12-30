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

package org.nightlabs.jfire.base.config;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.nightlabs.base.composite.XComposite;
import org.nightlabs.jfire.config.id.ConfigID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ConfigPreferencesTreeComposite extends XComposite {

	private TreeViewer treeViewer;
	private ContentProvider contentProvider;
	private ConfigID currentConfigID;
	
	private String parentCode;
	
	public static class ContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ConfigPreferenceNode)
				return ((ConfigPreferenceNode)parentElement).getChildren().toArray();
			return null;
		}

		public Object getParent(Object element) {
			if (element instanceof ConfigPreferenceNode)
				return ((ConfigPreferenceNode)element).getParent();
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof ConfigPreferenceNode)
				return ((ConfigPreferenceNode)element).getChildren().size() > 0;
			return false;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}		
	}
	
	public static class LabelProvider extends org.eclipse.jface.viewers.LabelProvider {

		public Image getImage(Object element) {
			if (element instanceof ConfigPreferenceNode)
				return ((ConfigPreferenceNode)element).getIcon();
			return super.getImage(element);
		}

		public String getText(Object element) {
			if (element instanceof ConfigPreferenceNode)
				return ((ConfigPreferenceNode)element).getConfigPreferenceName();
			return super.getText(element);
		}
		
	}
	
	
	/**
	 * @param parent
	 * @param style
	 * @param setLayoutData
	 */
	public ConfigPreferencesTreeComposite(Composite parent, int style, boolean setLayoutData, ConfigID configID) {
		super(parent, style, 
				XComposite.LAYOUT_MODE_TIGHT_WRAPPER, 
				setLayoutData ? XComposite.LAYOUT_DATA_MODE_GRID_DATA : XComposite.LAYOUT_DATA_MODE_NONE
		);
//		this.currentConfigGroupID();
		treeViewer = new TreeViewer(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		treeViewer.getTree().setLayoutData(gridData);

		contentProvider = new ContentProvider();
		treeViewer.setContentProvider(contentProvider);
		treeViewer.setLabelProvider(new LabelProvider());
		setConfigID(configID);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
				if (selection.size() <= 0)
					selectedPreferenceNode = null;
				selectedPreferenceNode = (ConfigPreferenceNode)selection.getFirstElement();
			}
			
		});
		this.parentCode = Integer.toHexString(parent.hashCode());
	}
	
	public void setConfigID(ConfigID configID) {
		if (configID == null || configID.equals(currentConfigID))
			return;
		this.currentConfigID = configID;
		if (currentConfigID != null) {			
			final ConfigPreferenceNode rootNode = ConfigSetupRegistry.sharedInstance().getMergedPreferenceRootNode(parentCode, currentConfigID);
			Display.getDefault().asyncExec(new Runnable(){
				public void run() {
					treeViewer.setInput(rootNode);
				}
			});
	}
	}
	
	public int getSelectionCount() {
		IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
		return selection.size();
	}
	
	private ConfigPreferenceNode selectedPreferenceNode;
	
	public ConfigPreferenceNode getSelectedPreferenceNode() {
		return selectedPreferenceNode;
	}
	
	public TreeViewer getTreeViewer() {
		return treeViewer;
	}
}
