package org.nightlabs.jfire.serverinit;

import javax.naming.InitialContext;

import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.j2ee.VendorAdapter;
import org.nightlabs.jfire.servermanager.ra.JFireServerManagerFactoryImpl;

/**
 * <p>
 * This is the base class for all server initializers. A ServerInitializer is triggered,
 * whenever the server boots after all other initialization is done. In most of the use
 * cases, you'd probably prefer to use the datastore initialization mechanism as
 * <a href="https://www.jfire.org/modules/phpwiki/index.php/HowToDataStoreInit">described in the wiki</a>.
 * </p>
 * <p>
 * To 
 * </p>
 * <p>
 * A common use case for the <code>ServerInitializer</code> is the setup of a demo system,
 * because this requires to create one or more organisations.
 * </p>
 * <p>
 * For the setup of a demo system, it is recommended to combine the server init with the
 * datastore init: Declare a ServerInitializer only for the creation of the necessary
 * datastores and populate the demo data via the datastore init. When doing that, you must
 * of course check the organisation id in your datastore init beans!
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class ServerInitializer
{
	private JFireServerManagerFactory jFireServerManagerFactory;

	private InitialContext initialContext;

	public InitialContext getInitialContext()
	{
		return initialContext;
	}
	public void setInitialContext(InitialContext initialContext)
	{
		this.initialContext = initialContext;
	}

	/**
	 * This method is called prior to {@link #initialize() } by
	 * {@link JFireServerManagerFactoryImpl#serverStarted()} after all other
	 * server-boot-initializations. 
	 *
	 * @param fireServerManagerFactory
	 */
	public void setJFireServerManagerFactory(
			JFireServerManagerFactory fireServerManagerFactory)
	{
		jFireServerManagerFactory = fireServerManagerFactory;
	}

	public JFireServerManagerFactory getJFireServerManagerFactory()
	{
		return jFireServerManagerFactory;
	}

	private VendorAdapter j2eeVendorAdapter;
	
	public VendorAdapter getJ2EEVendorAdapter()
	{
		return j2eeVendorAdapter;
	}
	public void setJ2EEVendorAdapter(VendorAdapter vendorAdapter)
	{
		j2eeVendorAdapter = vendorAdapter;
	}

	/**
	 * This method is called, every time the server is booted after all other server/datastore
	 * initialization is done and all setters of this instance have been called.
	 * <p>
	 * You must find out yourself, whether your system is already initialized. You can do that
	 * easily by calling {@link JFireServerManager} whether your demo organisation(s) already
	 * exist(s).
	 * </p>
	 * @throws Exception TODO
	 */
	public abstract void initialize() throws Exception;
}
