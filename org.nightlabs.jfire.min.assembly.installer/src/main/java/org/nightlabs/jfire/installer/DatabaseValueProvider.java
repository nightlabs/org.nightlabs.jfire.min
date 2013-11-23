package org.nightlabs.jfire.installer;

import java.util.Properties;

import org.nightlabs.installer.base.Installer;
import org.nightlabs.installer.base.defaults.DefaultValueProvider;
import org.nightlabs.jfire.servermanager.config.DatabaseCf;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DatabaseValueProvider extends DefaultValueProvider
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultValueProvider#getValues()
	 */
	@Override
	public Properties getValues()
	{
		Installer installer = getInstaller();
		String installataionPreset = installer.getResult("20_localServer.70_installationPresets.result"); //$NON-NLS-1$
		if(installataionPreset == null)
			return null;
		String db = null;
		if("JBossDerby".equals(installataionPreset))
			db = "Derby";
		else if("JBossMySQL".equals(installataionPreset))
			db = "MySQL";

		if(db == null)
			return null;

		DatabaseCf databaseCf = DatabaseCf.defaults().get(db);
		if(databaseCf == null) {
			System.err.println("WARNING: No defaults for database \"" + db + "\"!"); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}


		// since the configuration in DatabaseCf is relative to the bin-dir, we need to obtain it here.
//		File serverBinDir = new File(installer.getResult("20_localServer.50_deployBaseDir.result"), "../../../../bin"); //$NON-NLS-1$ //$NON-NLS-2$
//		serverBinDir = new File(IOUtil.simplifyPath(serverBinDir));
		//we use relative paths now - see https://www.jfire.org/modules/bugs/view.php?id=555

		Properties defaultValues = new Properties();
		defaultValues.setProperty("10_driverName_noTx.result", databaseCf.getDatabaseDriverName_noTx()); //$NON-NLS-1$
		defaultValues.setProperty("11_driverName_localTx.result", databaseCf.getDatabaseDriverName_localTx()); //$NON-NLS-1$
		defaultValues.setProperty("12_driverName_xa.result", databaseCf.getDatabaseDriverName_xa()); //$NON-NLS-1$
		defaultValues.setProperty("20_connectionURL.result", databaseCf.getDatabaseURL()); //$NON-NLS-1$
		defaultValues.setProperty("30_namePrefix.result", databaseCf.getDatabasePrefix()); //$NON-NLS-1$
		defaultValues.setProperty("40_nameSuffix.result", databaseCf.getDatabaseSuffix()); //$NON-NLS-1$
		defaultValues.setProperty("50_userName.result", databaseCf.getDatabaseUserName()); //$NON-NLS-1$
		defaultValues.setProperty("60_password.result", databaseCf.getDatabasePassword()); //$NON-NLS-1$
		defaultValues.setProperty("70_adapter.result", databaseCf.getDatabaseAdapter()); //$NON-NLS-1$
		defaultValues.setProperty("80_typeMapping.result", databaseCf.getDatasourceMetadataTypeMapping()); //$NON-NLS-1$
		defaultValues.setProperty("85_datasourceConfigurationFile.result", databaseCf.getDatasourceConfigFile()); //$NON-NLS-1$
//		defaultValues.setProperty("90_datasourceConfigurationTemplateFile.result", new File(serverBinDir, databaseCf.getDatasourceTemplateDSXMLFile()).getPath()); //$NON-NLS-1$
		defaultValues.setProperty("90_datasourceConfigurationTemplateFile.result", databaseCf.getDatasourceTemplateDSXMLFile()); //$NON-NLS-1$

		return defaultValues;
	}
}
