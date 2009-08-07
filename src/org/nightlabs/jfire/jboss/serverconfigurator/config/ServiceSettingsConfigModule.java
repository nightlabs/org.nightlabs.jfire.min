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

	private Integer jaasCacheTimeout;
	private Integer transactionTimeout;

	@Override
	public void init() throws InitException
	{
		if(jaasCacheTimeout == null)
			jaasCacheTimeout = 300;
		if(transactionTimeout == null)
			transactionTimeout = 900;
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
