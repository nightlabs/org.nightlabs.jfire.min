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

package org.nightlabs.jfire.prop.search;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jdo.search.SearchFilterItem;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.id.StructFieldID;




/**
 * Default implementation of {@link IStructFieldSearchFilterItem} that contains basic
 * code for most search filter items for struct fields. It implements the methods
 * {@link #getStructFieldID()} and {@link #getStructFieldIDs()} where it returns the
 * IDs that are passed in the constructor call.<p>
 * 
 * Subclasses have to implement {@link #getDataFieldClass()} and return the class of the
 * {@link DataField} that should be matched by the filter item.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de --> *
 */
public abstract class AbstractStructFieldSearchFilterItem
extends SearchFilterItem
implements IStructFieldSearchFilterItem
{
	private static final long serialVersionUID = 1L;

	public static final String QUERY_DATAFIELD_VARNAME = "dataField";

	protected List<StructFieldID> structFieldIDs = new ArrayList<StructFieldID>();

	protected AbstractStructFieldSearchFilterItem(Collection<StructFieldID> structFieldIDs, MatchType matchType) {
		super(matchType);
		
		if (!getSupportedMatchTypes().contains(matchType))
			throw new IllegalArgumentException("The given MatchType is not legal for this SearchFilterItem.");
		
		if (structFieldIDs != null)
			this.structFieldIDs = new LinkedList<StructFieldID>(structFieldIDs);
	}
	
	protected AbstractStructFieldSearchFilterItem(StructFieldID structFieldID, MatchType matchType) {
		this(Collections.singleton(structFieldID), matchType);
	}
	
	@Override
	public void appendSubQuery(int itemIndex, Set<Class<?>> imports, StringBuffer vars, StringBuffer filter, StringBuffer params,
			Map<String, Object> paramMap) {
	}
	
	public StructFieldID getStructFieldID() {
		return structFieldIDs.get(0);
	}

	public List<StructFieldID> getStructFieldIDs() {
		return structFieldIDs;
	}
}
