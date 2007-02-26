package org.nightlabs.jfire.config.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.nightlabs.annotation.Implement;
import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.config.ConfigFactory;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.SecurityReflector.UserDescriptor;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.l10n.DateFormatter;
import org.nightlabs.l10n.NumberFormatter;

/**
 * This implementation of {@link ConfigFactory} allows to call <code>Config.sharedInstance()</code>
 * in the server and thus use {@link DateFormatter} and {@link NumberFormatter}. It creates/reads
 * one separate {@link Config} for every user.
 * <p>
 * Note, that you should still NOT use the XML config in the server!!! The two classes mentioned above
 * should be the only ones using the XML config! You should use the JFire-JDO-Config instead!!! See
 * https://www.jfire.org/modules/phpwiki/index.php/HowToServerSideConfiguration for details!
 * </p>
 *
 * @author Marco Schulze - Marco at NightLabs dot de
 */
public class XMLConfigFactory
extends ConfigFactory
{
	private Map<UserID, Config> userID2config = new HashMap<UserID, Config>();

	public XMLConfigFactory()
	{
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run()
			{
				synchronized (userID2config) {
					for (Config config : userID2config.values())
						config.save();
				}
			}
		});
	}

	@Implement
	public Config createSharedInstance(File configFile)
			throws ConfigException
	{
		throw new UnsupportedOperationException("Not supported!");
	}

	@Implement
	public Config createSharedInstance(File configFile, boolean loadConfFile)
			throws ConfigException
	{
		throw new UnsupportedOperationException("Not supported!");
	}

	@Implement
	public boolean isSharedInstanceExisting()
	{
		return true;
	}

	private File userConfigBaseDir = null;

	@Implement
	public Config sharedInstance(boolean throwExceptionIfNotExisting)
	{
		UserDescriptor userDescriptor = SecurityReflector.getUserDescriptor();
		UserID userID = UserID.create(userDescriptor.getOrganisationID(), userDescriptor.getUserID());
		synchronized (userID2config) {
			Config config = userID2config.get(userID);
			if (config == null) {
				if (userConfigBaseDir == null) {
					JFireServerManager jfsm = new Lookup(userID.organisationID).getJFireServerManagerFactory().getJFireServerManager();
					try {
						userConfigBaseDir = new File(jfsm.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory(),  "JFireBase.ear" + File.separatorChar + "user-config");
					} finally {
						jfsm.close();
					}
				}

				File configDir = new File(
						userConfigBaseDir,
						userID.organisationID + File.separatorChar + userID.userID);

				if (!configDir.exists())
					configDir.mkdirs();

				File configFile = new File(configDir, "config.xml");
				config = new Config(configFile);
				userID2config.put(userID, config);
			}
			return config;
		}
	}
}