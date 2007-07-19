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
	
	private String jdoConfigDirectory;
	private String jdoConfigFile;
	private String jdoTemplateDSXMLFile;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.servermanager.config.JFireServerConfigPart#init()
	 */
	@Override
	public void init()
	{
		if (jdoConfigDirectory == null)
			setJdoConfigDirectory("../server/default/deploy/JFire_JDO_" + JFireServerConfigModule.ORGANISATION_ID_VAR + ".last/");

		if (jdoConfigFile == null)
			jdoConfigFile = "jdo-" + JFireServerConfigModule.ORGANISATION_ID_VAR + "-ds.xml";

		if (jdoTemplateDSXMLFile == null)
			jdoTemplateDSXMLFile = "../server/default/deploy/JFire.last/JFireBase.ear/jdo-jpox-1.2-ds.template.xml";

		logger.info("jdoConfigDirectory = "+jdoConfigDirectory);
		logger.info("jdoConfigFile = "+jdoConfigFile);
		logger.info("jdoTemplateDSXMLFile = "+jdoTemplateDSXMLFile);
	}
	
	/**
	 * @return Returns the jdoConfigDirectory.
	 */
	public String getJdoConfigDirectory() 
	{
		return jdoConfigDirectory;
	}
	
	public String getJdoConfigDirectory(String organisationID)
	{
		if (organisationID == null || "".equals(organisationID))
			throw new IllegalArgumentException("organisationID must not be null or empty string!");

		return jdoConfigDirectory.replace(JFireServerConfigModule.ORGANISATION_ID_VAR, organisationID);
	}
	
	/**
	 * @param jdoConfigDirectory The jdoConfigDirectory to set.
	 */
	public void setJdoConfigDirectory(String jdoConfigDirectory) 
	{
		if (jdoConfigDirectory == null)
			throw new IllegalArgumentException("jdoConfigDirectory must not be null!");

		if (jdoConfigDirectory.indexOf(JFireServerConfigModule.ORGANISATION_ID_VAR) < 0)
			throw new IllegalArgumentException("jdoConfigDirectory must contain \"" + JFireServerConfigModule.ORGANISATION_ID_VAR + "\"!");

		this.jdoConfigDirectory = jdoConfigDirectory;
		setChanged();
	}

	public String getJdoConfigFile()
	{
		return jdoConfigFile;
	}
	
	public String getJdoConfigFile(String organisationID)
	{
		if (organisationID == null || "".equals(organisationID))
			throw new IllegalArgumentException("organisationID must not be null or empty string!");

		return jdoConfigFile.replace(JFireServerConfigModule.ORGANISATION_ID_VAR, organisationID);
	}

	public void setJdoConfigFile(String jdoConfigFile)
	{
		if (jdoConfigFile == null)
			throw new IllegalArgumentException("jdoConfigFile must not be null!");

		if (jdoConfigFile.indexOf(JFireServerConfigModule.ORGANISATION_ID_VAR) < 0)
			throw new IllegalArgumentException("jdoConfigFile must contain \"" + JFireServerConfigModule.ORGANISATION_ID_VAR + "\"!");

		this.jdoConfigFile = jdoConfigFile;
		setChanged();
	}

	/**
	 * @return Returns the jdoTemplateDSXMLFile.
	 */
	public String getJdoTemplateDSXMLFile() 
	{
		return jdoTemplateDSXMLFile;
	}
	
	/**
	 * @param jdoTemplateDSXMLFile The jdoTemplateDSXMLFile to set.
	 */
	public void setJdoTemplateDSXMLFile(String jdoTemplateDSXMLFile) 
	{
		this.jdoTemplateDSXMLFile = jdoTemplateDSXMLFile;
		setChanged();
	}
}