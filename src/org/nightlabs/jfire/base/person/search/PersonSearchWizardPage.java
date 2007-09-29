/**
 * 
 */
package org.nightlabs.jfire.base.person.search;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.wizard.WizardHop;
import org.nightlabs.base.ui.wizard.WizardHopPage;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class PersonSearchWizardPage extends WizardHopPage {

	private PersonEditorWizardHop editorWizardHop;
	private PersonSearchComposite searchComposite;
	private String quickSearchText;
	private Person newPerson;
	
	public PersonSearchWizardPage(String quickSearchText) {
		super(
			PersonSearchWizardPage.class.getName(),
			Messages.getString("org.nightlabs.jfire.base.person.search.PersonSearchWizardPage.title") //$NON-NLS-1$
		);		
		this.quickSearchText = quickSearchText;
		new WizardHop(this);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Control createPageContents(Composite parent) {
		searchComposite = new PersonSearchComposite(parent, SWT.NONE, quickSearchText);
		Composite buttonBar = searchComposite.getButtonBar();
		GridLayout gl = new GridLayout();
		XComposite.configureLayout(LayoutMode.LEFT_RIGHT_WRAPPER, gl);
		gl.numColumns = 3;
		buttonBar.setLayout(gl);
		
		Button createNewButton = new Button(buttonBar, SWT.PUSH);
		createNewButton.setText(getCreateNewButtonText());
		createNewButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		createNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (newPerson == null) {
					newPerson = new Person(
						SecurityReflector.getUserDescriptor().getOrganisationID(),
						IDGenerator.nextID(PropertySet.class)
					);
					StructLocal structLocal = StructLocalDAO.sharedInstance().getStructLocal(Person.class, StructLocal.DEFAULT_SCOPE, new NullProgressMonitor());
					structLocal.explodePropertySet(newPerson);
					editorWizardHop = new PersonEditorWizardHop();
					editorWizardHop.initialise(newPerson);
				}
				getWizardHop().addHopPage(editorWizardHop.getEntryPage());
				getContainer().updateButtons();
				getContainer().showPage(getNextPage());
			}
		});
		
		new XComposite(buttonBar, SWT.NONE, LayoutDataMode.GRID_DATA_HORIZONTAL);
		
		searchComposite.createSearchButton(buttonBar);
		searchComposite.getResultTable().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				getContainer().updateButtons();
			}
		});
		return searchComposite;
	}

	@Override
	public void onShow() {
		getWizardHop().removeAllHopPages();
		getContainer().updateButtons();
	}
	
	@Override
	public boolean isPageComplete() {
		if (this.equals(getContainer().getCurrentPage())) {
			return searchComposite.getResultTable().getFirstSelectedElement() != null;
		}
		return true;
	}
	
	/**
	 * @return Either the Person selected in the table in the first page
	 * or the newly created Person.
	 */
	public Person getSelectedPerson() {
		if (getWizard().getContainer().getCurrentPage() == this) {
			return searchComposite.getResultTable().getFirstSelectedElement();
		} else {
			return newPerson;
		}
	}
	
	protected String getCreateNewButtonText() {
		return Messages.getString("org.nightlabs.jfire.base.person.search.PersonSearchWizardPage.createNewButton.text"); //$NON-NLS-1$
	}
	
}
