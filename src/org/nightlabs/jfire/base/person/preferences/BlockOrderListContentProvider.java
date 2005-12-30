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

package org.nightlabs.jfire.base.person.preferences;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class BlockOrderListContentProvider implements IStructuredContentProvider
{
	public BlockOrderListContentProvider() {		
	}
	
	public Map structBlockDisplayOrder;
	private List structBlockDisplayOrderEntryList;
	
	public class Entry implements Map.Entry {
		
		private Map.Entry entry;
		public Entry(Map.Entry entry) {
			this.entry = entry;
		}
		/**
		 * @see java.util.Map.Entry#getKey()
		 */
		public Object getKey() {
			return entry.getKey();
		}

		/**
		 * @see java.util.Map.Entry#getValue()
		 */
		public Object getValue() {
			if (internalValue == null)
				return entry.getValue();
			else
				return internalValue;
		}

		
		private Object internalValue;
		/**
		 * @see java.util.Map.Entry#setValue(java.lang.Object)
		 */
		public Object setValue(Object value) {
			return internalValue = value;
		}
		
	}
	
	protected void refreshOrder() {
		Set entrySet = structBlockDisplayOrder.entrySet();
		structBlockDisplayOrderEntryList = new LinkedList();
		// get the list
		for (Iterator iter = entrySet.iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			structBlockDisplayOrderEntryList.add(new Entry(entry));
		}
		sortList();
	}
	
	protected void sortList() {
		// sort it 
		Collections.sort(structBlockDisplayOrderEntryList,new Comparator() {
			public int compare(Object o1, Object o2) {
				if ((o1 instanceof Map.Entry) && (o2 instanceof Map.Entry)) {
					int p1, p2;
					p1 = ((Integer) ((Map.Entry)o1).getValue() ).intValue();
					p2 = ((Integer) ((Map.Entry)o2).getValue() ).intValue();
					if (p1 < p2)
						return -1;
					else if (p1 > p2) 
						return 1;
					else 
						return 0;
				}
				return 0;
			}			
		});
		
	}
	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		if (inputElement != structBlockDisplayOrder) {
			this.structBlockDisplayOrder = (Map)inputElement;
			// get the list
			refreshOrder();
		}
		// return the array 
		return structBlockDisplayOrderEntryList.toArray();
//		return PersonStructOrderConfigModule.getSharedInstance().structBlockDisplayOrder().entrySet().toArray();
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {		
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (structBlockDisplayOrderEntryList != null)
			sortList();
//		sortList();
	}
	
	
	public void moveUp(int priority) {
		structBlockDisplayOrderEntryList.hashCode();
		for (Iterator iter = structBlockDisplayOrderEntryList.iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			int swapPriority = ((Integer)entry.getValue()).intValue();
			if (swapPriority == priority)
				entry.setValue(new Integer(priority-1));
			else if (swapPriority == (priority-1))
				entry.setValue(new Integer(priority));
		}
		sortList();
	}

	public void moveDown(int priority) {
		structBlockDisplayOrderEntryList.hashCode();
		for (Iterator iter = structBlockDisplayOrderEntryList.iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			int swapPriority = ((Integer)entry.getValue()).intValue();
			if (swapPriority == priority)
				entry.setValue(new Integer(priority+1));
			else if (swapPriority == (priority+1))
				entry.setValue(new Integer(priority));
		}
		sortList();
	}
	
	public Map getStructBlockOrder() {
		Map result = new HashMap();
		for (Iterator iter = structBlockDisplayOrderEntryList.iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			result.put(entry.getKey(),entry.getValue());			
		}
		return result;
	}
	
}
