package org.nightlabs.jfire.idgenerator;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.idgenerator.IDNamespace;
import org.nightlabs.jfire.idgenerator.id.IDNamespaceID;

/**
 * @ejb.bean name="jfire/ejb/JFireBaseBean/IDGeneratorHelper"
 *	jndi-name="jfire/ejb/JFireBaseBean/IDGeneratorHelper"
 *	type="Stateless"
 *
 * @ejb.util generate="physical"
 */
public abstract class IDGeneratorHelperBean
extends BaseSessionBeanImpl implements SessionBean
{
//	public static final Logger LOGGER = Logger.getLogger(IDGeneratorHelperBean.class);

	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	/**
	 * @ejb.create-method  
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate() throws CreateException
	{
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }

	/**
	 * @param namespace The namespace (within the scope of the current organisation) within which unique IDs need to be generated.
	 * @param currentCacheSize The current number of cached IDs.
	 * @param minCacheSize The minimum number of IDs that must be available in the cache after the generated ones are added.
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="RequiresNew"
	 **/
	public long[] nextIDs(String namespace, int currentCacheSize, int minCacheSize) 
	throws ModuleException
	{
		try {
			PersistenceManager pm = this.getPersistenceManager();
			try {
				pm.getExtent(IDNamespace.class);

				IDNamespace idNamespace = null;
				try {
					idNamespace = (IDNamespace) pm.getObjectById(IDNamespaceID.create(getOrganisationID(), namespace));
				} catch (JDOObjectNotFoundException e) {
					idNamespace = new IDNamespace(getOrganisationID(), namespace);
					pm.makePersistent(idNamespace);
				}

				int quantity = minCacheSize - currentCacheSize + idNamespace.getCacheSizeServer();
				if (quantity <= 0)
					return new long[0];

				long[] res = new long[quantity];
				long nextID = idNamespace.getNextID();
				for (int i = 0; i < quantity; ++i) {
					res[i] = nextID++;
				}
				idNamespace.setNextID(nextID);

				return res;
			} finally {
				pm.close();
			}
		} catch(Exception e) {
			throw new ModuleException(e);
		}
	}

}
