package org.nightlabs.jfire.servermanager.deploy;

import java.io.File;
import java.io.IOException;

public class DeployedFileAlreadyExistsException
		extends IOException
{
	private static final long serialVersionUID = 1L;

	private File destinationFile;

	public DeployedFileAlreadyExistsException()
	{
	}

	public DeployedFileAlreadyExistsException(File destinationFile)
	{
		super("File already exists: " + destinationFile);
		this.destinationFile = destinationFile;
	}

	public File getDestinationFile()
	{
		return destinationFile;
	}
}
