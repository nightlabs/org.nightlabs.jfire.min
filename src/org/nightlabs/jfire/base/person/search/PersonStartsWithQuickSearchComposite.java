/*
 * Created 	on Dec 21, 2004
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.search;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import org.nightlabs.jfire.base.login.Login;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonStartsWithQuickSearchComposite extends Composite {

	/**
	 * @param parent
	 * @param style
	 */
	public PersonStartsWithQuickSearchComposite(Composite arg0, int arg1) {
		super(arg0, arg1);
		try {
			Login.getLogin();
			
			GridLayout layout = new GridLayout();
			layout.horizontalSpacing = 0;
			layout.verticalSpacing = 0;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			setLayout(layout);
			
			for (int i=97; i<=122; i++) {
				String ch;
				ch = new String(new byte[]{(byte)i}, "UTF8");
				
				PersonStartsWithQuickSearch pswqs = new PersonStartsWithQuickSearch(null,ch);				
				pswqs.createComposite(this);								
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

}
