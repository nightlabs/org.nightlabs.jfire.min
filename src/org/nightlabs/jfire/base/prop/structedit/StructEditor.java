package org.nightlabs.jfire.base.prop.structedit;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.prop.StructLocalDAO;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertyManager;
import org.nightlabs.jfire.prop.PropertyManagerUtil;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.id.StructID;
import org.nightlabs.jfire.prop.id.StructLocalID;

public class StructEditor {
	private PropertyManager propertyManager;
	private IStruct currentStruct;
	private StructEditorComposite structEditorComposite;
	private Collection<IStructureChangedListener> changeListeners;
	private boolean changed = false;
	private StructEditorView structEditorView;
	
	public StructEditor(StructEditorView structEditorView) {
		changeListeners = new ArrayList<IStructureChangedListener>();
		propertyManager = getPropertyManager();
		this.structEditorView = structEditorView;
	}
	
	public StructEditorComposite createComposite(Composite parent, int style) {
		if (structEditorComposite == null)
			structEditorComposite = new StructEditorComposite(parent, style, this);
		
		return structEditorComposite; 
	}
	
	public void setCurrentStructLocalID(final StructLocalID structLocalID) {
		structEditorComposite.setLoadingText();
		new Job("Fetching structure...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				StructLocal struct = StructLocalDAO.sharedInstance().getStructLocal(structLocalID);
				currentStruct = org.nightlabs.util.Utils.cloneSerializable(struct);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {						
						structEditorComposite.setCurrentStruct(currentStruct);
					}
				});
				structEditorView.triggerSiteSelection();
				return Status.OK_STATUS;
			}
		}.schedule();		
	}

	private PropertyManager getPropertyManager() {
		if (propertyManager == null) {
			try {
				propertyManager = PropertyManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return propertyManager;
	}
	
	public Collection<StructID> getAvailableStructIDs() {
		try {
			return propertyManager.getAvailableStructIDs();
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public Collection<StructLocalID> getAvailableStructLocalIDs() {
		try {
			return propertyManager.getAvailableStructLocalIDs();
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public long getDataFieldInstanceCount(StructFieldID structFieldID) {
		try {
			return propertyManager.getDataFieldInstanceCount(structFieldID);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void storeStructure() {
		try {
			getPropertyManager().storeStruct(currentStruct);
			setChanged(false);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public void setChanged(boolean changed) {
		this.changed = changed;
		if (changed)
			notifyChangeListeners();
	}
	
	public boolean isChanged() {
		return changed;
	}
	
	public boolean hasStructureLoaded() {
		return currentStruct != null;
	}
	
	public void addStructureChangedListener(IStructureChangedListener listener) {
		if (!changeListeners.contains(listener))
			changeListeners.add(listener);
	}
	
	public void removeStructureChangedListener(IStructureChangedListener listener) {
		changeListeners.remove(listener);
	}
	
	private synchronized void notifyChangeListeners() {
		for (IStructureChangedListener listener : changeListeners)
			listener.structureChanged();
	}
}
