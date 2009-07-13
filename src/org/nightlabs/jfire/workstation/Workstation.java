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

package org.nightlabs.jfire.workstation;

import java.io.Serializable;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.JFireBasePrincipal;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.SecurityReflector.UserDescriptor;
import org.nightlabs.jfire.workstation.id.WorkstationID;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 * @jdo.persistence-capable
 *    identity-type="application"
 *    objectid-class="org.nightlabs.jfire.workstation.id.WorkstationID"
 *    detachable="true"
 *    table="JFireBase_Workstation"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, workstationID"
 *
 * @jdo.fetch-group name="Workstation.this" fetch-groups="default"
 *
 */
@PersistenceCapable(
	objectIdClass=WorkstationID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Workstation")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name=Workstation.FETCH_GROUP_THIS_WORKSTATION,
		members={})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Workstation implements Serializable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_WORKSTATION = "Workstation.this";

	public static final String WORKSTATION_ID_FALLBACK = "_Fallback_";

	/**
	 * Get the <code>Workstation</code> object of the currently logged-in <code>principal</code>. If no workstation was specified,
	 * this method returns <code>null</code>. You can and should use this method when you need a Workstation inside an EJB.
	 *
	 * @param pm the door to the datastore.
	 * @param principal the principal.
	 * @param workstationResolveStrategy specifies what to do when the <code>principal.workstationID</code> is <code>null</code> (i.e. has not been specified by the user during login).
	 * @return a <code>Workstation</code> object or <code>null</code>, if allowed by <code>workstationResolveStrategy</code>.
	 */
	public static Workstation getWorkstation(PersistenceManager pm, JFireBasePrincipal principal, WorkstationResolveStrategy workstationResolveStrategy) {
		String workstationID = principal.getWorkstationID();
		if (workstationID == null)
			return null;

		return getWorkstation(pm, principal.getOrganisationID(), workstationID, workstationResolveStrategy);
	}

	/**
	 * Get a <code>Workstation</code> object.
	 *
	 * @param pm the door to the datastore.
	 * @param organisationID the organisationID (i.e. the first part of the composite primary key).
	 * @param workstationID the workstationID or <code>null</code>.
	 * @param workstationResolveStrategy specifies what to do when the <code>workstationID</code> is <code>null</code> (i.e. has not been specified by the user during login).
	 * @return a <code>Workstation</code> or <code>null</code> if <code>workstationID == null</code> was passed and the <code>workstationResolveStrategy</code> allows it.
	 */
	public static Workstation getWorkstation(PersistenceManager pm, String organisationID, String workstationID, WorkstationResolveStrategy workstationResolveStrategy) {
		if (workstationID == null) {
			switch (workstationResolveStrategy) {
				case NULL:
					return null;
				case EXCEPTION:
					throw new WorkstationNotSpecifiedException();
				case FALLBACK:
					workstationID = WORKSTATION_ID_FALLBACK;
					break;
				default:
					throw new IllegalStateException("Unknown WorkstationResolveStrategy: " + workstationResolveStrategy);
			}
		}

		pm.getExtent(Workstation.class);
		return (Workstation) pm.getObjectById(WorkstationID.create(organisationID, workstationID), true);
	}

	/**
	 * Get the <code>Workstation</code> object of the currently logged-in principal. This method uses
	 * the {@link SecurityReflector} to find out the current workstation.
	 *
	 * @param pm the door to the datastore.
	 * @param workstationResolveStrategy specifies what to do when the <code>workstationID</code> of the current principal is <code>null</code> (i.e. has not been specified by the user during login).
	 * @return a <code>Workstation</code> or <code>null</code> if there is no workstationID specified for the current user and the <code>workstationResolveStrategy</code> allows it.
	 */
	public static Workstation getWorkstation(PersistenceManager pm, WorkstationResolveStrategy workstationResolveStrategy) {
		UserDescriptor userDescriptor = SecurityReflector.getUserDescriptor();
		String organisationID = userDescriptor.getOrganisationID();
		String workstationID = userDescriptor.getWorkstationID();
		return getWorkstation(pm, organisationID, workstationID, workstationResolveStrategy);
	}
	
	public static WorkstationID getWorkstationID(String organisationID, String workstationID, WorkstationResolveStrategy workstationResolveStrategy) {
		if (workstationID == null || workstationID.trim().isEmpty()) {
			switch (workstationResolveStrategy) {
				case NULL: return null;
				case EXCEPTION: throw new WorkstationNotSpecifiedException();
				case FALLBACK: workstationID = WORKSTATION_ID_FALLBACK;
			}
		}
		return WorkstationID.create(organisationID, workstationID);
	}

  /**
   * @jdo.field primary-key="true"
   * @jdo.column length="100"
   */
  @PrimaryKey
  @Column(length=100)
  private String organisationID;

  /**
   * @jdo.field primary-key="true"
   * @jdo.column length="100"
   */
  @PrimaryKey
  @Column(length=100)
  private String workstationID;

  /**
   * @jdo.field persistence-modifier="persistent"
   * @jdo.column length="255"
   */
  @Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
  @Column(length=255)
  private String description;

  public Workstation()
  {
  }

  public Workstation(String organisationID, String workstationID)
  {
  	Organisation.assertValidOrganisationID(organisationID);
    this.organisationID = organisationID;
    ObjectIDUtil.assertValidIDString(workstationID, "workstationID");
    this.workstationID = workstationID;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getOrganisationID()
  {
    return organisationID;
  }

  public String getWorkstationID()
  {
    return workstationID;
  }

  public static Workstation storeWorkstation(PersistenceManager pm, Workstation ws)
  {
    Workstation ret;
    if(JDOHelper.isDetached(ws))
      ret = pm.makePersistent(ws); // (Workstation)pm.attachCopy(ws, true);
    else
    {
      ws = pm.makePersistent(ws);
      ConfigSetup.ensureAllPrerequisites(pm);
      ret = ws;
    }
    return ret;
  }

//	/**
//	 * Creates a new Workstation with the given key, makes it persitent and
//	 * returns it.
//	 *
//	 * @param pm The PersitenceManager to use.
//	 * @param organisationID The organistationID of the new Workstation
//	 * @param workstationID The workstationID of the new Workstation
//	 * @return A new and perstiten Workstation.
//	 */
//	public static Workstation addWorkstation(PersistenceManager pm, String organisationID, String workstationID) {
//		Workstation workstation = new Workstation(organisationID, workstationID);
//		pm.makePersistent(workstation);
//		return workstation;
//	}

//	/**
//	 * Returns all Workstations known in the datastore of the given
//	 * PersistenceManager.
//	 *
//	 * @param pm The persitenceManager to use.
//	 * @return All Workstations
//	 */
//	public static Collection<Workstation> getWorkstations(PersistenceManager pm) {
//		return (Collection<Workstation>)pm.newQuery(pm.getExtent(Workstation.class)).execute();
//	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + workstationID + ']';
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((workstationID == null) ? 0 : workstationID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final Workstation other = (Workstation) obj;
		return (
				Util.equals(this.organisationID, other.organisationID) &&
				Util.equals(this.workstationID, other.workstationID)
		);
	}
}
