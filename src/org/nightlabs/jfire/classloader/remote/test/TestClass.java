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

package org.nightlabs.jfire.classloader.remote.test;

//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.URL;
//
//import org.nightlabs.config.Config;
//
//
///**
// * @author marco
// */
//public class TestClass
//{
//	static {
//		ClassLoader cl = TestClass.class.getClassLoader();
//		System.out.println("ClassLoader: " + (cl == null ? "null" : cl.toString()));
//		Config.isSharedInstanceExisting();
//		URL url = Config.class.getResource("test.txt");
//		System.out.println("URL(test.txt).class: " + url.getClass().getName());
//		System.out.println("URL(test.txt): " + url);
//		InputStream in = Config.class.getResourceAsStream("test.txt");
//		if (in == null)
//			System.out.println("InputStream is null!");
//		else {
//			InputStreamReader r = new InputStreamReader(in);
//			try {
//				int i;
//				do {
//					i = r.read();
//					char ch = (char)i;
//					System.out.print(ch);
//				} while (i >= 0);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//			
//	}
//
//	public TestClass() {
//		ClassLoader cl = TestClass.class.getClassLoader();
//		System.out.println("ClassLoader: " + (cl == null ? "null" : cl.toString()));
//	}
//
//}
