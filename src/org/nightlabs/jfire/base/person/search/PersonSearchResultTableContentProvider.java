/*
 * Created 	on Dec 19, 2004
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.search;

import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * This is a ContentProvider for person lists obtained
 * by a person search. The inputElement should be a 
 * Collecion of Person.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonSearchResultTableContentProvider implements IStructuredContentProvider{

	/**
	 * 
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Collection) { 
			Object[] result = ((Collection)inputElement).toArray();
//			if (result.length > 0)
//				if (! (result[0] instanceof Person))
//					throw new IllegalArgumentException("Elements of the passed collections have to be of type Person but are "+result[0].getClass().getName());
				
			return result;
		} 
		else
			throw new IllegalArgumentException("InputElement should be a collection but is "+inputElement.getClass().getName());
		
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
	}

}
