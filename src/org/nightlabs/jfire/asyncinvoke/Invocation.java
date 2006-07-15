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

package org.nightlabs.jfire.asyncinvoke;

import java.io.Serializable;

/**
 * <p>
 * Subclass this <code>Invocation</code> and pass an instance to {@link AsyncInvoke} in order
 * to do some work asynchronously.
 * </p>
 * <p>
 * If you want to react on successful completion, an error or the final give-up (when it permanently fails),
 * you can implement one or more of the callbacks (and pass them additionally to {@link AsyncInvoke}):
 * <ul>
 * <li>{@link SuccessCallback}</li>
 * <li>{@link ErrorCallback}</li>
 * <li>{@link UndeliverableCallback}</li>
 * </ul>
 * All of them are triggered within a <b>separate transaction</b> in order to isolate them
 * from this invocation (in case of an error, this is a must, of course; for the {@link SuccessCallback},
 * it's not really necessary as you can put that logic into the main invocation).
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class Invocation
extends BaseInvocation
{

	public Invocation()
	{
	}

	/**
	 * This method is called by the framework and you can do here whatever you want
	 * to do asynchronously.
	 *
	 * @return You can return either <code>null</code> or any {@link Serializable} that you want to pass
	 *		to the {@link SuccessCallback}.
	 * @throws Exception If anything goes wrong, throw whatever exception you like. It will be passed to the
	 *		{@link ErrorCallback}.
	 */
	public abstract Serializable invoke()
	throws Exception;

}
