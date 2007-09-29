package org.nightlabs.jfire.base.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.nightlabs.annotation.Implement;
import org.nightlabs.base.ui.part.ControllablePart;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.login.part.LSDPartController;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class LSDPreferencePage 
extends PreferencePage
implements IWorkbenchPreferencePage, ControllablePart
{
	public LSDPreferencePage() {
		super();
		init();
	}
	
	public LSDPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
		init();
	}

	public LSDPreferencePage(String title) {
		super(title);
		init();
	}

	protected void init() {
		LSDPartController.sharedInstance().registerPart(this, new FillLayout());			
	}
	
	@Implement
	public boolean canDisplayPart() {
		return Login.isLoggedIn();
	}

	@Implement
	public void createPartContents(Composite parent) {
//		createContents(parent);
	}

	@Override
	protected Control createContents(Composite parent) 
	{
		LSDPartController.sharedInstance().createPartControl(this, parent);
		return parent;
	}
	
	private IWorkbench workbench;
	public IWorkbench getWorkbench() {
		return workbench;
	}
	
	@Implement
	public void init(IWorkbench workbench) {
		this.workbench = workbench;
	}
	
}
