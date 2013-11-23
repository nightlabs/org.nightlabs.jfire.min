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

package org.nightlabs.jfire.web.admin;

/**
 * This exception is thrown if a user tries to go onto a page that needs authentication
 * but the user did not yet login (or the login expired).
 * 
 * @author marco
 */
public class NotAuthenticatedException extends Exception {
	private static final long serialVersionUID = 1L;
	public NotAuthenticatedException() {
		super();
	}
	/**
	 * @param message
	 */
	public NotAuthenticatedException(String message) {
		super(message);
	}
	/**
	 * @param cause
	 */
	public NotAuthenticatedException(Throwable cause) {
		super(cause);
	}
	/**
	 * @param message
	 * @param cause
	 */
	public NotAuthenticatedException(String message, Throwable cause) {
		super(message, cause);
	}
}
