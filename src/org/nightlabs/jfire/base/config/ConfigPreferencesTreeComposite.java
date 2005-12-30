/*
 * Created 	on May 31, 2005
 * 					by alex
 *
 */
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
