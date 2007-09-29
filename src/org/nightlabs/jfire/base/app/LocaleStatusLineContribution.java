/**
 * 
 */
package org.nightlabs.jfire.base.app;

import java.util.Locale;

import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.action.AbstractContributionItem;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.language.LanguageManager;

/**
 * StatusLine Contribution that displays the current Locale to the user.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class LocaleStatusLineContribution 
extends AbstractContributionItem
{
	private XComposite wrapper;
	private Label image;
	private Label text;
	
	public LocaleStatusLineContribution(String name, boolean fillToolBar, boolean fillCoolBar, boolean fillMenuBar, boolean fillComposite) {
		super(LocaleStatusLineContribution.class.getName(), name, fillToolBar, fillCoolBar, fillMenuBar, fillComposite);
		init();
	}

	public LocaleStatusLineContribution(String name) {
		super(LocaleStatusLineContribution.class.getName(), name);
		init();
	}
	
	private void init() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.action.AbstractContributionItem#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createControl(Composite parent) {
		wrapper = new XComposite(parent, SWT.NONE);
		StatusLineLayoutData layoutData = new StatusLineLayoutData();
		layoutData.widthHint = 100;
		wrapper.setLayoutData(layoutData);
		wrapper.getGridLayout().numColumns = 2;
		wrapper.getGridLayout().makeColumnsEqualWidth = false;
		image = new Label(wrapper, SWT.ICON);
		image.setImage(LanguageManager.sharedInstance().getFlag16x16Image(Locale.getDefault().getLanguage())); //$NON-NLS-1$
		image.setLayoutData(new GridData());
		text = new Label(wrapper, SWT.NONE);		
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.setText(Locale.getDefault().getDisplayLanguage());
		return wrapper;
	}

}
