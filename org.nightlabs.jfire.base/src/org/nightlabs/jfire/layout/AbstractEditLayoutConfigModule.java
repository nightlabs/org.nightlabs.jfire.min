package org.nightlabs.jfire.layout;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.clientui.layout.GridLayout;
import org.nightlabs.datastructure.Pair;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.util.CollectionUtil;

/**
 * The base class for any server-side stored layout information that uses the JFire
 * Client-UI-GridLayout to layout its parts.<br>
 * The parts layouted might be individual to a use-case and therefore have to be created by
 * implementations of this class in {@link #createEditLayoutEntry(String)}. <br>
 * Each ConfigModule defines a layout for a specific use-case. The use-case is encoded into the
 * {@link #getCfModID()}. It adheres to the following semantic: 
 * <i>cfModID = clienttype/useCaseName</i>. 
 * Hence <b>NO</b> '/' is allowed to appear in either of the cfModID's parts!
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 * @author Marius Heinzmann <!-- marius [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Layout_EditLayoutConfigModule")
@FetchGroups({
	@FetchGroup(
		name=AbstractEditLayoutConfigModule.FETCH_GROUP_GRID_LAYOUT,
		members=@Persistent(name="gridLayout")),
	@FetchGroup(
		name=AbstractEditLayoutConfigModule.FETCH_GROUP_EDIT_LAYOUT_ENTRIES,
		members=@Persistent(name="editLayoutEntries"))
})
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class AbstractEditLayoutConfigModule<O, E extends AbstractEditLayoutEntry<O>>
	extends ConfigModule
{
	private static final long serialVersionUID = 20100108L;

	public static final String CFMODID_SEPARATOR = "/";
	public static final String CLIENT_TYPE_RCP = "RCPClient";
	public static final String FETCH_GROUP_GRID_LAYOUT = "AbstractEditLayoutConfigModule.gridLayout";
	public static final String FETCH_GROUP_EDIT_LAYOUT_ENTRIES = "AbstractEditLayoutConfigModule.editLayoutEntries";

	/**
	 * Given an {@link AbstractEditLayoutConfigModule} extracts the client type as well as the use case of its cfModID.
	 *
	 * @param cfMod The ConfigModule to examine.
	 * @return The client type as well as the use case of the given ConfigModule's cfModID.
	 */
	public static Pair<String, String> getClientTypeAndUseCase(AbstractEditLayoutConfigModule<?,?> cfMod)
	{
		String[] splitted = cfMod.getCfModID().split(CFMODID_SEPARATOR);
		if (splitted.length > 2)
			throw new RuntimeException("The cfModID of an AbstractEditLayoutConfigModule contains a '"+CFMODID_SEPARATOR+
					"' in either its client-type name or the use-case name! CfMod=" + JDOHelper.getObjectId(cfMod));

		return new Pair<String, String>(splitted[0], splitted[1]);
	}

	/**
	 * Returns the cfModID for a given clientType and useCaseName according to the policy described in the javadoc of
	 * this class.
	 *
	 * @param clientType The type of client framework like {@link #CLIENT_TYPE_RCP}.
	 * @param useCaseName The name for the use case an {@link AbstractEditLayoutConfigModule} shall describe.
	 * @return The cfModID for a given clientType and useCaseName according to the policy described in the javadoc of
	 * this class.
	 */
	public static String getCfModID(String clientType, String useCaseName)
	{
		if (clientType.contains(CFMODID_SEPARATOR) || useCaseName.contains(CFMODID_SEPARATOR))
			throw new IllegalArgumentException("Either the given clientType or the useCaseName contains a '"+
					CFMODID_SEPARATOR+"', this is not allowed: clientType="+clientType+", useCaseName="+useCaseName);
		return clientType + CFMODID_SEPARATOR + useCaseName;
	}

	public static class FieldName {
		public static final String gridLayout = "gridLayout";
		public static final String editLayoutEntries = "editLayoutEntries";
	}

	/**
	 * Defines the top level layout (obviously grid-based).
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private GridLayout gridLayout;

	/**
	 * The list of entries that describe each cell defined by the {@link #gridLayout}.
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		mappedBy="configModule",
		persistenceModifier=PersistenceModifier.PERSISTENT,
		dependentElement="true")
	private List<AbstractEditLayoutEntry<?>> editLayoutEntries;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {
		gridLayout = new GridLayout(IDGenerator.nextID(GridLayout.class));
		editLayoutEntries = new LinkedList<AbstractEditLayoutEntry<?>>();
	}
	
	/**
	 * @return The GridLayout of this config-module.
	 */
	public GridLayout getGridLayout() {
		return gridLayout;
	}
	
	/**
	 * @return An unmodifiable list of all EditLayoutEntries of this config-module.
	 */
	public List<E> getEditLayoutEntries() {
		List<E> result = CollectionUtil.castList(editLayoutEntries);
		return Collections.unmodifiableList(result);
	}

	/**
	 * Create a new instance of an EditLayoutEntry that can be used as entry for this config-module.
	 * 
	 * @param entryType The entry-type for the new entry.
	 * @return A new instance of an EditLayoutEntry that can be used as entry for this
	 *         config-module.
	 */
	public abstract E createEditLayoutEntry(String entryType);

	/**
	 * Clears the list of EditLayoutEntries.
	 */
	public void clearEditLayoutEntries()
	{
		editLayoutEntries.clear();
	}

	/**
	 * Adds the given EditLayoutEntry to the list of EditLayoutEntries of this config-module.
	 * 
	 * @param editLayoutEntry The EditLayoutEntry to add.
	 */
	public void addEditLayoutEntry(E editLayoutEntry) {
		editLayoutEntries.add(editLayoutEntry);
	}

	/**
	 * Removes the given EditLayoutEntry from the list of EditLayoutEntries of this config-module.
	 * 
	 * @param editLayoutEntry The EditLayoutEntry to remove.
	 * @return Whether the given entry was contained and successfully removed from the list.
	 */
	public boolean removeEditLayoutEntry(E editLayoutEntry) {
		return editLayoutEntries.remove(editLayoutEntry);
	}

	/**
	 * Move the given EditLayoutEntry one position up in the list of EditLayoutEntries of this config-module.
	 * 
	 * @param editLayoutEntry The EditLayoutEntry to move.
	 * @return Whether the operation succeeded.
	 */
	public boolean moveEditLayoutEntryUp(E editLayoutEntry) {
		int idx = editLayoutEntries.indexOf(editLayoutEntry);
		if (idx <= 0)
			return false;
		Collections.swap(editLayoutEntries, idx-1, idx);
		return true;
	}

	/**
	 * Move the given EditLayoutEntry one position down in the list of EditLayoutEntries of this config-module.
	 * 
	 * @param editLayoutEntry The EditLayoutEntry to move.
	 * @return Whether the operation succeeded.
	 */
	public boolean moveEditLayoutEntryDown(E editLayoutEntry) {
		int idx = editLayoutEntries.indexOf(editLayoutEntry);
		if (idx < 0 || idx >= editLayoutEntries.size() -1)
			return false;
		Collections.swap(editLayoutEntries, idx, idx+1);
		return true;
	}
}
