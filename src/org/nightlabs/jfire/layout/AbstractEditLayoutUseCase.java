package org.nightlabs.jfire.layout;

import java.io.Serializable;
import java.util.Collection;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.layout.id.AbstractEditLayoutUseCaseID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutUseCase;
import org.nightlabs.util.Util;

/**
 * Instances of this class represent a persistent configured of a
 * layout of the UI for a given use case.
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
@PersistenceCapable(
		objectIdClass=AbstractEditLayoutUseCaseID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireBase_AbstractEditLayoutUseCase"
	)
	@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
	@FetchGroups({
		@FetchGroup(
			name=AbstractEditLayoutUseCase.FETCH_GROUP_NAME,
			fetchGroups={FetchPlan.DEFAULT},
			members=@Persistent(name=AbstractEditLayoutUseCase.FieldName.name)
		),
		@FetchGroup(
				name=PropertySetFieldBasedEditLayoutUseCase.FETCH_GROUP_DESCRIPTION,
				members=@Persistent(name=AbstractEditLayoutUseCase.FieldName.description)
		)
	})
	@Queries(
			@javax.jdo.annotations.Query(name="getAllUseCaseIDs", value="SELECT JDOHelper.getObjectId(this)")
	)
public class AbstractEditLayoutUseCase implements Serializable {

	private static final long serialVersionUID = 20101801L;

	public static class FieldName {
		public static final String name = "name";
		public static final String description = "description";
		public static final String structLocal = "structLocal";
		public static final String structLocalID = "structLocalID";
	}

	public static final String FETCH_GROUP_NAME = "AbstractEditLayoutUseCase.name";
	public static final String FETCH_GROUP_DESCRIPTION = "AbstractEditLayoutUseCase.description";
	public static final String FETCH_GROUP_STRUCT_LOCAL = "AbstractEditLayoutUseCase.structLocal";
	public static final String FETCH_GROUP_STRUCT_LOCAL_ID = "AbstractEditLayoutUseCase.structLocalID";

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=50)
	private String useCaseID;

	@Persistent(
		mappedBy="useCase",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private EditLayoutUseCaseName name;

	@Persistent(
		mappedBy="useCase",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private EditLayoutUseCaseDescription description;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	public AbstractEditLayoutUseCase() {
	}

	public AbstractEditLayoutUseCase(String organisationID, String useCaseID) {
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		ObjectIDUtil.assertValidIDString(useCaseID, "useCaseID");
		this.useCaseID = useCaseID;
		this.name = new EditLayoutUseCaseName(this);
		this.description = new EditLayoutUseCaseDescription(this);
	}

	/**
	 * Get the organisationID of this {@link AbstractEditLayoutUseCase}.
	 * @return The organisationID of this {@link AbstractEditLayoutUseCase}.
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * Get the useCaseID of this {@link AbstractEditLayoutUseCase}.
	 * @return The useCaseID of this {@link AbstractEditLayoutUseCase}.
	 */
	public String getUseCaseID() {
		return useCaseID;
	}

	/**
	 * Get the name of this {@link AbstractEditLayoutUseCase}.
	 * @return The name of this {@link AbstractEditLayoutUseCase}.
	 */
	public EditLayoutUseCaseName getName() {
		return name;
	}

	/**
	 * Get the description of this {@link AbstractEditLayoutUseCase}.
	 * @return The description of this {@link AbstractEditLayoutUseCase}.
	 */
	public EditLayoutUseCaseDescription getDescription() {
		return description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((useCaseID == null) ? 0 : useCaseID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final AbstractEditLayoutUseCase other = (AbstractEditLayoutUseCase) obj;
		return (
				Util.equals(this.organisationID, other.organisationID) &&
				Util.equals(this.useCaseID, other.useCaseID)
		);
	}

	public static Collection<AbstractEditLayoutUseCaseID> getAllUseCaseIDs(PersistenceManager pm) {
		Query q = pm.newNamedQuery(AbstractEditLayoutUseCase.class, "getAllUseCaseIDs");
		return (Collection<AbstractEditLayoutUseCaseID>) q.execute();
	}
}
