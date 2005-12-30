/*
 * Created 	on Nov 26, 2004
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.edit;

import org.eclipse.swt.widgets.Composite;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class AbstractPersonDataFieldComposite extends Composite { 
	public abstract void refresh();
	
	/**
	 * @param parent
	 * @param style
	 * @see Composite#Composite(org.eclipse.swt.widgets.Composite, int)
	 */
	public AbstractPersonDataFieldComposite(Composite parent, int style) {
		super(parent, style);
	}
}
