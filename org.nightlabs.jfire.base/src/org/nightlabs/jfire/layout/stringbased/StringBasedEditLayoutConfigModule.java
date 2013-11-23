package org.nightlabs.jfire.layout.stringbased;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;

/**
 * ConfigModule that stores a layout of arbitrary parts (EditLayoutEntries) that are referenced by
 * an identifier-string. The type of EditLayoutEntry it uses is {@link StringBasedEditLayoutEntry}.
 * 
 * @author Marius Heinzmann <!-- marius [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class StringBasedEditLayoutConfigModule
	extends AbstractEditLayoutConfigModule<String, StringBasedEditLayoutEntry>
{
	private static final long serialVersionUID = 20100108L;

	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates {@link StringBasedEditLayoutEntry}s.
	 * </p>
	 */
	@Override
	public StringBasedEditLayoutEntry createEditLayoutEntry(String entryType)
	{
		return new StringBasedEditLayoutEntry(this,  IDGenerator.nextID(StringBasedEditLayoutEntry.class), entryType);
	}

}
