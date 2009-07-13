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

package org.nightlabs.jfire.base;

import java.io.Serializable;
import java.security.Principal;

import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.RoleSet;


/**
 * @author marco
 */
public abstract class JFireBasePrincipal
implements Principal, Serializable // Serializable might be necessary for clusters?!
// We have to be very careful though that our password is not transferred to a remote server
// when accessing the OrganisationLinkerBean anonymously (no cascaded authentication)! Even
// though the password is not stored in this object, it might be transferred to the other server. TODO need further analysis!
{
	private static final long serialVersionUID = 1L;
//	private static final Logger logger = Logger.getLogger(JFireBasePrincipal.class);

	private LoginData loginData;
	private boolean userIsOrganisation;
	private RoleSet roleSet;

	public JFireBasePrincipal(LoginData _loginData, boolean _userIsOrganisation, RoleSet _roleSet)
	{
		if (_loginData == null)
			throw new IllegalArgumentException("loginData must not be null!");

		if (_roleSet == null)
			throw new IllegalArgumentException("roleSet must not be null!");

		// A principal *MUST* *NOT* contain a password!!! Hence, we copy it and remove the password.
		this.loginData = new LoginData(_loginData);
		this.loginData.setPassword(null);
		this.userIsOrganisation = _userIsOrganisation;
		this.roleSet = _roleSet;
	}

	public RoleSet getRoleSet()
	{
		return roleSet;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (!(obj instanceof JFireBasePrincipal))
			return false;

		JFireBasePrincipal other = (JFireBasePrincipal)obj;

		return
			this.userIsOrganisation == other.userIsOrganisation &&
			this.getName().equals(other.getName()) &&
			loginData.getOrganisationID().equals(other.loginData.getOrganisationID());
	}

	protected String thisString = null;
	@Override
	public String toString()
	{
		if (thisString == null) {
			StringBuilder sb = new StringBuilder();
			sb.append(this.getClass().getName());
			sb.append('[');
			sb.append(getName());
			sb.append(']');

//			sb.append('{');
//			sb.append(loginData.getUserID());
//			sb.append('@');
//			sb.append(loginData.getOrganisationID());
//			sb.append('}');
			thisString = sb.toString();
		}
		return thisString;
	}

	@Override
	public int hashCode()
	{
		return this.toString().hashCode();
	}

	/**
	 * @return Returns the userIsOrganisation.
	 */
	public boolean userIsOrganisation() {
		return userIsOrganisation;
	}
	/**
	 * @return the organisationID.
	 */
	public String getOrganisationID() {
		String organisationID = loginData.getOrganisationID();
		Organisation.assertValidOrganisationID(organisationID); // TODO remove this check later again. only temporary. Marco.
		return organisationID;
	}
	/**
	 * @return the userID.
	 */
	public String getUserID() {
		return loginData.getUserID();
	}

	/**
	 * Get the workstation-identifier of the currently logged-in user or <code>null</code> if he didn't specify one during login.
	 *
	 * @return the workstationID or <code>null</code>.
	 */
	public String getWorkstationID()
	{
		return loginData.getWorkstationID();
	}

	/**
	 * @return Returns the sessionID.
	 */
	public String getSessionID()
	{
		return loginData.getSessionID();
	}

	protected String principalName = null;

	@Override
	public String getName() {
		if (principalName == null) {
//			StringBuffer sb = new StringBuffer();
//			sb.append(loginData.getUserID());
//			sb.append('@');
//			sb.append(loginData.getOrganisationID());
//			sb.append('?');
//			sb.append(loginData.getAdditionalParams().dump());
//			principalName = sb.toString();
			principalName = loginData.getLoginDataURL();
		}
//		logger.debug("principalName="+principalName);
		return principalName;
	}

}
