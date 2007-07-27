package org.nightlabs.jfire.servermanager.config;

import java.io.Serializable;

import org.nightlabs.config.InitException;

/**
 * An abstract convenience class for configuration classes
 * that are owned by the core config module {@link JFireServerConfigModule}.
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public abstract class JFireServerConfigPart implements Serializable
{
	/**
	 * The parent config module.
	 */
	private transient JFireServerConfigModule parentConfigModule;
	
	/**
	 * Notify the server config module that this config part has changed.
	 */
	public void setChanged()
	{
		if(parentConfigModule != null)
			parentConfigModule.setChanged();
	}
	
  /**
   * Initialise this config part.
   * @throws InitException In case of an error
   */
  public void init()
  throws InitException
  {
  }

	/**
	 * Get the parent config module.
	 * @return the parent config module
	 */
	public JFireServerConfigModule getParentConfigModule()
	{
		return parentConfigModule;
	}

	/**
	 * Set the parent config module.
	 * @param parentConfigModule the parent config module to set
	 */
	public void setParentConfigModule(JFireServerConfigModule parentConfigModule)
	{
		this.parentConfigModule = parentConfigModule;
	}
}
