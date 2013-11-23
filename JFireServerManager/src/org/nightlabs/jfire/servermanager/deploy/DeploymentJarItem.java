package org.nightlabs.jfire.servermanager.deploy;

import java.io.File;
import java.util.Map;

public class DeploymentJarItem
{
	private File deploymentJarEntry;
	private File templateFile;
	private Map<String, String> additionalVariables;

	/**
	 * @param deploymentJarEntry The <b>relative</b> path of the entry within the jar. If it contains no directory, but only the
	 *		simple file name, it will be created directly within the jar's root.
	 * @param templateFile The template. Must not be <code>null</code>.
	 * @param additionalVariables Additional variables. Can be <code>null</code>.
	 */
	public DeploymentJarItem(File deploymentJarEntry, File templateFile, Map<String, String> additionalVariables)
	{
		if (templateFile == null)
			throw new IllegalArgumentException("templateFile must not be null!");

		if (deploymentJarEntry == null)
			throw new IllegalArgumentException("deploymentJarEntry must not be null!");

		this.templateFile = templateFile;
		this.deploymentJarEntry = deploymentJarEntry;
		this.additionalVariables = additionalVariables;
	}

	public File getDeploymentJarEntry()
	{
		return deploymentJarEntry;
	}

	public File getTemplateFile()
	{
		return templateFile;
	}

	public Map<String, String> getAdditionalVariables()
	{
		return additionalVariables;
	}
}
