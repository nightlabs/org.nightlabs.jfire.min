package org.nightlabs.jfire.prop.view;

import java.io.Serializable;

import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.prop.view.id.PropertySetViewerConfigurationID;

@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		table="JFireBase_Prop_PropertySetViewerConfiguration",
		objectIdClass=PropertySetViewerConfigurationID.class,
		detachable="true")
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public abstract class PropertySetViewerConfiguration implements Serializable {
	
	private static final long serialVersionUID = 20100217L;
	
	/**
	 * Extendors should declare this fetchgroup with all their members included.
	 */
	public static final String FETCH_GROUP_CONFIG_DATA = "PropertySetViewerConfiguration.configData";

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@PrimaryKey
	private String organisationID;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@PrimaryKey
	private long configurationID;
	
	protected PropertySetViewerConfiguration(String organisationID, long configurationID) {
		this.organisationID = organisationID;
		this.configurationID = configurationID;
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public long getConfigurationID() {
		return configurationID;
	}
}
