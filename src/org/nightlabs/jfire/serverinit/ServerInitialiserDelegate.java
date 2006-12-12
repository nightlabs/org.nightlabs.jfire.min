package org.nightlabs.jfire.serverinit;

import javax.naming.InitialContext;

import org.nightlabs.jfire.init.InitException;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;
import org.nightlabs.jfire.servermanager.ra.JFireServerManagerFactoryImpl;

/**
 * <p>
 * This is the base class for all server initialisers. A ServerInitialiserDelegate is triggered,
 * whenever the server boots after all other initialisation is done. In most of the use
 * cases, you'd probably prefer to use the datastore initialisation mechanism as
 * <a href="https://www.jfire.org/modules/phpwiki/index.php/HowToDataStoreInit">described in the wiki</a>.
 * </p>
 * <p>
 * Currently, there can only be one ServerInitialiserDelegate per EAR. You register it by placing
 * a file called <code>serverinit.properties</code> into the EAR directory (parallel to
 * the *.jar, *.war and *.rar files). Into this <code>serverinit.properties</code>, you declare
 * your initialiser as value to the property <code>serverInitialiser.class</code>.
 * </p>
 * <p>
 * <u>Example:</u><br/>
 * <code>
 * serverInitialiser.class=org.nightlabs.jfire.chezfrancois.ChezFrancoisServerInitialiser
 * </code>
 * </p>
 * <p>
 * <b>Important note:</b> This registration mechanism might soon be changed in order to
 * be controlled by XML files similar to the datastore initialisation (with dependency
 * declarations). Stay tuned on http://www.jfire.org
 * </p>
 * <p>
 * If there are multiple EARs declaring server initialisers, the EARs are ordered by their
 * directory name.
 * </p>
 * <p>
 * A common use case for the <code>ServerInitialiserDelegate</code> is the setup of a demo system,
 * because this requires to create one or more organisations.
 * </p>
 * <p>
 * For the setup of a demo system, it is recommended to combine the server init with the
 * datastore init: Declare a ServerInitialiserDelegate only for the creation of the necessary
 * datastores and populate the demo data via the datastore init. When doing that, you must
 * of course check the organisation id in your datastore init beans!
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class ServerInitialiserDelegate implements IServerInitialiser
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
	 * This method is called prior to {@link #initialise() } by
	 * {@link JFireServerManagerFactoryImpl#serverStarted()} after all other
	 * server-boot-initialisations. 
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

	private J2EEAdapter j2eeVendorAdapter;
	
	public J2EEAdapter getJ2EEVendorAdapter()
	{
		return j2eeVendorAdapter;
	}
	public void setJ2EEVendorAdapter(J2EEAdapter vendorAdapter)
	{
		j2eeVendorAdapter = vendorAdapter;
	}

	/**
	 * This method is called, every time the server is booted after all other server/datastore
	 * initialisation is done and all setters of this instance have been called.
	 * <p>
	 * You must find out yourself, whether your system is already initialised. You can do that
	 * easily by calling {@link JFireServerManager} whether your demo organisation(s) already
	 * exist(s).
	 * </p>
	 * @throws InitException when something goes wrong.
	 */
	public abstract void initialise() throws InitException;
}
