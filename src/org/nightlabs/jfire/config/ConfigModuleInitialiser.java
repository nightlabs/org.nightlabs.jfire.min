package org.nightlabs.jfire.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
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
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

import org.nightlabs.jfire.config.id.ConfigModuleInitialiserID;

/**
 * ConfigModuleInitaliser can be registered to a certain type (className)
 * of ConfigModule and will be invoked to initialise ConfigModules
 * when they are created.
 * <p>
 * Multiple initialisers can be registered for the same ConfigModule type.
 * Then all of them, ordered by their priority will be invoked.
 * </p>
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */@PersistenceCapable(
	objectIdClass=ConfigModuleInitialiserID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_ConfigModuleIntialiser")
@Version(strategy=VersionStrategy.VERSION_NUMBER)
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="ConfigModuleInitialiser.this",
		members={})
)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Queries(
	@javax.jdo.annotations.Query(
		name="getConfigModuleInitialiserForClass",
		value="SELECT WHERE organisationID == :pOrganisationID && configModuleClassName == :pConfigModuleClassName import java.lang.String")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class ConfigModuleInitialiser
{
	public static final Integer PRIORITY_DEFAULT = 500;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */	@PrimaryKey

	private String configModuleClassName;

	/**
	 * @jdo.field primary-key="true"
	 */	@PrimaryKey

	protected String configModuleInitialiserID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private Integer priority;

	/**
	 * @deprecated Only for JDO
	 */
	@Deprecated
	protected ConfigModuleInitialiser() {
	}

	/**
	 * Create a new {@link ConfigModuleInitialiser} for the given organisation,
	 * with the given initialiser id and the type of ConfigModule it is linked to.
	 * The new initialiser will have the default priority of 500 (see {@link #PRIORITY_DEFAULT}).
	 *
	 * @param organisationID The organisation this initialiser is for.
	 * @param configModuleInitialiserID The id of this initialiser in the scope of the type of ConfigModule.
	 * @param configModuleClassName The type of ConfigModule this initialiser is for.
	 * 		Has to be the fully qualified class-name of the ConfigModule.
	 */
	public ConfigModuleInitialiser(
			String organisationID,
			String configModuleClassName,
			String configModuleInitialiserID
	) {
		super();
		this.organisationID = organisationID;
		this.configModuleInitialiserID = configModuleInitialiserID;
		this.configModuleClassName = configModuleClassName;
		this.priority = PRIORITY_DEFAULT;
	}


	/**
	 * Create a new {@link ConfigModuleInitialiser} for the given organisation,
	 * with the given initialiser id and the type of ConfigModule it is linked to.
	 * The new initialiser will have the default priority of 500 (see {@link #PRIORITY_DEFAULT}).
	 *
	 * @param organisationID The organisation this initialiser is for.
	 * @param configModuleInitialiserID The id of this initialiser in the scope of the type of ConfigModule.
	 * @param configModuleClassName The type of ConfigModule this initialiser is for.
	 * 		Has to be the fully qualified class-name of the ConfigModule.
	 * @param priority The priority of this initialiser
	 */
	public ConfigModuleInitialiser(
			String organisationID,
			String configModuleClassName,
			String configModuleInitialiserID,
			Integer priority
	) {
		super();
		this.organisationID = organisationID;
		this.configModuleInitialiserID = configModuleInitialiserID;
		this.configModuleClassName = configModuleClassName;
		this.priority = priority;
	}

	/**
	 * Returns the configModuleClassName of this ConfigModuleInitialiser.
	 * @return the configModuleClassName.
	 */
	public String getConfigModuleClassName() {
		return configModuleClassName;
	}


	/**
	 * Returns the configModuleInitialiserID of this ConfigModuleInitialiser.
	 * @return the configModuleInitialiserID.
	 */
	public String getConfigModuleInitialiserID() {
		return configModuleInitialiserID;
	}


	/**
	 * Returns the organisationID of this ConfigModuleInitialiser.
	 * @return the organisationID.
	 */
	public String getOrganisationID() {
		return organisationID;
	}


	/**
	 * Returns the priority of this ConfigModuleInitialiser.
	 * @return the priority.
	 */
	public Integer getPriority() {
		return priority;
	}


	/**
	 * Sets the priority of this ConfigModuleInitialiser.
	 * @param priority the priority to set.
	 */
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	/**
	 * This method is called to initialise the given ConfigModule.
	 * It is invoked by the framework when ConfigModules are created
	 * (i.e. instantiated and right after made persistent for the first time),
	 * it is therefore called with an attached instance of ConfigModule.
	 *
	 * @param configModule The ConfigModule to intialise.
	 */
	public abstract void initialiseConfigModule(PersistenceManager pm, ConfigModule configModule);

	/**
	 * This method is also called to initialise a ConfigModule, but
	 * it is intended to be called with detached modules, possibly
	 * in some client to reset configuration.
	 *
	 * @param configModule The detached ConfigModule to initialise.
	 */
	public abstract void initialiseDetachedConfigModule(ConfigModule configModule);

	/**
	 * Get all {@link ConfigModuleInitialiser}s for the given ConfigModule sorted
	 * by their priority.
	 *
	 * @param pm The PersistenceManager to use.
	 * @param cfMod The ConfigModule to find initialisers for.
	 * @return All {@link ConfigModuleInitialiser}s for the given ConfigModule sorted
	 * 	by their priority.
	 */
	public static List<ConfigModuleInitialiser> getSortedInitialisersForConfigModule(
			PersistenceManager pm,
			ConfigModule cfMod
	) {
		return getSortedInitialisersForConfigModuleClass(pm, cfMod.getOrganisationID(), cfMod.getClass());
	}

	/**
	 * Get all {@link ConfigModuleInitialiser}s for the given ConfigModule sorted
	 * by their priority.
	 *
	 * @param pm The PersistenceManager to use.
	 * @param organisationID The orgainsationID of the initialiser (and ConfigModule)
	 * @param configModuleClass The fully qualified class-name of the ConfigModule to find initialisers for.
	 * @return All {@link ConfigModuleInitialiser}s for the given ConfigModule sorted
	 * 	by their priority.
	 */
	@SuppressWarnings("unchecked")
	public static List<ConfigModuleInitialiser> getSortedInitialisersForConfigModuleClass(
			PersistenceManager pm,
			String organisationID,
			Class<? extends ConfigModule> configModuleClass)
	{
		Query q = pm.newNamedQuery(ConfigModuleInitialiser.class, "getConfigModuleInitialiserForClass");

		List<ConfigModuleInitialiser> result = new ArrayList<ConfigModuleInitialiser>(
				(Collection<ConfigModuleInitialiser>) q.execute(organisationID, configModuleClass.getName()));

		Collections.sort(result, new Comparator<ConfigModuleInitialiser>() {
			public int compare(ConfigModuleInitialiser initialiser1, ConfigModuleInitialiser initialiser2) {
				return initialiser1.getPriority().compareTo(initialiser2.getPriority());
			}

		});
		return result;
	}
}
