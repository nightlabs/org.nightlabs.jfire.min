/* ************************************************************************** *
 * Copyright (C) 2004 NightLabs GmbH, Marco Schulze                           *
 * All rights reserved.                                                       *
 * http://www.NightLabs.de                                                    *
 *                                                                            *
 * This program and the accompanying materials are free software; you can re- *
 * distribute it and/or modify it under the terms of the GNU General Public   *
 * License as published by the Free Software Foundation; either ver 2 of the  *
 * License, or any later version.                                             *
 *                                                                            *
 * This module is distributed in the hope that it will be useful, but WITHOUT *
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FIT- *
 * NESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more *
 * details.                                                                   *
 *                                                                            *
 * You should have received a copy of the GNU General Public License along    *
 * with this module; if not, write to the Free Software Foundation, Inc.:     *
 *    59 Temple Place, Suite 330                                              *
 *    Boston MA 02111-1307                                                    *
 *    USA                                                                     *
 *                                                                            *
 * Or get it online:                                                          *
 *    http://www.opensource.org/licenses/gpl-license.php                      *
 *                                                                            *
 * In case, you want to use this module or parts of it in a proprietary pro-  *
 * ject, you can purchase it under the NightLabs Commercial License. Please   *
 * contact NightLabs GmbH under info AT nightlabs DOT com for more infos or   *
 * visit http://www.NightLabs.com                                             *
 * ************************************************************************** */

/*
 * Created on 13.08.2004
 */
package org.nightlabs.jfire.servermanager.xml.test;

import java.io.File;
import java.util.Iterator;

import org.nightlabs.jfire.module.ModuleType;
import org.nightlabs.jfire.servermanager.xml.EARApplicationMan;
import org.nightlabs.jfire.servermanager.xml.ModuleDef;


/**
 * @author marco
 */
public class EARApplicationManTest {
	public static void main(String[] args) {
		try {
			File file = new File("/opt/java/jboss/server/default/deploy/JFire.last/JFireBase.ear");
			EARApplicationMan earAppMan = new EARApplicationMan(file, ModuleType.MODULE_TYPE_WEB);

			for (Iterator it = earAppMan.getModules().iterator(); it.hasNext(); ) {
				ModuleDef md = (ModuleDef)it.next();
				System.out.println(md.getResourceURI()+" : "+md.getContextPath()+" : "+md.getName());
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
}
