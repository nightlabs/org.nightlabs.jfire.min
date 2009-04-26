/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.testsuite.jdo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.User;


/**
 * @ejb.bean name="jfire/ejb/JFireTestSuite/JDOTest"
 *					 jndi-name="jfire/ejb/JFireTestSuite/JDOTest"
 *					 type="Stateless"
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class JDOTestBean
extends BaseSessionBeanImpl
implements JDOTestRemote
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(JDOTestBean.class);

	private static Query createUserQuery(PersistenceManager pm)
	{
		Query q = pm.newQuery(User.class);
		q.setOrdering("organisationID ASC, userID ASC");
		return q;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.testsuite.jdo.JDOTestRemote#createArrayListFromQueryResult()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public void createArrayListFromQueryResult()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			ArrayList<User> userList;
			{
				Query q = createUserQuery(pm);
				Collection<?> c = (Collection<?>) q.execute();
				ArrayList<?> l = new ArrayList<Object>(c);
				userList = new ArrayList<User>(l.size());

				for (Object object : l) {
					if (object == null)
						throw new IllegalStateException("One of the elements in the ArrayList is null!");

					userList.add((User) object);
				}
			}

			if (userList.isEmpty())
				throw new IllegalStateException("userList is empty! This is impossible! There must be at least some initial users in every organisation!");

			// try the toArray(...) method a few times with different array sizes
			// first try with correct size
			User[] userArray;
			{
				Query q = createUserQuery(pm);
				Collection<?> c = (Collection<?>) q.execute();
				userArray = new User[userList.size()];
				Object[] objectArray = c.toArray(userArray);
				if (objectArray != userArray)
					throw new IllegalStateException("Even though the array passed to Collection.toArray(Object[]) was long enough, a new instance was returned!");

				int idx = -1;
				for (User user : userList) {
					if (!user.equals(userArray[++idx]))
						throw new IllegalStateException("The users returned by the first query are not the same as returned by the second query! idx=" + idx + " userFrom1stQuery=" + user.getCompleteUserID() + " userFrom2ndQuery=" + userArray[idx]);
				}
			}

			// try with bigger array than necessary (1 element more)
			{
				Query q = createUserQuery(pm);
				Collection<?> c = (Collection<?>) q.execute();
				userArray = new User[userList.size() + 1];
				for (int i = 0; i < userArray.length; i++) {
					userArray[i] = new User("test.dummy.org", "dummy");
				}

				Object[] objectArray = c.toArray(userArray);
				if (objectArray != userArray)
					throw new IllegalStateException("Even though the array passed to Collection.toArray(Object[]) was long enough, a new instance was returned!");

				int idx = -1;
				for (User user : userList) {
					if (!user.equals(userArray[++idx]))
						throw new IllegalStateException("The users returned by the first query are not the same as returned by the second query! idx=" + idx + " userFrom1stQuery=" + user.getCompleteUserID() + " userFrom2ndQuery=" + userArray[idx]);
				}

				if (userArray[++idx] != null)
					throw new IllegalStateException("The first element after the data in the array should be null, but isn't!");
			}

			// try with bigger array than necessary (2 elements more)
			{
				Query q = createUserQuery(pm);
				Collection<?> c = (Collection<?>) q.execute();
				userArray = new User[userList.size() + 2];
				for (int i = 0; i < userArray.length; i++) {
					userArray[i] = new User("test.dummy.org", "dummy");
				}

				Object[] objectArray = c.toArray(userArray);
				if (objectArray != userArray)
					throw new IllegalStateException("Even though the array passed to Collection.toArray(Object[]) was long enough, a new instance was returned!");

				int idx = -1;
				for (User user : userList) {
					if (!user.equals(userArray[++idx]))
						throw new IllegalStateException("The users returned by the first query are not the same as returned by the second query! idx=" + idx + " userFrom1stQuery=" + user.getCompleteUserID() + " userFrom2ndQuery=" + userArray[idx]);
				}

				if (userArray[++idx] != null)
					throw new IllegalStateException("The first element after the data in the array should be null, but isn't!");

				if (userArray[++idx] == null)
					logger.info("The second element after the data in the array has been nulled, even though this is not necessary.");
			}

			// try with smaller array than necessary
			{
				Query q = createUserQuery(pm);
				Collection<?> c = (Collection<?>) q.execute();
				userArray = new User[userList.size() - 1];
				Object[] objectArray = c.toArray(userArray);
				if (objectArray == userArray)
					throw new IllegalStateException("Even though the array passed to Collection.toArray(Object[]) was too small, the same instance was returned!");

				if (!(objectArray instanceof User[]))
					throw new IllegalStateException("The result returned from Collection.toArray(Object[]) has not the same type as the array argument! Should be: " + User[].class.getName() + " but is: " + objectArray.getClass().getName());

				userArray = (User[]) objectArray;

				int idx = -1;
				for (User user : userList) {
					if (!user.equals(userArray[++idx]))
						throw new IllegalStateException("The users returned by the first query are not the same as returned by the second query! idx=" + idx + " userFrom1stQuery=" + user.getCompleteUserID() + " userFrom2ndQuery=" + userArray[idx]);
				}
			}

		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.testsuite.jdo.JDOTestRemote#createHashSetFromQueryResult()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public void createHashSetFromQueryResult()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = createUserQuery(pm);
			Collection<?> c = (Collection<?>) q.execute();
			new HashSet<Object>(c);
		} finally {
			pm.close();
		}
	}

//	/**
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 * @ejb.transaction type="Required"
//	 */
//	public Workstation getWorkstation(WorkstationID workstationID)
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
//			pm.getFetchPlan().setGroups(FetchPlan.DEFAULT);
//
//			Workstation workstation;
//			try {
//				workstation = (Workstation) pm.getObjectById(workstationID);
//			} catch (JDOObjectNotFoundException x) {
//				workstation = new Workstation(workstationID.organisationID, workstationID.workstationID);
//				workstation.setDescription("Test");
//				workstation = pm.makePersistent(workstation);
//			}
//
//			return pm.detachCopy(workstation);
//		} finally {
//			pm.close();
//		}
//	}
//
//	/**
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 * @ejb.transaction type="Required"
//	 */
//	public void storeWorkstation(Workstation workstation)
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			pm.makePersistent(workstation);
//			workstation = null;
//
//			System.gc();
//
//		} finally {
//			pm.close();
//		}
//	}
}
