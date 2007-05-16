/**
 * 
 */
package org.nightlabs.jfire.base.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDODetachedFieldAccessException;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.dialog.CenteredDialog;
import org.nightlabs.base.util.RCPUtil;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigModule;

/**
 * 
 * @author Marius Heinzmann <marius[AT]nightlabs[DOT]de>
 */
public class ChangedConfigModulePagesDialog
extends CenteredDialog // IconAndMessageDialog
{
	
	private ChangedConfigModulePagesDialog() {
		super(RCPUtil.getActiveWorkbenchShell());
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setBlockOnOpen(false);
//		message = "The following configurations changed. Mark the Modules that shall be reloaded.";
	}

	protected static class ContentProvider implements ITreeContentProvider {		
		Map<Config, Set<TreeItem>> updatedModules = null;
		
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Map) {
				updatedModules = (Map<Config, Set<TreeItem>>) inputElement;
				return updatedModules.keySet().toArray();
			}

			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof Config) {
				if (updatedModules.get((Config) element) != null)
					return true;
			}
				
			return false;
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Config) {
				Config currentConfig = (Config) parentElement;
				return updatedModules.get(currentConfig).toArray();
			}
			return new Object[0];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			System.out.println(newInput);
		}
	}
	
	protected static class LabelProvider extends org.eclipse.jface.viewers.LabelProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		@Override
		public String getText(Object element) {
			if (element instanceof Config)
				return ((Config) element).getConfigKey();
			
			return ((TreeItem) element).getUpdatedConfigModule().getCfModKey();
		}
	}
	
	protected class TreeItem {
		private AbstractConfigModulePreferencePage page;
		private ConfigModule updatedConfigModule;
		
		public TreeItem(AbstractConfigModulePreferencePage page, ConfigModule updatedModule) {
			this.page = page;
			this.updatedConfigModule = updatedModule;
		}
		
		public AbstractConfigModulePreferencePage getCorrespondingPage() {
			return page;
		}
		
		public ConfigModule getUpdatedConfigModule() {
			return updatedConfigModule;
		}
	}
		

	protected Map<Config, Set<TreeItem>> updatedConfigs = new HashMap<Config, Set<TreeItem>>();

	protected TreeViewer treeViewer = null;
	
	private static ChangedConfigModulePagesDialog sharedInstance = null;

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Trallalitralla");
	}
	
	/**
	 * Adds an updated {@link ConfigModule} to the dialog , which asks the user to mark the modules that
	 * shall be updated from the server. If no dialog is open, a new one is created.
	 * <p>
	 * This method must be called on the GUI Thread!
	 * </p>
	 * 
	 * @param page the {@link AbstractConfigModulePreferencePage}, which may be updated
	 * @param updatedModule the updated {@link ConfigModule}
	 * @param correspondingConfig the config corresponding to the changed module
	 */
	public static void addChangedConfigModule(AbstractConfigModulePreferencePage page, 
			ConfigModule updatedModule, Config correspondingConfig) 
	{
		// TODO we should NOT pass the correspondingConfig because it allows inconsistent data to be passed
		// to this dialog. Instead the correspondingConfig should be read from the config module.
		// It should, however, only be detached with the primary keys (FetchPlan.DEFAULT) and NOT any other fields!
		// TODO requires further thoughts because of modification notifications (the module would get dirty on the client side if the config is marked dirty)
		try {
			if (! updatedModule.getConfig().equals(correspondingConfig))
				throw new RuntimeException("The updated ConfigModule does not belong to the given Config!");
		} catch (JDODetachedFieldAccessException e) {
			// This may happen if the fetchDepth or the FetchGroups did not include the Config of the 
			// ConfigModule. In these cases we believe the caller gives us the correct corresponding config. 
		}

		if (sharedInstance == null) {
			sharedInstance = new ChangedConfigModulePagesDialog();
			sharedInstance.open();
		}

		sharedInstance._addChangedConfigModule(page, updatedModule, correspondingConfig);
//		if (! sharedInstance.isDialogOpened()) {
//			sharedInstance.open(); // TODO really blocking mode?! NO!!! We don't use blocking mode anymore!
//		}
	}

	private void _addChangedConfigModule(AbstractConfigModulePreferencePage page, ConfigModule updatedModule,
			Config correspondingConfig) {
		Set<TreeItem> changedModulesOfConfig = updatedConfigs.get(correspondingConfig);
		if (changedModulesOfConfig == null) {
			changedModulesOfConfig = new HashSet<TreeItem>();
			updatedConfigs.put(correspondingConfig, changedModulesOfConfig);			
		}
		changedModulesOfConfig.add(new TreeItem(page, updatedModule));

		treeViewer.refresh(updatedConfigs);
		treeViewer.expandToLevel(correspondingConfig, 2);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		super.okPressed();
	}

//	@Override
//	protected Image getImage() {
//		return getInfoImage();
//	}

//	/* (non-Javadoc)
//	 * @see org.eclipse.jface.dialogs.IconAndMessageDialog#createContents(org.eclipse.swt.widgets.Composite)
//	 */
//	@Override
//	protected void createDialogAndButtonArea(Composite parent) {
//		super.createDialogAndButtonArea(parent);
//		Composite composite = new Composite(parent, SWT.NONE);
//		GridLayout layout = new GridLayout(1, false);
//		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
//		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
//		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
//		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
//		composite.setLayout(layout);
//		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
//		treeViewer = new TreeViewer(composite, SWT.CHECK);
//		treeViewer.setContentProvider(new ContentProvider());
//		treeViewer.setLabelProvider(new LabelProvider());
//		if (updatedConfigs != null)
//			treeViewer.setInput(updatedConfigs);
//		applyDialogFont(composite);
//	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		treeViewer = new TreeViewer(area, SWT.CHECK);
		treeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		treeViewer.setContentProvider(new ContentProvider());
		treeViewer.setLabelProvider(new LabelProvider());
		treeViewer.setInput(updatedConfigs);
		return area;
	}

	@Override
	public boolean close() {
		boolean res = super.close();
		if (res)
			sharedInstance = null;

		return res;
	}

//	/**
//	 * @param dialogOpened the dialogOpened to set
//	 */
//	public void setDialogOpened(boolean dialogOpened) {
//		this.dialogOpened = dialogOpened;
//	}

}
