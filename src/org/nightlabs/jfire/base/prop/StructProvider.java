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
//
//package org.nightlabs.jfire.base.prop;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.security.auth.login.LoginException;
//
//import org.nightlabs.jdo.NLJDOHelper;
//import org.nightlabs.jfire.base.jdo.JDOObjectProvider;
//import org.nightlabs.jfire.base.login.Login;
//import org.nightlabs.jfire.base.prop.id.StructBlockID;
//import org.nightlabs.jfire.base.prop.id.StructID;
//
///**
// * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
// * 
// */
//public class StructProvider extends JDOObjectProvider
//{
//	protected static final String[] dummyFetchGroups = new String[] {};
//
//	private List orderedPropStructBlocks;
//
//	private Class linkClass;
//
//	/**
//	 * 
//	 */
//	public StructProvider(Class linkClass)
//	{
//		super();
//		this.linkClass = linkClass;
//	}
//	
//	/**
//	 * Retrieves the Struct and creates the orderedPropStructBlocks list.
//	 * 
//	 * @see org.nightlabs.jfire.base.jdo.JDOObjectProvider#retrieveJDOObject(java.lang.String,
//	 *      java.lang.Object, java.lang.String[])
//	 */
//	protected synchronized Object retrieveJDOObject(String scope, Object objectID, String[] fetchGroups, int maxFetchDepth)
//			throws Exception
//	{
//		PropertyManager propManager = PropertyManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
//		IStruct propStruct = propManager.getFullStructure(Login.getLogin().getOrganisationID(), linkClass);
//
//		orderedPropStructBlocks = new ArrayList();
//		int allStructBlockCount = propStruct.getStructBlocks().size();
//		int unmentionedCount = 0;
//
////		Map structBlockOrder = AbstractPropStructOrderConfigModule.sharedInstance().structBlockDisplayOrder();
////		
////		for (Iterator it = propStruct.getStructBlocks().iterator(); it.hasNext();)
////		{
////			// all blocks
////			StructBlock structBlock = (StructBlock) it.next();
////
////			if (structBlockOrder.containsKey(structBlock.getPrimaryKey()))
////			{
////				// block mentioned in structBlockOrder
////				Integer index = (Integer) structBlockOrder.get(structBlock.getPrimaryKey());
////				structBlock.setPriority(index.intValue());
////			}
////			else
////			{
////				structBlock.setPriority(allStructBlockCount + (unmentionedCount++));
////			}
////
////			orderedPropStructBlocks.add(structBlock);
////		}
////		Collections.sort(orderedPropStructBlocks);
//		
//		for (StructBlock block : propStruct.getStructBlocks())
//		{
//			orderedPropStructBlocks.add(block);
//		}
//
//		return propStruct;
//	}
//
//	/**
//	 * Get the full Property structure out of the Cache.
//	 * 
//	 * @return The full Struct.
//	 */
//	public Struct getStruct()
//	{
//		try
//		{
//			return (Struct) getJDOObject("PropManager.getFullStruct", StructID.create(linkClass.getName(),
//					Login.getLogin().getOrganisationID()), dummyFetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
//		}
//		catch (LoginException e)
//		{
//			throw new RuntimeException(e);
//		}
//	}
//	
//	public StructLocal getStructLocal()
//	{
//		try
//		{
//			return (StructLocal) getJDOObject("PropManager.getFullStructLocal", StructID.create(linkClass.getName(),
//					Login.getLogin().getOrganisationID()), dummyFetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
//		}
//		catch (LoginException e)
//		{
//			throw new RuntimeException(e);
//		}
//	}
//
//	/**
//	 * Returns a ordered list of the StructBlocks for the current propertySet structure.
//	 */
//	public List getOrderedPropStructBlocks()
//	{
//		getStruct();
//		return orderedPropStructBlocks;
//	}
//
//	public List getOrderedPropStructFields(StructBlockID structBlockID)
//	{
//		// TODO: Implement getOrderedPropStructFields
//		return null;
//	}
//}