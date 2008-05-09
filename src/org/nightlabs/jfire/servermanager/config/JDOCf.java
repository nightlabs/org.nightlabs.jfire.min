package org.nightlabs.jfire.servermanager.config;

import java.io.Serializable;

/**
 * The server core JDO configuration
 * @author Marco Schulze
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class JDOCf extends JFireServerConfigPart implements Serializable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * LOG4J logger used by this class.
	 */
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(JDOCf.class);
	
	private String jdoDeploymentDirectory;

	private String jdoDeploymentDescriptorFile;
	private String jdoDeploymentDescriptorTemplateFile;

	private String jdoPersistenceConfigurationFile;
	private String jdoPersistenceConfigurationTemplateFile;

	@Override
	public void init()
	{
		if (jdoDeploymentDirectory == null)
			setJdoDeploymentDirectory("../server/default/deploy/JFire_JDO_" + JFireServerConfigModule.ORGANISATION_ID_VAR + ".last/");

		if (jdoDeploymentDescriptorFile == null)
			jdoDeploymentDescriptorFile = "jdo-" + JFireServerConfigModule.ORGANISATION_ID_VAR + "-ds.xml";

		if (jdoDeploymentDescriptorTemplateFile == null)
			jdoDeploymentDescriptorTemplateFile = "../server/default/deploy/JFire.last/JFireBase.ear/jdo-datanucleus-1.0-ds.template.xml";

		if (jdoPersistenceConfigurationFile == null)
			jdoPersistenceConfigurationFile = "persistence-" + JFireServerConfigModule.ORGANISATION_ID_VAR + ".xml";

		if (jdoPersistenceConfigurationTemplateFile == null)
			jdoPersistenceConfigurationTemplateFile = "../server/default/deploy/JFire.last/JFireBase.ear/jdo-datanucleus-1.0-persistence.template.xml";

		logger.info("jdoDeploymentDirectory = "+jdoDeploymentDirectory);
		logger.info("jdoDeploymentDescriptorFile = "+jdoDeploymentDescriptorFile);
		logger.info("jdoDeploymentDescriptorTemplateFile = "+jdoDeploymentDescriptorTemplateFile);
		logger.info("jdoPersistenceConfigurationFile = "+jdoPersistenceConfigurationFile);
		logger.info("jdoPersistenceConfigurationTemplateFile = "+jdoPersistenceConfigurationTemplateFile);
	}

	/**
	 * @return Returns the jdoDeploymentDirectory.
	 */
	public String getJdoDeploymentDirectory()
	{
		return jdoDeploymentDirectory;
	}

	public String getJdoConfigDirectory(String organisationID)
	{
		if (organisationID == null || "".equals(organisationID))
			throw new IllegalArgumentException("organisationID must not be null or empty string!");

		return jdoDeploymentDirectory.replace(JFireServerConfigModule.ORGANISATION_ID_VAR, organisationID);
	}
	
	/**
	 * @param jdoDeploymentDirectory The jdoDeploymentDirectory to set.
	 */
	public void setJdoDeploymentDirectory(String jdoConfigDirectory)
	{
		if (jdoConfigDirectory == null)
			throw new IllegalArgumentException("jdoDeploymentDirectory must not be null!");

		if (jdoConfigDirectory.indexOf(JFireServerConfigModule.ORGANISATION_ID_VAR) < 0)
			throw new IllegalArgumentException("jdoDeploymentDirectory must contain \"" + JFireServerConfigModule.ORGANISATION_ID_VAR + "\"!");

		this.jdoDeploymentDirectory = jdoConfigDirectory;
		setChanged();
	}

	public String getJdoDeploymentDescriptorFile()
	{
		return jdoDeploymentDescriptorFile;
	}
	
	public String getJdoDeploymentDescriptorFile(String organisationID)
	{
		if (organisationID == null || "".equals(organisationID))
			throw new IllegalArgumentException("organisationID must not be null or empty string!");

		return jdoDeploymentDescriptorFile.replace(JFireServerConfigModule.ORGANISATION_ID_VAR, organisationID);
	}

	public void setJdoDeploymentDescriptorFile(String jdoConfigFile)
	{
		if (jdoConfigFile == null)
			throw new IllegalArgumentException("jdoDeploymentDescriptorFile must not be null!");

		if (jdoConfigFile.indexOf(JFireServerConfigModule.ORGANISATION_ID_VAR) < 0)
			throw new IllegalArgumentException("jdoDeploymentDescriptorFile must contain \"" + JFireServerConfigModule.ORGANISATION_ID_VAR + "\"!");

		this.jdoDeploymentDescriptorFile = jdoConfigFile;
		setChanged();
	}

	/**
	 * @return Returns the jdoDeploymentDescriptorTemplateFile.
	 */
	public String getJdoDeploymentDescriptorTemplateFile()
	{
		return jdoDeploymentDescriptorTemplateFile;
	}
	
	/**
	 * @param jdoDeploymentDescriptorTemplateFile The jdoDeploymentDescriptorTemplateFile to set.
	 */
	public void setJdoDeploymentDescriptorTemplateFile(String jdoTemplateDSXMLFile)
	{
		this.jdoDeploymentDescriptorTemplateFile = jdoTemplateDSXMLFile;
		setChanged();
	}

	public String getJdoPersistenceConfigurationFile(String organisationID)
	{
		if (organisationID == null || "".equals(organisationID))
			throw new IllegalArgumentException("organisationID must not be null or empty string!");

		return jdoPersistenceConfigurationFile.replace(JFireServerConfigModule.ORGANISATION_ID_VAR, organisationID);
	}
	public String getJdoPersistenceConfigurationFile() {
		return jdoPersistenceConfigurationFile;
	}
	public void setJdoPersistenceConfigurationFile(String jdoPersistenceConfigurationFile) {
		if (jdoPersistenceConfigurationFile == null)
			jdoPersistenceConfigurationFile = "";

		this.jdoPersistenceConfigurationFile = jdoPersistenceConfigurationFile;
		setChanged();
	}
	public String getJdoPersistenceConfigurationTemplateFile() {
		return jdoPersistenceConfigurationTemplateFile;
	}
	public void setJdoPersistenceConfigurationTemplateFile(String template) {
		if (template == null)
			template = "";

		this.jdoPersistenceConfigurationTemplateFile = template;
		setChanged();
	}
}