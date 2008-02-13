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

package org.nightlabs.jfire.servermanager.xml.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import org.nightlabs.jfire.servermanager.xml.EJBJarMan;
import org.nightlabs.jfire.servermanager.xml.EJBRoleGroupMan;
import org.nightlabs.jfire.servermanager.xml.RoleGroupDef;


/**
 * @author marco
 */
public class EJBJarManTest {
	public static void main(String[] args) {
		try {
			File file = new File("/home/marco/workspace/JFireBaseBean/JFireBaseBean/META-INF/ejb-jar.xml");
			InputStream in = new FileInputStream(file);
			EJBJarMan ejbJarMan = new EJBJarMan("testmodule", in);
			in.close();
			
			file = new File("/home/marco/workspace/JFireBaseBean/JFireBaseBean/META-INF/ejb-rolegroup.xml");
			in = new FileInputStream(file);
			EJBRoleGroupMan ejb = new EJBRoleGroupMan(ejbJarMan, in);
			for (Iterator<RoleGroupDef> it = ejb.getRoleGroups().iterator(); it.hasNext(); ) {
				RoleGroupDef rg = it.next();
				System.out.println(rg.getRoleGroupID());
			}
			in.close();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
}
