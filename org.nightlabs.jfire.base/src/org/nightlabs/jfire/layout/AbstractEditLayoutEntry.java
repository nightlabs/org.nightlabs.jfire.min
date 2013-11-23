package org.nightlabs.jfire.layout;

import java.io.Serializable;

import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.clientui.layout.GridData;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.layout.id.AbstractEditLayoutEntryID;

/**
 * Base class for EditLayoutEntries that can be managed by {@link AbstractEditLayoutConfigModule}.
 * Implementations have to manage their individual configuration-information in
 * {@link #setObject(Object)} and {@link #getObject()}.<br>
 * The individual configuration-information will provide the the information for consumers what to
 * render inside the cell created for this entry. The configurable {@link GridData} (
 * {@link #getGridData()}) of this config-module, however, will define how the cell is layouted in
 * its parents layout.
 * 
 * @param <T> The type of individual information-object used for this entry.
 * 
 * @author Marius Heinzmann <!-- marius [AT] nightlabs [DOT] de -->
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
		objectIdClass = AbstractEditLayoutEntryID.class,
		identityType = IdentityType.APPLICATION,
		detachable = "true",
		table = "JFireBase_Layout_AbstractEditLayoutEntry")
@FetchGroups({
	@FetchGroup(
			fetchGroups = { "default" },
			name = AbstractEditLayoutEntry.FETCH_GROUP_GRID_DATA,
			members = @Persistent(name = "gridData")),
	@FetchGroup(
			name=AbstractEditLayoutEntry.FETCH_GROUP_CONFIGMODULE,
			members=@Persistent(name="editLayoutEntries"))
	})
@Discriminator(strategy = DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
public abstract class AbstractEditLayoutEntry<T>
	implements EditLayoutEntry<T>, Serializable
{
	private static final long serialVersionUID = 20100108L;

	public static final String	FETCH_GROUP_GRID_DATA			= "AbstractEditLayoutEntry.gridData";
	public static final String	FETCH_GROUP_OBJECT				= "AbstractEditLayoutEntry.object";
	public static final String	FETCH_GROUP_CONFIGMODULE	= "AbstractEditLayoutEntry.configModule";

	public static class FieldName
	{
		public static final String	gridData			= "gridData";
		public static final String	object				= "object";
		public static final String	identifier		= "identifier";
	}

	@PrimaryKey
	private long editLayoutEntryID;

	@SuppressWarnings("unused")
	@Persistent(persistenceModifier = PersistenceModifier.PERSISTENT)
	private AbstractEditLayoutConfigModule<T, ?>	configModule;

	@Persistent(persistenceModifier = PersistenceModifier.PERSISTENT)
	private String entryType;

	@Persistent(persistenceModifier = PersistenceModifier.PERSISTENT)
	private GridData gridData;

//	@Persistent(persistenceModifier = PersistenceModifier.PERSISTENT)
//	private T object;

	/**
	 * @deprecated Only for JDO
	 */
	@Deprecated
	protected AbstractEditLayoutEntry()
	{
	}

	/**
	 * Create a new {@link AbstractEditLayoutEntry}.
	 * 
	 * @param configModule The ConfigModule this entry is contained in.
	 * @param entryID The unique ID assigned to it.
	 * @param entryType The type of entry this element is representing.
	 */
	public AbstractEditLayoutEntry(AbstractEditLayoutConfigModule<T, ?> configModule,
			long entryID, String entryType)
	{
		this.configModule = configModule;
		this.editLayoutEntryID = entryID;
		if (entryType == null) throw new IllegalArgumentException("entryType must not be null");
		this.entryType = entryType;
		this.gridData = new GridData(IDGenerator.nextID(GridData.class));
	}

	public long getEditLayoutEntryID()
	{
		return editLayoutEntryID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GridData getGridData()
	{
		return gridData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setGridData(GridData gridData)
	{
		this.gridData = gridData;
	}

	/**
	 * @return The individual object that configures what should be shown in the cell created for
	 *         this entry.
	 */
	public abstract T getObject();

	/**
	 * Set the object that will give individual, use-case-dependent information about what to show
	 * in the cell created for this entry.
	 * 
	 * @param object The object to set.
	 */
	public abstract void setObject(T object);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEntryType()
	{
		return entryType;
	}
	
	/**
	 * @return A descriptive name of this entry to be displayed in lists of entries.
	 */
	public abstract String getName();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (editLayoutEntryID ^ (editLayoutEntryID >>> 32));
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractEditLayoutEntry other = (AbstractEditLayoutEntry) obj;
		if (editLayoutEntryID != other.editLayoutEntryID)
			return false;
		return true;
	}
}
