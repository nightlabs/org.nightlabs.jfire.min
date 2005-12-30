/*
 * Created 	on Dec 19, 2004
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.search;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.TextPersonDataField;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonSearchResultTableLabelProvider implements ITableLabelProvider {

	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		// TODO: temporär -> columns, data ...
		String label = "";
		if ((element instanceof Person)) {
//			throw new IllegalArgumentException("Element should be of type Person but is "+element.getClass().getName());
			Person person = (Person)element;
			label = person.getOrganisationID()+": "+person.getPersonID();
		} 
		else if (element instanceof Object[]) {
			Object[] array = (Object[])element;
			if (array.length > columnIndex) {
				if (array[columnIndex] instanceof TextPersonDataField) {
					label = ((TextPersonDataField)array[columnIndex]).getText();
				}
			}
			else
				label = array[columnIndex].toString();
		}
		return label;
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}

}
