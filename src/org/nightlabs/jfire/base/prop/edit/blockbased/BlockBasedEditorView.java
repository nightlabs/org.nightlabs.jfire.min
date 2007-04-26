package org.nightlabs.jfire.base.prop.edit.blockbased;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;
import org.nightlabs.base.part.ControllablePart;
import org.nightlabs.base.part.PartVisibilityListener;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.login.part.LSDPartController;
import org.nightlabs.jfire.base.prop.StructLocalDAO;
import org.nightlabs.jfire.base.prop.edit.fieldbased.FieldBasedEditor;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.Property;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.id.StructLocalID;

public class BlockBasedEditorView 
extends ViewPart
implements PartVisibilityListener, ControllablePart
{
	private BlockBasedEditor blockBasedEditor;
	private ExpandableBlocksEditor expandableBlocksEditor;
	private FieldBasedEditor fieldBasedEditor;
	
	public BlockBasedEditorView() {
		LSDPartController.sharedInstance().registerPart(this);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		// Delegate this to the view-controller, to let him decide what to display
		LSDPartController.sharedInstance().createPartControl(this, parent);
	}

	@Override
	public void setFocus() {
	}

	public void partHidden(IWorkbenchPartReference partRef) {
	}

	public void partVisible(IWorkbenchPartReference partRef) {
	}

	public boolean canDisplayPart() {
		return Login.isLoggedIn();
	}

	public void createPartContents(Composite parent) {
		StructLocal structLocal = StructLocalDAO.sharedInstance().getStructLocal(StructLocalID.create("chezfrancois.jfire.org", Person.class.getName()));
		Property prop = new Property("chezfrancois.jfire.org", 1);
		structLocal.explodeProperty(prop);
		parent.setLayout(new GridLayout());
//		expandableBlocksEditor = new ExpandableBlocksEditor(prop, struct);
//		Control control = expandableBlocksEditor.createControl(parent, true);
//		fieldBasedEditor = new FieldBasedEditor();
//		fieldBasedEditor.setProp(prop, struct);
//		Control control = fieldBasedEditor.createControl(parent, true);
//		control.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		blockBasedEditor = new BlockBasedEditor(prop, structLocal);
		blockBasedEditor.createControl(parent, true);
		
	}
}
