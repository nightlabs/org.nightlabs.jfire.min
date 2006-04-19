package org.nightlabs.jfire.id;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.naming.InitialContext;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.servermanager.j2ee.SecurityReflector;


public class IDGeneratorServer
		extends IDGenerator
{

	/**
	 * key: String organisationID<br/>
	 * value: Map {<br/>
	 * 		key: String namespace<br/>
	 * 		value: LinkedList cachedIDs<br/>
	 * }
	 */
	private Map<String, Map<String, LinkedList<Long>>> organisationID2IDCache = new HashMap<String, Map<String, LinkedList<Long>>>();

	private SecurityReflector securityReflector = null;

	@Override
	protected long[] _nextIDs(String namespace, int quantity)
	{
		// We have for sure no problem as long as the server has only one VM. But,
		// if it's running in a cluster and thus has many VMs, the synchronized blocks here
		// are not sufficient. In this case, it is necessary that the JDO backend (e.g. JPOX)
		// ensures that no two transactions retrieve the same IDs.
		// In case there are issues in a cluster which cannot be solved by JDO, we have to implement
		// a way to synchronise the different VMs.

		InitialContext initialContext = null;
		try {
			String organisationID;
			try {
				if (securityReflector == null) {
					if (initialContext == null)
						initialContext = new InitialContext();

					securityReflector = SecurityReflector.lookupSecurityReflector(initialContext);
				}
				organisationID = securityReflector.whoAmI().getOrganisationID();

				Map<String, LinkedList<Long>> namespace2cachedIDs;
				synchronized (organisationID2IDCache) {
					namespace2cachedIDs = organisationID2IDCache.get(organisationID);
					if (namespace2cachedIDs == null) {
						namespace2cachedIDs = new HashMap<String, LinkedList<Long>>();
						organisationID2IDCache.put(organisationID, namespace2cachedIDs);
					}
				} // synchronized (organisationID2IDCache) {

				LinkedList<Long> cachedIDs;
				synchronized (namespace2cachedIDs) {
					cachedIDs = namespace2cachedIDs.get(namespace);
					if (cachedIDs == null) {
						cachedIDs = new LinkedList<Long>();
						namespace2cachedIDs.put(namespace, cachedIDs);
					}
				} // synchronized (namespace2cachedIDs) {

				synchronized (cachedIDs) {
					if (quantity > cachedIDs.size()) {
						if (initialContext == null)
							initialContext = new InitialContext();

						Object objRef = initialContext.lookup(IDGeneratorHelperLocalHome.JNDI_NAME);
						IDGeneratorHelperLocalHome home;
		        // only narrow if necessary
		        if (java.rmi.Remote.class.isAssignableFrom(IDGeneratorHelperLocalHome.class))
		           home = (IDGeneratorHelperLocalHome) javax.rmi.PortableRemoteObject.narrow(objRef, IDGeneratorHelperLocalHome.class);
		        else
		           home = (IDGeneratorHelperLocalHome) objRef;

						IDGeneratorHelperLocal idGeneratorHelper = (IDGeneratorHelperLocal) home.create();
						long[] nextIDs = idGeneratorHelper.nextIDs(namespace, cachedIDs.size(), quantity);
						for (int i = 0; i < nextIDs.length; i++) {
							cachedIDs.add(new Long(nextIDs[i]));
						}
					}

					if (quantity > cachedIDs.size())
						throw new IllegalStateException("Number of cached IDs not sufficient!");

					long[] res = new long[quantity];
					for (int i = 0; i < quantity; ++i) {
						res[i] = cachedIDs.poll().longValue();
					}
					return res;
				} // synchronized (cachedIDs) {

			} finally {
				if (initialContext != null)
					initialContext.close();
			}
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

}
