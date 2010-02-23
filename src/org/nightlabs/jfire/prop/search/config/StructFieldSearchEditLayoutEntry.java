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
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutEntry2;

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
public class StructFieldSearchEditLayoutEntry extends PropertySetFieldBasedEditLayoutEntry2 {
	
	private static final long serialVersionUID = 20100119L;

	public static final String FETCH_GROUP_MATCH_TYPE = "StructFieldSearchEditLayoutEntry.matchType";
	
	public StructFieldSearchEditLayoutEntry(PropertySetEditLayoutConfigModule configModule, long entryID, String entryType) {
		super(configModule, entryID, entryType);
	}

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private MatchType matchType;
	
	public void setMatchType(MatchType matchType) {
		this.matchType = matchType;
	}
	
	public MatchType getMatchType() {
		return matchType;
	}
	
	@Override
	public String getName() {
		StringBuilder name = new StringBuilder(super.getName());
		if (getMatchType() != null)
			name.append("[").append(getMatchType().getLocalisedName()).append("]");
		return name.toString();
	}
	
	@Override
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
