package org.nightlabs.jfire.base.prop.structedit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.nightlabs.base.language.I18nTextEditor;
import org.nightlabs.base.language.LanguageChooser;
import org.nightlabs.base.util.RCPUtil;
import org.nightlabs.base.wizard.DynamicPathWizardDialog;
import org.nightlabs.base.wizard.DynamicPathWizardPage;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.prop.StructLocalDAO;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertyManager;
import org.nightlabs.jfire.prop.PropertyManagerUtil;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.exception.IllegalStructureModificationException;
import org.nightlabs.jfire.prop.exception.PropertyException;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.math.Base36Coder;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;
import org.nightlabs.notification.NotificationListenerCallerThread;

public class StructEditor {
	private StructTree structTree;
	private StructPartEditor currentStructPartEditor;
	private StructBlockEditor structBlockEditor;	
	private LanguageChooser languageChooser;
	private TreeNode lastSelection;
	private boolean ignoreChangeEvent;
	
	private Composite structPartEditorComposite;
	
	private PropertyManager propertyManager;
	private IStruct currentStruct;
	private StructEditorComposite structEditorComposite;
	private Collection<StructureChangedListener> changeListeners;
	private boolean changed = false;
	private StructEditorView structEditorView;
	
	public StructEditor(StructEditorView structEditorView) {
		changeListeners = new ArrayList<StructureChangedListener>();
		propertyManager = getPropertyManager();
		this.structEditorView = structEditorView;
		this.structBlockEditor = new StructBlockEditor();
	}
	
	public StructEditorComposite createComposite(Composite parent, int style) {
		if (structEditorComposite == null) {
			structTree = new StructTree(this);
			structEditorComposite = new StructEditorComposite(parent, style, this, structTree);
			languageChooser = structEditorComposite.getLanguageChooser();
			JDOLifecycleManager.sharedInstance().addNotificationListener(StructLocal.class, changeListener);
			structEditorComposite.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					JDOLifecycleManager.sharedInstance().removeNotificationListener(StructLocal.class, changeListener);
				}
			});
			
			structTree.addSelectionChangedListener(new ISelectionChangedListener() {				
				public void selectionChanged(SelectionChangedEvent event) {
					if (ignoreChangeEvent)
						return;
					
					if (!validatePartEditor()) {
						// restore last selection
						ignoreChangeEvent = true;
						structTree.select(lastSelection);
						ignoreChangeEvent = false;
						return;						
					}
					
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					if (selection.isEmpty()) {
						currentStructPartEditor.setEnabled(false);
						return;
					}
					
					lastSelection = (TreeNode) ((IStructuredSelection)event.getSelection()).getFirstElement();
					
					if (structPartEditorComposite != null)
						structPartEditorComposite.dispose();
					
					TreeNode selected = (TreeNode) selection.getFirstElement();
					StructFieldFactoryRegistry structFieldFactoryRegistry = StructFieldFactoryRegistry.sharedInstance();

					
					if (selected instanceof StructFieldNode) {
						StructField field = ((StructFieldNode) selected).getField();
						try {
							StructFieldEditor editor = structFieldFactoryRegistry.getEditorSingleton(field);
							structEditorComposite.setPartEditor(editor);
							editor.setData(field);
							currentStructPartEditor = editor;
							// save the data of the editor to make it able to be restored later
							editor.saveData();
						} catch (PropertyException e) {
							e.printStackTrace();
							throw new RuntimeException(e);
						}						
					} else if (selected instanceof StructBlockNode) {
						StructBlock block = ((StructBlockNode) selected).getBlock();
						structEditorComposite.setPartEditor(structBlockEditor);
						structBlockEditor.setData(block);
						currentStructPartEditor = structBlockEditor;						
					}
					
					I18nTextEditor partNameEditor = currentStructPartEditor.getPartNameEditor();
					partNameEditor.setSelection(0, partNameEditor.getEditText().length());
					partNameEditor.setFocus();
					partNameEditor.addModifyListener(new ModifyListener() {
						public void modifyText(ModifyEvent e) {
							StructEditor.this.setChanged(true);
							structTree.refreshSelected();
						}
					});
				}				
			});
		}
		
		setCurrentStructLocalID(StructEditorUtil.getAvailableStructLocalIDs().toArray(new StructLocalID[1])[0]);
		return structEditorComposite; 
	}
	
	public void setCurrentStructLocalID(final StructLocalID structLocalID) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {						
				structEditorComposite.setLoadingText();
			}
		});
		new Job("Fetching structure...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						StructLocal struct = StructLocalDAO.sharedInstance().getStructLocal(structLocalID);
						currentStruct = org.nightlabs.util.Utils.cloneSerializable(struct);
						structTree.setInput(currentStruct);						
					}
				});
				structEditorView.triggerSiteSelection();
				return Status.OK_STATUS;
			}
		}.schedule();		
	}
	
	private boolean validatePartEditor() {
		if (currentStructPartEditor instanceof StructBlockEditor)
			return true;
		
		if (currentStructPartEditor instanceof StructFieldEditor<?>) {
			StructFieldEditor<?> structFieldEditor = (StructFieldEditor<?>) currentStructPartEditor;
			if (!structFieldEditor.validateInput()) {
				MessageBox mb = new MessageBox(RCPUtil.getActiveWorkbenchShell(), SWT.YES | SWT.NO );
				String message = "The entered data is not valid.\nError: " + structFieldEditor.getValidationError();
				message += "\nDo you want to discard all changes regarding this field?";
				mb.setMessage(message);
				mb.setText("Validation error");
				switch (mb.open()) {
				case SWT.YES:
					structFieldEditor.restoreData();
					return true;
				case SWT.NO:					
					return false;
				}
			}
			return true;
		}
		
		return true;
	}

	private NotificationListener changeListener = new NotificationListenerCallerThread() {
		public void notify(NotificationEvent notificationEvent) {
			for (DirtyObjectID dirtyObjectID : (Set<DirtyObjectID>)notificationEvent.getSubjects()) {
				Object currentStructID = currentStruct == null ? null : JDOHelper.getObjectId(currentStruct);
				if (dirtyObjectID.getObjectID().equals(currentStructID)) {
					setCurrentStructLocalID((StructLocalID) dirtyObjectID.getObjectID());
				}
			}
		}
	};
	
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
	
	public StructTree getStructTree() {
		return structTree;
	}
	
	public LanguageChooser getLanguageChooser() {
		return languageChooser;
	}
	
	public void addStructureChangedListener(StructureChangedListener listener) {
		if (!changeListeners.contains(listener))
			changeListeners.add(listener);
	}
	
	public void removeStructureChangedListener(StructureChangedListener listener) {
		changeListeners.remove(listener);
	}
	
	private synchronized void notifyChangeListeners() {
		for (StructureChangedListener listener : changeListeners)
			listener.structureChanged();
	}
	
	public void addStructBlock() {
		long newBlockID = IDGenerator.nextID(StructEditor.class.getName() + '/' + "newBlockID");
		Base36Coder coder = Base36Coder.sharedInstance(false);

		StructBlock newBlock;
		try {
			newBlock = new StructBlock(currentStruct, Login.getLogin().getOrganisationID(), "sb_"
					+ coder.encode(newBlockID, 1));
			newBlock.getName().setText(languageChooser.getLanguage().getLanguageID(), "Change me");
			currentStruct.addStructBlock(newBlock);
			structTree.addStructBlock(newBlock);
			setChanged(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		structTree.select(newBlock);		
	}

	public void addStructField(StructBlock structBlock) {
		StructFieldCreationWizard wiz = new StructFieldCreationWizard();
		DynamicPathWizardDialog dialog = new DynamicPathWizardDialog(wiz);

		if (dialog.open() == SWT.CANCEL)
			return;

		StructFieldMetaData newFieldMetaData = wiz.getSelectedFieldMetaData();
		addStructField(structBlock, newFieldMetaData, wiz.getDetailsWizardPage());		
	}
	
	private void addStructField(StructBlock toBlock, StructFieldMetaData newFieldMetaData, DynamicPathWizardPage detailsPage) {
		long newFieldID = IDGenerator.nextID(StructEditorComposite.class.getName() + '/' + "newFieldID");
		Base36Coder coder = Base36Coder.sharedInstance(false);

		StructFieldFactory fieldFactory = newFieldMetaData.getFieldFactory();
		String fieldID = "sf_" + coder.encode(newFieldID, 3);

		StructField newField;
		
		try {
			newField = fieldFactory.createStructField(toBlock, Login.getLogin().getOrganisationID(), fieldID, detailsPage);
			newField.getName().setText(languageChooser.getLanguage().getLanguageID(), "Change me");

			structTree.addStructField(structTree.getCurrentBlockNode(), newField);
			setChanged(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		structTree.select(newField);
	}
	
	public void removeSelectedItem() {
		TreeNode selected = structTree.getSelectedNode();
		if (selected instanceof StructBlockNode) {
			StructBlockNode block = (StructBlockNode) selected;
			removeStructBlock(block);
		} else if (selected instanceof StructFieldNode) {
			StructFieldNode field = (StructFieldNode) selected;
			removeStructField(field);
		}
		
		structTree.refresh();
	}
	
	private void removeStructBlock(StructBlockNode blockNode) {
		MessageBox mb = new MessageBox(RCPUtil.getActiveWorkbenchShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		mb.setMessage("Are you sure you want to delete the selected struct block and all of its contained struct fields?");
		mb.setText("Confirm deletion");
		int result = mb.open();
		if (result == SWT.YES) {
			try {
				currentStruct.removeStructBlock(blockNode.getBlock());
			} catch (IllegalStructureModificationException e) {
				e.printStackTrace();
				mb = new MessageBox(null, SWT.ICON_ERROR | SWT.OK);
				mb.setMessage("Block could not be deleted: " + e.getMessage());
				mb.setText("Deleting failed");
				mb.open();
				return;
			}
			
			structTree.removeStructBlock(blockNode);
			setChanged(true);
		}
	}

	private void removeStructField(StructFieldNode fieldNode) {
		MessageBox mb = new MessageBox(RCPUtil.getActiveWorkbenchShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		String fieldName = fieldNode.getI18nText().getText(languageChooser.getLanguage().getLanguageID());
		String message = "Are you sure you want to delete the struct field named '" + fieldName + "'?";
		long dataFieldInstanceCount;
		dataFieldInstanceCount = StructEditorUtil.getDataFieldInstanceCount(fieldNode.getField().getStructFieldIDObj());
		
		message += "\n\n" + dataFieldInstanceCount + " instances of this struct field will be also deleted if you continue.";
		mb.setMessage(message);
		mb.setText("Confirm deletion");
		int result = mb.open();
		if (result == SWT.YES) {
			try {
				fieldNode.getParentBlock().getBlock().removeStructField(fieldNode.getField());
				structTree.removeStructField(fieldNode.getParentBlock(), fieldNode);
				currentStructPartEditor.setData(null);
			} catch (IllegalStructureModificationException e) {
				mb = new MessageBox(null, SWT.ICON_ERROR | SWT.OK);
				mb.setMessage("You cannot delete a struct block that has already been persisted.");
				mb.setText("Deletion failed");
				mb.open();
				return;
			}
		}
		
		setChanged(true);
	}
}
