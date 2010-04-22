package org.nightlabs.jfire.prop.view;

import java.io.Serializable;

import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.search.config.PropertySetSearchEditLayoutConfigModule;
import org.nightlabs.jfire.prop.view.id.PropertySetViewerConfigurationID;

/**
 * Base-class for configurations of a {@link PropertySet}-Viewers (like IPropertySetViewer in the
 * RCP client). Instance of this type can be stored as viewer-configuration in for a viewer in a 
 * {@link PropertySetSearchEditLayoutConfigModule}. These are used to configure the search-fields
 * and result-viewer for different use-cases of a PropertySet-search. 
 * 
 * @author Tobias Langner
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
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
	
	/**
	 * Create a new {@link PropertySetViewerConfiguration}.
	 * 
	 * @param organisationID organisationID primary-key field.
	 * @param configurationID configurationID primary-key field.
	 */
	protected PropertySetViewerConfiguration(String organisationID, long configurationID) {
		this.organisationID = organisationID;
		this.configurationID = configurationID;
	}
	
	/**
	 * @return The organisationID primary-key field.
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	
	/**
	 * @return The configurationID primary-key field.
	 */
	public long getConfigurationID() {
		return configurationID;
	}
}
