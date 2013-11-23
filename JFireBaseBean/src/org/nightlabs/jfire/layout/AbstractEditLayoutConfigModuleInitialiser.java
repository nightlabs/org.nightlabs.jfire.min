package org.nightlabs.jfire.layout;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.nightlabs.datastructure.Pair;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.ConfigModuleInitialiser;

/**
 * Base class for all EditLayoutConfigModules that filters the given ConfigModules according to th e
 *
 * @author Marius Heinzmann <!-- marius [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireBase_Layout_ConfigModuleIntialiser")
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class AbstractEditLayoutConfigModuleInitialiser
	extends ConfigModuleInitialiser
{
	@Persistent
	private String clientType;

	@Persistent
	private String useCaseName;

	/**
	 * Create a new {@link ConfigModuleInitialiser} for the given organisation,
	 * with the given initialiser id and the type of ConfigModule it is linked to.
	 * The new initialiser will have the default priority of 500 (see {@link #PRIORITY_DEFAULT}).
	 *
	 * @param organisationID The organisation this initialiser is for.
	 * @param configModuleInitialiserID The id of this initialiser in the scope of the type of ConfigModule.
	 * @param configModuleClassName The type of ConfigModule this initialiser is for.
	 * 		Has to be the fully qualified class-name of the ConfigModule.
	 * @param useCaseName The name of the use case according to which the ConfigModules of the same type are filtered.
	 * @param clientType The type of client according to which the ConfigModules of the same type are filtered
	 *        (may be null).
	 */
	public AbstractEditLayoutConfigModuleInitialiser(
			String organisationID, String configModuleClassName,
			String configModuleInitialiserID, String useCaseName, String clientType)
	{
		super(organisationID, configModuleClassName, configModuleInitialiserID);
		assert useCaseName != null && useCaseName.trim().length() > 0;
		this.clientType = clientType;
		this.useCaseName = useCaseName;
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
	 * @param useCaseName The name of the use case according to which the ConfigModules of the same type are filtered.
	 * @param clientType The type of client according to which the ConfigModules of the same type are filtered
	 *        (may be null).
	 */
	public AbstractEditLayoutConfigModuleInitialiser(
			String organisationID, String configModuleClassName,
			String configModuleInitialiserID, Integer priority, String useCaseName, String clientType)
	{
		super(organisationID, configModuleClassName, configModuleInitialiserID,
				priority);
		assert useCaseName != null && useCaseName.trim().length() > 0;
		this.clientType = clientType;
		this.useCaseName = useCaseName;
	}


	/**
	 * @return the clientType
	 */
	public String getClientType()
	{
		return clientType;
	}

	/**
	 * @return the useCaseName
	 */
	public String getUseCaseName()
	{
		return useCaseName;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigModuleInitialiser#initialiseConfigModule(javax.jdo.PersistenceManager, org.nightlabs.jfire.config.ConfigModule)
	 */
	@Override
	public void initialiseConfigModule(PersistenceManager pm, ConfigModule configModule)
	{
		if (!isConfigModuleMatching(configModule))
			return;

		final AbstractEditLayoutConfigModule<?,?> cfMod = (AbstractEditLayoutConfigModule<?,?>) configModule;
		initialiseLayoutConfigModule(cfMod);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigModuleInitialiser#initialiseDetachedConfigModule(org.nightlabs.jfire.config.ConfigModule)
	 */
	@Override
	public void initialiseDetachedConfigModule(ConfigModule configModule)
	{
		if (!isConfigModuleMatching(configModule))
			return;

		final AbstractEditLayoutConfigModule<?,?> cfMod = (AbstractEditLayoutConfigModule<?,?>) configModule;
		initialiseLayoutConfigModule(cfMod);
	}

	/**
	 * Here you should initialise the given {@link AbstractEditLayoutConfigModule} with the default values.
	 * <p>Be aware that the given ConfigModule might be detached and if so you should restore the default values. </p>
	 *
	 * @param cfMod The ConfigModule to set the default values on.
	 */
	protected abstract void initialiseLayoutConfigModule(AbstractEditLayoutConfigModule<?, ?> cfMod);

	/**
	 * Tests whether a given ConfigModule matches the filter criteria. Only an {@link AbstractEditLayoutConfigModule}
	 * with the matching useCase and, if set, matching client type yields a <code>true</code> as result.
	 *
	 * @param configModule The ConfigModule to check.
	 * @return <code>true</code> iff it is an {@link AbstractEditLayoutConfigModule} with the matching useCase and,
	 * if set, matching client type.
	 */
	protected boolean isConfigModuleMatching(ConfigModule configModule)
	{
//		This shouldn't be necessary as the Query fired by the ConfigModuleInitialiser already filters according to the
//		ConfigModuleClassName. (marius)
//		if (!(configModule instanceof AbstractEditLayoutConfigModule<?,?>))
//			return false;

		final AbstractEditLayoutConfigModule<?,?> cfMod = (AbstractEditLayoutConfigModule<?,?>) configModule;

		final Pair<String,String> clientTypeAndUseCase =
			AbstractEditLayoutConfigModule.getClientTypeAndUseCase(cfMod);

		if (! getUseCaseName().equals(clientTypeAndUseCase.getSecond()))
			return false;

		if (getClientType() != null && !getClientType().equals(clientTypeAndUseCase.getFirst()))
			return false;

		return true;
	}
}
