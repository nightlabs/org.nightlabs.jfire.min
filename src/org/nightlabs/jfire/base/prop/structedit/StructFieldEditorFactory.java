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

package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.core.runtime.IExecutableExtension;
import org.nightlabs.jfire.prop.AbstractStructField;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public interface StructFieldEditorFactory extends IExecutableExtension
{	
	/**
	 * Editors should return an appropriate {@link StructFieldEditor} for the given {@link AbstractStructField}
	 */
	public StructFieldEditor getStructFieldEditorSingleton(String structFieldClass);
	
	/**
	 * Should return the editor which must implement {@link StructFieldEditor}.
	 */
	public String getStructFieldEditorClass();
	
	/**
	 * Sets the editor class
	 * @param theClass Must implement {@link StructFieldEditor}
	 */
	public void setStructFieldEditorClass(String theClass);
	
	/**
	 * Should return a subclass of {@link AbstractStructField}
	 */
	public String getStructFieldClass();
	
	/**
	 * Sets the struct field.
	 * @param theClass Must be a subclass of {@link AbstractStructField}
	 */
	public void setStructFieldClass(String theClass);
}
