/*
 * Created 	on Dec 15, 2004
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.search;

import java.util.Collection;

import javax.jdo.FetchPlan;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.nightlabs.base.composite.XComposite;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.person.PersonManager;
import org.nightlabs.jfire.person.PersonManagerHome;
import org.nightlabs.jfire.person.PersonManagerUtil;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.person.TextPersonDataField;
import org.nightlabs.jfire.person.util.PersonSearchFilter;
import org.nightlabs.j2ee.InitialContextProvider;
import org.nightlabs.jdo.search.SearchFilterProvider;
import org.nightlabs.jdo.search.SearchResultFetcher;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonSearchDialog extends Dialog implements SearchResultFetcher{

	private static final Logger LOGGER = Logger.getLogger(PersonSearchDialog.class);
	/**
	 * @param parentShell
	 */
	public PersonSearchDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.RESIZE);
	}
	
	
	private DynamicPersonSearchFilterProvider filterProvider;
	private PersonSearchResultTable resultTable;

	protected Control createDialogArea(Composite parent) {
		
		XComposite wrapper = new XComposite(parent, SWT.NONE, XComposite.LAYOUT_MODE_TIGHT_WRAPPER);
		
		filterProvider = new DynamicPersonSearchFilterProvider(new PersonSearchFilterItemListMutator());
		try {
			filterProvider.setSearchResultFetcher(this,Login.getLogin());
		} catch (LoginException e) {
			LOGGER.error("Error logging in ",e);
			throw new RuntimeException(e);
		}
		Composite providerComp = filterProvider.createComposite(wrapper);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 550;
		gd.heightHint = 200;
		providerComp.setLayoutData(gd);
		
		resultTable = new PersonSearchResultTable(wrapper,SWT.NONE	);		
		GridData tgd = new GridData(GridData.FILL_BOTH);
		tgd.heightHint = 300;
		resultTable.setLayoutData(tgd);
		
		return wrapper;
	}

	/**
	 * @see org.nightlabs.jdo.search.SearchResultFetcher#searchTriggered(org.nightlabs.jdo.search.SearchFilterProvider, org.nightlabs.j2ee.InitialContextProvider)
	 */
	public void searchTriggered(SearchFilterProvider filterProvider, InitialContextProvider login) {
		LOGGER.debug("Search triggered, getting PersonManager");
    PersonManagerHome home = null;
    PersonManager personManager = null;
    try {
    	// TODO: find reason of IncompatibleClassChangeError when accessing login
//    	 home = PersonManagerUtil.getHome(login.getInitialContextProperties());
      home = PersonManagerUtil.getHome(Login.getLogin().getInitialContextProperties());
      personManager = home.create();
    } catch (Exception e) {
    	LOGGER.error("Error creating PersonManagerUtil.",e);
    	throw new RuntimeException(e);
    }
		LOGGER.debug("Have PersonManager searching");
		
		PersonSearchFilter searchFilter = (PersonSearchFilter)filterProvider.getSearchFilter();
		searchFilter.clearResultFields();
		searchFilter.addResultPersonStructFieldID(TextPersonDataField.class, PersonStruct.PERSONALDATA_COMPANY);
		searchFilter.addResultPersonStructFieldID(TextPersonDataField.class, PersonStruct.PERSONALDATA_NAME);
		searchFilter.addResultPersonStructFieldID(TextPersonDataField.class, PersonStruct.PERSONALDATA_FIRSTNAME);
		searchFilter.addResultPersonStructFieldID(TextPersonDataField.class, PersonStruct.PHONE_LOCALNUMBER);
		
		try {
			long start = System.currentTimeMillis();
			Collection persons = personManager.searchPerson(searchFilter, new String[] {FetchPlan.ALL});
			LOGGER.debug("Person search for "+persons.size()+" entries took "+(System.currentTimeMillis()-start)/1000+" s.");
			start = System.currentTimeMillis();
			resultTable.setInput(persons);
			LOGGER.debug("Setting results to table for "+persons.size()+" entries took "+(System.currentTimeMillis()-start)/1000+" s.");
		} catch (Exception e) {
			LOGGER.error("Error searching person.",e);
			throw new RuntimeException(e);
		}
		
	}
}
