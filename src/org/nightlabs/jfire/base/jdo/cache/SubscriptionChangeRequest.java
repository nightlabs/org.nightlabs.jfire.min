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

package org.nightlabs.jfire.base.jdo.cache;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class SubscriptionChangeRequest
{
	public static final byte ACTION_ADD = 0;
	public static final byte ACTION_REMOVE = 1;

	private static String[] ACTIONS = new String[] {"add", "remove"};

	private byte action;
	private Object objectID;
	private long createDT = System.currentTimeMillis();
	private long delayMSec;

	/**
	 * @param action One of {@link #ACTION_ADD} or {@link #ACTION_REMOVE}
	 * @param objectID The objectID for which to either add or remove a listener.
	 * @param delayMSec The action can be done immediately (means as soon as possible,
	 *		because it's async and periodical) or it can be delayed for a certain time
	 *		specified in millisec.
	 */
	public SubscriptionChangeRequest(byte action, Object objectID, long delayMSec)
	{
		if (action != ACTION_ADD && action != ACTION_REMOVE)
			throw new IllegalArgumentException("action \""+action+"\" invalid! Must be either ACTION_ADD=\""+ACTION_ADD+"\" or ACTION_REMOVE=\""+ACTION_REMOVE+"\"");

		this.action = action;

		if (null == objectID)
			throw new NullPointerException("objectID");
		this.objectID = objectID;

		if (delayMSec < 0)
			throw new IllegalArgumentException("delayMSec < 0!!! Must be >= 0!");
		this.delayMSec = delayMSec;
	}

	/**
	 * @return Returns the action.
	 */
	public byte getAction()
	{
		return action;
	}
	/**
	 * @return Returns the objectID.
	 */
	public Object getObjectID()
	{
		return objectID;
	}
	/**
	 * @return Returns the createDT.
	 */
	public long getCreateDT()
	{
		return createDT;
	}
	/**
	 * @return Returns the delayMSec.
	 */
	public long getDelayMSec()
	{
		return delayMSec;
	}
	/**
	 * @return Returns the sum of <tt>createDT</tt> and <tt>delayMSec</tt>.
	 */
	public long getScheduledActionDT()
	{
		return createDT + delayMSec;
	}

	private transient String thisString = null;
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		if (thisString == null) {
			StringBuffer sb = new StringBuffer();
			sb.append(this.getClass().getName());
			sb.append('{');
			sb.append("action=");
			sb.append(ACTIONS[action]);
			sb.append(';');
			sb.append("objectID=");
			sb.append(objectID);
			sb.append(';');
			sb.append("delayMSec=");
			sb.append(delayMSec);
			sb.append('}');

			thisString = sb.toString();
		}

		return thisString;
	}
}
