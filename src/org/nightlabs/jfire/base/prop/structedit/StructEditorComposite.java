package org.nightlabs.jfire.base.prop.structedit;

import java.util.LinkedList;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.nightlabs.base.composite.ComboComposite;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.language.I18nTextEditor;
import org.nightlabs.base.language.LanguageChooser;
import org.nightlabs.base.language.LanguageChooserImageCombo;
import org.nightlabs.base.wizard.DynamicPathWizardDialog;
import org.nightlabs.base.wizard.DynamicPathWizardPage;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.AbstractStructField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.exception.IllegalStructureModificationException;
import org.nightlabs.jfire.prop.id.StructID;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.math.Base36Coder;

public class StructEditorComposite extends XComposite {

	private StructTree structTree;
	private XComposite editComposite;
	private StructBlockEditorComposite blockEditComposite;
	private StructFieldEditorComposite fieldEditComposite;
	private StackLayout editCompositeStackLayout;
	private LanguageChooserImageCombo langChooser;	
	private ComboComposite<StructLocalID> structIDComposite;	
	private StructEditor structEditor;
	private IStruct struct;

	private static class StructLocalIDLabelProvider extends org.eclipse.jface.viewers.LabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof StructID) {
				StructID structID = (StructID) element;
				return structID.organisationID + "#" + structID.linkClass;
			}
			if (element instanceof StructLocalID) {
				StructLocalID structLocalID = (StructLocalID) element;
				return structLocalID.organisationID + '#' + structLocalID.linkClass;
			}
			return super.getText(element);
		}
	}

	private class StructIDComboSelectionListener implements SelectionListener {
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		public void widgetSelected(SelectionEvent e) {
			if (structEditor.isChanged()) {
				MessageBox mb = new MessageBox(getShell(), SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
				mb.setText("Current structure was modified.");
				mb.setMessage("The current structure was modified. Save changes?");
				switch (mb.open())
				{
				case SWT.YES:
					structEditor.storeStructure();
					break;
				case SWT.NO:
					structEditor.setChanged(false);
					break;
				case SWT.CANCEL:
					return;
				}
			}
			structEditor.setCurrentStructLocalID(structIDComposite.getSelectedElement());
		}
	}

	public StructEditorComposite(Composite parent, int style, StructEditor structEditor) {
		super(parent, style);
		this.setLayout(new GridLayout(2, false));
		this.structEditor = structEditor;

		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.CENTER;

		structIDComposite = new ComboComposite<StructLocalID>(this, SWT.NONE, new StructLocalIDLabelProvider(), "Current struct: ");
		structIDComposite.setLayoutData(gd);
		structIDComposite.getCombo().addSelectionListener(new StructIDComboSelectionListener());
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(gd);

		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.CENTER;

		langChooser = new LanguageChooserImageCombo(this, true, true);
		langChooser.setLayoutData(gd);

		structTree = new StructTree(this, true, langChooser);
		structTree.getTreeViewer().addSelectionChangedListener(new SelectionChangedListener());
		gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = 200;
		structTree.setLayoutData(gd);

		editComposite = new XComposite(this, style);
		gd = new GridData(GridData.FILL_BOTH);
		editComposite.setLayoutData(gd);
		editCompositeStackLayout = new StackLayout();
		editComposite.setLayout(editCompositeStackLayout);
		blockEditComposite = new StructBlockEditorComposite(editComposite, langChooser, style, structEditor);
		blockEditComposite.getBlockNameEditor().addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				IStructuredSelection sel = (IStructuredSelection) structTree.getSelection();
				structTree.refresh(sel.getFirstElement());
			}
		});
		fieldEditComposite = new StructFieldEditorComposite(editComposite, langChooser, style, structEditor);
		fieldEditComposite.getFieldNameEditor().addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				IStructuredSelection sel = (IStructuredSelection) structTree.getSelection();
				structTree.refresh(sel.getFirstElement());
			}
		});

		try {
			structIDComposite.setInput(new LinkedList<StructLocalID>(structEditor.getAvailableStructLocalIDs()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public void setLoadingText() {
		structTree.setInput("Loading structure...");
	}
	
	public void setCurrentStruct(IStruct struct) {
		this.struct = struct;
		structTree.setInput(struct);
	}	

	public void addStructBlock() {
		long newBlockID = IDGenerator.nextID(StructEditorComposite.class.getName() + '/' + "newBlockID");
		Base36Coder coder = Base36Coder.sharedInstance(false);

		try {
			StructBlock newBlock = new StructBlock(struct, Login.getLogin().getOrganisationID(), "sb_"
					+ coder.encode(newBlockID, 1));
			newBlock.getName().setText(langChooser.getLanguage().getLanguageID(), "Change me");
			struct.addStructBlock(newBlock);			
			structTree.addStructBlock(newBlock);
			structEditor.setChanged(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void addStructField() {
		StructFieldCreationWizard wiz = new StructFieldCreationWizard();
		DynamicPathWizardDialog dialog = new DynamicPathWizardDialog(wiz);

		if (dialog.open() == SWT.CANCEL)
			return;

		StructFieldMetaData newFieldMetaData = wiz.getSelectedFieldMetaData();
		addStructField(structTree.getCurrentBlockNode().getBlock(), newFieldMetaData, wiz.getDetailsWizardPage());		
	}

	public void addStructField(StructBlock toBlock, StructFieldMetaData newFieldMetaData,
			DynamicPathWizardPage detailsPage) {
		long newFieldID = IDGenerator.nextID(StructEditorComposite.class.getName() + '/' + "newFieldID");
		Base36Coder coder = Base36Coder.sharedInstance(false);

		IStructFieldFactory fieldFactory = newFieldMetaData.getFieldFactory();
		String fieldID = "sf_" + coder.encode(newFieldID, 3);

		try {
			AbstractStructField newField = fieldFactory.createStructField(toBlock, Login.getLogin().getOrganisationID(),
					fieldID, detailsPage);
			newField.getName().setText(langChooser.getLanguage().getLanguageID(), "Change me");

			structTree.addStructField(structTree.getCurrentBlockNode(), newField);
			structEditor.setChanged(true);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void removeSelectedItem() {
		Object selected = ((IStructuredSelection) structTree.getSelection()).getFirstElement();
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
		MessageBox mb = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		mb.setMessage("Are you sure you want to delete the selected struct block and all of its contained struct fields?");
		mb.setText("Confirm deletion");
		int result = mb.open();
		if (result == SWT.YES) {
			try {
				struct.removeStructBlock(blockNode.getBlock());
			} catch (IllegalStructureModificationException e) {
				e.printStackTrace();
				mb = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
				mb.setMessage("Block could not be deleted: " + e.getMessage());
				mb.setText("Deleting failed");
				mb.open();
				return;
			}
			structTree.removeStructBlock(blockNode);
			structEditor.setChanged(true);
		}
	}

	private void removeStructField(StructFieldNode fieldNode) {
		MessageBox mb = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		String fieldName = fieldNode.getI18nText().getText(langChooser.getLanguage().getLanguageID());
		String message = "Are you sure you want to delete the struct field named " + fieldName + "?";
		long dataFieldInstanceCount;
		dataFieldInstanceCount = structEditor.getDataFieldInstanceCount(fieldNode.getField().getStructFieldIDObj());
		
		message += "\n\n" + dataFieldInstanceCount
				+ " instances of this struct field will be also deleted if you continue.";
		mb.setMessage(message);
		mb.setText("Confirm deletion");
		int result = mb.open();
		if (result == SWT.YES) {
			try {
				fieldNode.getParentBlock().getBlock().removeStructField(fieldNode.getField());
				structTree.removeStructField(fieldNode.getParentBlock(), fieldNode);
				fieldEditComposite.setCurrentField(null);
			} catch (IllegalStructureModificationException e) {
				mb = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
				mb.setMessage("You cannot delete a struct block that has already been persisted.");
				mb.setText("Deletion failed");
				mb.open();
				return;
			}
		}
		structEditor.setChanged(true);
	}

	private class SelectionChangedListener implements ISelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			if (selection.isEmpty()) {
				editComposite.setEnabled(false);
				return;
			}
			
			editComposite.setEnabled(true);
			TreeNode selected = (TreeNode) selection.getFirstElement();

			if (selected instanceof StructFieldNode) {
				AbstractStructField field = ((StructFieldNode) selected).getField();
				fieldEditComposite.setCurrentField(field);
				editCompositeStackLayout.topControl = fieldEditComposite;
				editComposite.layout(true, true);
				I18nTextEditor fieldNameEditor = fieldEditComposite.getFieldNameEditor();
				fieldNameEditor.setSelection(0, fieldNameEditor.getEditText().length());
				fieldNameEditor.setFocus();
			} else if (selected instanceof StructBlockNode) {
				StructBlock block = ((StructBlockNode) selected).getBlock();
				blockEditComposite.setCurrentStructBlock(block);
				editCompositeStackLayout.topControl = blockEditComposite;
				editComposite.layout(true, true);
				I18nTextEditor blockNameEditor = blockEditComposite.getBlockNameEditor();
				blockNameEditor.setSelection(0, blockNameEditor.getEditText().length());
				blockNameEditor.setFocus();
			}
		}
	}

	public StructTree getStructTree() {
		return structTree;
	}

	public LanguageChooser getLanguageChooser() {
		return langChooser;
	}
}
