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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.nightlabs.base.tree.AbstractTreeComposite;
import org.nightlabs.jfire.config.id.ConfigID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ConfigPreferencesTreeComposite extends AbstractTreeComposite<ConfigPreferenceNode> {

	private ContentProvider contentProvider;
	private ConfigID currentConfigID;
	
	private String parentCode;
	
	private static class ContentProvider implements ITreeContentProvider {

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
	
	private static class LabelProvider extends org.eclipse.jface.viewers.LabelProvider {

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
		super(parent, SWT.BORDER, true, true, false); 
		setConfigID(configID);
		this.parentCode = Integer.toHexString(parent.hashCode());
	}
	
	@Override
	public void createTreeColumns(Tree tree) {
	}
	
	@Override
	public void setTreeProvider(TreeViewer treeViewer) {
		contentProvider = new ContentProvider();
		treeViewer.setContentProvider(contentProvider);
		treeViewer.setLabelProvider(new LabelProvider());
	}
	
	public void setConfigID(ConfigID configID) {
		if (configID == null || configID.equals(currentConfigID))
			return;
		this.currentConfigID = configID;
		if (currentConfigID != null) {			
			final ConfigPreferenceNode rootNode = ConfigSetupRegistry.sharedInstance().getMergedPreferenceRootNode(parentCode, currentConfigID);
			Display.getDefault().asyncExec(new Runnable(){
				public void run() {
					setInput(rootNode);
				}
			});
	}
	}
	
	public int getSelectionCount() {
		IStructuredSelection selection = (IStructuredSelection)getTreeViewer().getSelection();
		return selection.size();
	}

}
