/* ********************************************************************
 * NightLabsBase - Utilities by NightLabs                             *
 * Copyright (C) 2004-2008 NightLabs GmbH - http://NightLabs.org      *
 *                                                                    *
 * This library is free software; you can redistribute it and/or      *
 * modify it under the terms of the GNU Lesser General Public         *
 * License as published by the Free Software Foundation; either       *
 * version 2.1 of the License, or (at your option) any later version. *
 *                                                                    *
 * This library is distributed in the hope that it will be useful,    *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of     *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU  *
 * Lesser General Public License for more details.                    *
 *                                                                    *
 * You should have received a copy of the GNU Lesser General Public   *
 * License along with this library; if not, write to the              *
 *     Free Software Foundation, Inc.,                                *
 *     51 Franklin St, Fifth Floor,                                   *
 *     Boston, MA  02110-1301  USA                                    *
 *                                                                    *
 * Or get it online:                                                  *
 *     http://www.gnu.org/copyleft/lesser.html                        *
 **********************************************************************/
package org.nightlabs.jfire.serverupdate.launcher;

/**
 * This is a copy of the interface <code>org.nightlabs.util.ParameterCoder</code> because
 * it is not available in this project (<code>JFireServerUpdateLauncher</code> must not have
 * any dependencies!).
 * <p>
 * Implementations of this interface encode and decode parameter-names and -values in
 * the {@link ParameterMap}.
 * </p>
 *
 * @author Marius Heinzmann -- Marius[at]NightLabs[dot]de
 */
interface ParameterCoder
{
	String encode(String plain);
	String decode(String encoded);
}
