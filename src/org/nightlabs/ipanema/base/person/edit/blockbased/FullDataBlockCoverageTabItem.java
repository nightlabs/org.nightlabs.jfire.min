/*
 * Created 	on Jan 25, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person.edit.blockbased;

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import org.nightlabs.ipanema.person.Person;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class FullDataBlockCoverageTabItem extends TabItem {

	public FullDataBlockCoverageTabItem(TabFolder parent, int style, String editorScope, Person person) {
		super(parent, style);
		FullDataBlockCoverageComposite comp = new FullDataBlockCoverageComposite(parent, style, editorScope, person);
		setControl(comp);
	}

}
