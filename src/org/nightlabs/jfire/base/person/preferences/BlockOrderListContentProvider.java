/*
 * Created 	on Dec 10, 2004
 * 					by alex
 *
 */
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
