package org.nightlabs.jfire.base.prop.structedit;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.nightlabs.base.composite.ComboComposite;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.language.LanguageChooser;
import org.nightlabs.base.language.LanguageChooserImageCombo;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.id.StructID;
import org.nightlabs.jfire.prop.id.StructLocalID;

public class StructEditorComposite extends XComposite {

	private StructPartEditor<?> structPartEditor;
	private StructTree structTree;
	private IStruct struct;
	private StructEditor structEditor;
	
	private Composite partEditorComposite;
	private LanguageChooserImageCombo languageChooser;	
	protected ComboComposite<StructLocalID> structIDComposite;
	private Label errorLabel;

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

	public StructEditorComposite(Composite parent, int style, StructEditor structEditor, StructTree structTree) {
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

		languageChooser = new LanguageChooserImageCombo(this, true, true);
		languageChooser.setLayoutData(gd);

		this.structTree = structTree;
		structTree.createComposite(this, style, languageChooser);
		gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = 200;
		structTree.getComposite().setLayoutData(gd);

		try {
			structIDComposite.setInput(new LinkedList<StructLocalID>(StructEditorUtil.getAvailableStructLocalIDs()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public void setLoadingText() {
		structTree.setInput("Loading structure...");
	}
	
	public void setPartEditor(StructPartEditor structPartEditor) {
		if (partEditorComposite != null) {
			partEditorComposite.dispose();
		}
		
		this.structPartEditor = structPartEditor;
		this.partEditorComposite = structPartEditor.createComposite(this, this.getStyle(), structEditor, languageChooser);
		((GridData)this.partEditorComposite.getLayoutData()).verticalAlignment = SWT.TOP;
		this.partEditorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.layout(true, true);
	}
	
	public LanguageChooser getLanguageChooser() {
		return languageChooser;
	}
}
