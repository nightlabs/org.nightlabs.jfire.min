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
import org.nightlabs.jfire.layout.id.AbstractEditLayoutEntryID;

/**
 *
 * @author Marius Heinzmann <!-- marius [AT] nightlabs [DOT] de -->
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
	}

	public long getEditLayoutEntryID()
	{
		return editLayoutEntryID;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.config.EditLayoutEntry#getGridData()
	 */
	public GridData getGridData()
	{
		return gridData;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.config.EditLayoutEntry#setGridData(org.nightlabs.clientui.layout.GridData)
	 */
	public void setGridData(GridData gridData)
	{
		this.gridData = gridData;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.layout.EditLayoutEntry#getObject()
	 */
	public abstract T getObject();
//	{
//		return object;
//	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.layout.EditLayoutEntry#setObject(java.lang.Object)
	 */
	public abstract void setObject(T object);
//	{
//		this.object = object;
//	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.config.EditLayoutEntry#getEntryType()
	 */
	public String getEntryType()
	{
		return entryType;
	}
	
	/**
	 * Return a descriptive name of this entry to be displayed in lists of entries.
	 */
	public abstract String getName();
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (editLayoutEntryID ^ (editLayoutEntryID >>> 32));
		return result;
	}

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
