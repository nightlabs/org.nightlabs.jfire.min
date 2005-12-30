/*
 * Created 	on Jan 22, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.edit.blockbased;

import org.eclipse.swt.widgets.Composite;

import org.nightlabs.jfire.person.PersonDataBlock;
import org.nightlabs.jfire.person.id.PersonStructBlockID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public interface PersonDataBlockEditorFactory {
	public PersonStructBlockID getProviderStructBlockID();
	public PersonDataBlockEditor createPersonDataBlockEditor(PersonDataBlock dataBlock, Composite parent, int style);
}
