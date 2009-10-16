package org.nightlabs.jfire.testsuite.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.jdo.FetchPlan;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigManagerRemote;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.WorkstationConfigSetup;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.config.id.ConfigSetupID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.testsuite.JFireTestSuite;


/**
 *
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 *
 */
@JFireTestSuite(JFireBaseConfigTestSuite.class)
public class ConfigTestCase extends TestCase{
	Logger logger = Logger.getLogger(ConfigTestCase.class);	

	@Test
	public void testWorkstationConfigSetup() throws Exception	
	{			
		logger.info("test WorkstationConfigSetup: Begin");

		ConfigManagerRemote configManager = JFireEjb3Factory.getRemoteBean(ConfigManagerRemote.class, SecurityReflector.getInitialContextProperties());
		
		String configGroupName = "workstationtest";
		String configGroupKey = ObjectIDUtil.makeValidIDString(configGroupName, true);

		ConfigID configID = ConfigID.create(
				SecurityReflector.getUserDescriptor().getOrganisationID(),
				configGroupKey,
				WorkstationConfigSetup.CONFIG_GROUP_CONFIG_TYPE_WORKSTATION_CONFIG);
		
		configManager.addConfigGroup(
				configID.configKey,
				configID.configType,
				configGroupName,
				false,
				null, 0
		);

		Collection<ConfigSetupID> setupIDs = configManager.getAllConfigSetupIDs();
		List<ConfigSetup> configSetups = configManager.getConfigSetups(new HashSet<ConfigSetupID>(setupIDs),
				new String[] {FetchPlan.DEFAULT, Config.FETCH_GROUP_CONFIG_GROUP}, 
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		for (ConfigSetup configSetup : configSetups) {
			if (configSetup.getConfigGroupType().equals(configID.configType)) 
			{
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
//				logger.info("the following configs has been found");
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
