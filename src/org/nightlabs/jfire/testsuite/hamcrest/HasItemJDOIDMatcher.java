package org.nightlabs.jfire.testsuite.hamcrest;

import java.util.Collection;

import javax.jdo.JDOHelper;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.nightlabs.jdo.ObjectID;


/**
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 *
 * very useful Matcher to quick matches of an ObjectID exists among the list of JDO Objects 
 *
 */
public class HasItemJDOIDMatcher extends TypeSafeMatcher<Collection<Object>> {

	
    private final ObjectID objectID;
    
    public HasItemJDOIDMatcher(ObjectID objectID) {
        if (objectID == null) {
            throw new IllegalArgumentException("Non-null value required by HasJDOItemID()");
        }
        this.objectID = objectID;
    }
	
	  @Override
	  public boolean matchesSafely(Collection<Object> collections) {
		  Collection<Object> objectIDs =  JDOHelper.getObjectIds(collections);
		 return objectIDs.contains(objectID);
	  }

	  @Override
	  public void describeMismatchSafely(Collection<Object> item, Description mismatchDescription) {
		  mismatchDescription.appendValue(item);
	  }

	  public void describeTo(Description description) {
		  description.appendText("an empty collection");
	  }

	  @Factory
	  public static <T> Matcher<Collection<Object>> HasItemJDOID(ObjectID objectID) {
		  return new HasItemJDOIDMatcher(objectID);
	  }
}
