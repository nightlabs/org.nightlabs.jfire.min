package org.nightlabs.jfire.jboss.serverconfigurator.config;

import org.nightlabs.config.ConfigModule;
import org.nightlabs.config.InitException;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ServiceSettingsConfigModule extends ConfigModule
{
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	private int jaasCacheTimeout = -1;
	private int transactionTimeout = -1;

	@Override
	public void init() throws InitException
	{
		if(jaasCacheTimeout == -1)
			jaasCacheTimeout = 300;
		if(transactionTimeout == -1)
			transactionTimeout = 900;

		// this is actually not how the config should be used. But without this call,
		// the config may never be changed by the user because it is never written.
		setChanged();
	}

	/**
	 * Get the jaasCacheTimeout.
	 * @return the jaasCacheTimeout
	 */
	public int getJaasCacheTimeout()
	{
		return jaasCacheTimeout;
	}

	/**
	 * Set the jaasCacheTimeout.
	 * @param jaasCacheTimeout the jaasCacheTimeout to set
	 */
	public void setJaasCacheTimeout(int jaasCacheTimeout)
	{
		this.jaasCacheTimeout = jaasCacheTimeout;
		setChanged();
	}

	/**
	 * Get the transactionTimeout.
	 * @return the transactionTimeout
	 */
	public int getTransactionTimeout()
	{
		return transactionTimeout;
	}

	/**
	 * Set the transactionTimeout.
	 * @param transactionTimeout the transactionTimeout to set
	 */
	public void setTransactionTimeout(int transactionTimeout)
	{
		this.transactionTimeout = transactionTimeout;
		setChanged();
	}
}
