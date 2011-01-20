package org.nightlabs.jfire.testsuite.base.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigGroup;
import org.nightlabs.jfire.config.ConfigManagerRemote;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.WorkstationConfigSetup;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.config.id.ConfigSetupID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.testsuite.JFireTestSuite;
import org.nightlabs.jfire.testsuite.TestCase;


/**
 *
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 *
 */

public class ConfigTestCase extends TestCase {
	Logger logger = Logger.getLogger(ConfigTestCase.class);	
	
	
	@Override
	protected String canRunTest(PersistenceManager pm) throws Exception
	{
		String className = "org.nightlabs.jfire.editlock.EditLock";
		try {
			Class.forName(className);
		} catch (ClassNotFoundException x) {
			return "The module JFireBase seems not to be installed (Class \"" + className + "\" could not be found)!";
		}
		return null;
	}
	
	@Test
	public void testWorkstationConfigSetup() throws Exception	
	{			
		logger.info("test WorkstationConfigSetup: Begin");

		// REV Alex: Sorry, I don't understand this test. What is it supposed to
		// do? The method-name implies it tests the Workstation-ConfigSetup.
		// What exactly does it test?
		
		ConfigManagerRemote configManager = JFireEjb3Factory.getRemoteBean(ConfigManagerRemote.class, SecurityReflector.getInitialContextProperties());
		
		String configGroupName = "workstationtest";
		String configGroupKey = ObjectIDUtil.makeValidIDString(configGroupName, true);

		ConfigID configID = ConfigID.create(
				SecurityReflector.getUserDescriptor().getOrganisationID(),
				configGroupKey,
				WorkstationConfigSetup.CONFIG_GROUP_CONFIG_TYPE_WORKSTATION_CONFIG);
		
		ConfigGroup  configGroup = configManager.addConfigGroup(
				configID.configKey,
				configID.configType,
				configGroupName,
				true,
				null, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT
		);
					
		Collection<ConfigSetupID> setupIDs = configManager.getAllConfigSetupIDs();
		List<ConfigSetup> configSetups = configManager.getConfigSetups(new HashSet<ConfigSetupID>(setupIDs),
				new String[] {FetchPlan.DEFAULT,ConfigSetup.FETCH_GROUP_CONFIG_MODULE_CLASSES, Config.FETCH_GROUP_CONFIG_GROUP}, 
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		
		
		
		
		
		for (ConfigSetup configSetup : configSetups) {
			if (configSetup.getConfigGroupType().equals(configID.configType)) 
			{		
				configSetup = (WorkstationConfigSetup)configSetup;
				List<Config> notAssignedConfigs = configSetup.getConfigsNotInGroup(configID.configKey);
				Set<String> modules = configSetup.getConfigModuleClasses();
				
				if(notAssignedConfigs.isEmpty())
				{
					logger.info("the following configs has been found");
				}			
				
			//	List<Config> configs = configSetup.getConfigsForGroup(configID.configKey);
				
//				Collection<Config> configs = configSetup.getConfigs();
//				
//				
//				List<Config> notAssignedconfigs = configSetup.getConfigsNotInGroup(configID.configKey);
//
//				if(!configs.isEmpty())
//				{
//					logger.info("the following configs has been found");
//
//				}
				logger.info("the following configs has been found");
//				Random rndGen = new Random(System.currentTimeMillis());		
//
//				// assign 3 random configs
//				for (int i =0;i<3;i++) {
//					configSetup.moveConfigToGroup(notAssignedconfigs.get(rndGen.nextInt(notAssignedconfigs.size())), configID.configKey);
//				}
//
//				configManager.storeConfigSetup(configSetup.getModifiedConfigs());

			}
			logger.info("test WorkstationConfigSetup: End");

		}


	}

}
