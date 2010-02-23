package org.nightlabs.jfire.layout.stringbased;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;
import org.nightlabs.jfire.layout.AbstractEditLayoutEntry;

/**
 *
 * @author Marius Heinzmann <!-- marius [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
		identityType = IdentityType.APPLICATION,
		detachable = "true",
		table = "JFireBase_Layout_StringBasedEditLayoutEntry") // TODO
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class StringBasedEditLayoutEntry
	extends AbstractEditLayoutEntry<String>
{
	private static final long serialVersionUID = 20100108L;

	@Persistent
	private String objectString;

	/**
	 * @param configModule The ConfigModule this entry is contained in.
	 * @param entryID The unique ID assigned to it.
	 * @param entryType The type of entry this element is representing.
	 */
	public StringBasedEditLayoutEntry(
			AbstractEditLayoutConfigModule<String, ?> configModule,
			long entryID,
			String entryType)
	{
		super(configModule, entryID, entryType);
	}

	@Override
	public String getObject()
	{
		return objectString;
	}

	@Override
	public void setObject(String object)
	{
		this.objectString = object;
	}
	
	@Override
	public String getName() {
		// TODO Do something useful here.
		return "";
	}
}
