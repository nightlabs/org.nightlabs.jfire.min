package org.nightlabs.jfire.prop.search.config;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.config.PropertySetEditLayoutConfigModule;
import org.nightlabs.jfire.prop.config.PropertySetEditLayoutEntry;

/**
 * This entry is used for defining the layout of search-fields in a PropertySet-search and
 * additionally to the StructFields that should be searched it stored the match-type that should be
 * used when searching in those fields. A Match-type defines how a search string (or other criteria)
 * should be matched against the Data of for StructField.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireBase_Prop_StructFieldSearchEditLayoutEntry")
	@FetchGroups({
		@FetchGroup(
			fetchGroups={"default"},
			name=StructFieldSearchEditLayoutEntry.FETCH_GROUP_MATCH_TYPE,
			members=@Persistent(name="matchType"))
	})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class StructFieldSearchEditLayoutEntry extends PropertySetEditLayoutEntry {
	
	private static final long serialVersionUID = 20100119L;

	public static final String FETCH_GROUP_MATCH_TYPE = "StructFieldSearchEditLayoutEntry.matchType";
	
	/**
	 * Create a new {@link StructFieldSearchEditLayoutEntry}.
	 * 
	 * @param configModule The config-module the entry is for.
	 * @param entryID The id of the new entry.
	 * @param entryType The type of the new entry.
	 */
	public StructFieldSearchEditLayoutEntry(PropertySetEditLayoutConfigModule configModule, long entryID, String entryType) {
		super(configModule, entryID, entryType);
	}

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private MatchType matchType;

	/**
	 * Set the {@link MatchType} to use for the search created from this entry.
	 * 
	 * @param matchType The {@link MatchType} to set.
	 */
	public void setMatchType(MatchType matchType) {
		this.matchType = matchType;
	}

	/**
	 * Get the {@link MatchType} that should be used for the search created from this entry.
	 * 
	 * @return The {@link MatchType} that should be used for the search created from this entry.
	 */
	public MatchType getMatchType() {
		return matchType;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return Uses the super-implementation to get the names of the StuctFields and appends the
	 *         localized name of the match-type.
	 */
	@Override
	public String getName() {
		StringBuilder name = new StringBuilder(super.getName());
		if (getMatchType() != null)
			name.append("[").append(getMatchType().getLocalisedName()).append("]");
		return name.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(StructFieldSearchEditLayoutEntry.class.getName()).append(" {\n");
		
		for (StructField field : getStructFields()) {
			sb.append("\t").append(field.getStructFieldIDObj().toString()).append("\n");
		}
		sb.append("}");
		if (getMatchType() != null) {
			sb.append(", MatchType: ").append(getMatchType().getLocalisedName());
		}
		
		return sb.toString();
	}
}
