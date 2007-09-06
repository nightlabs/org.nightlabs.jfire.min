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

package org.nightlabs.jfire.base.prop.edit.blockbased;

import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.id.StructBlockID;

/**
 * A WizardPage to define values for one DataBlock.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class DataBlockWizardPage extends CompoundDataBlockWizardPage {

	private StructBlockID structBlockID;
		
	
	/**
	 * Creates a new DataBlockWizardPage for the 
	 * StructBlock identified by the dataBlockID 
	 */
	public DataBlockWizardPage(
		String pageName, 
		String title, 
		IStruct struct,
		PropertySet prop,
		StructBlockID structBlockID
	) {
		super(pageName, title, prop, new StructBlockID[]{structBlockID});
		this.structBlockID = structBlockID;
	}
	
	
	/**
	 * Returns the PropsStructBlockID this WizardPage is 
	 * associated to.
	 * 
	 * @return
	 */
	public StructBlockID getStructBlockID() {
		return structBlockID;
	}
	
}
