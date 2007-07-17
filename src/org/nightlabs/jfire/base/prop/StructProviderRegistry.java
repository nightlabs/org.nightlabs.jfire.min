package org.nightlabs.jfire.base.prop;
///* *****************************************************************************
// * JFire - it's hot - Free ERP System - http://jfire.org                       *
// * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
// *                                                                             *
// * This library is free software; you can redistribute it and/or               *
// * modify it under the terms of the GNU Lesser General Public                  *
// * License as published by the Free Software Foundation; either                *
// * version 2.1 of the License, or (at your option) any later version.          *
// *                                                                             *
// * This library is distributed in the hope that it will be useful,             *
// * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
// * Lesser General Public License for more details.                             *
// *                                                                             *
// * You should have received a copy of the GNU Lesser General Public            *
// * License along with this library; if not, write to the                       *
// *     Free Software Foundation, Inc.,                                         *
// *     51 Franklin St, Fifth Floor,                                            *
// *     Boston, MA  02110-1301  USA                                             *
// *                                                                             *
// * Or get it online :                                                          *
// *     http://opensource.org/licenses/lgpl-license.php                         *
// *                                                                             *
// *                                                                             *
// ******************************************************************************/
//package org.nightlabs.jfire.base.prop;
//
//import java.util.Map;
//
//import org.nightlabs.jfire.base.jdo.JDOObjectProvider;
//
///**
// * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
// *
// */
//public class PropStructProviderRegistry
//{
//	private static Map<Class, JDOObjectProvider> providers;
//	private static PropStructProviderRegistry sharedInstance;
//	
//	public PropStructProviderRegistry()
//	{
//		
//	}
//	
//	public JDOObjectProvider getPropStructProvider(Class linkClass)
//	{
//		if (providers.containsKey(linkClass))
//			return providers.get(linkClass);
//		else
//		{
//			JDOObjectProvider prov = new StructProvider(linkClass);
//			providers.put(linkClass, prov);
//			return prov;
//		}
//	}
//	
//	public static PropStructProviderRegistry sharedInstance()
//	{
//		return sharedInstance == null ? sharedInstance = new PropStructProviderRegistry() : sharedInstance;
//	}	
//}
