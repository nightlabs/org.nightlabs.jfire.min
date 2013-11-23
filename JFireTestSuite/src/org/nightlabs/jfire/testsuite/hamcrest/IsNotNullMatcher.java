package org.nightlabs.jfire.testsuite.hamcrest;


import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;


/**
*
* a custom Hamcrest Matcher to assert a certain collection is not Null and empty.
* 
* @author Fitas Amine - fitas [at] nightlabs [dot] de
*/
public class IsNotNullMatcher  extends TypeSafeMatcher<Collection<?>> {

	  @Override
	  public boolean matchesSafely(Collection<?> collection) {		  
		return (collection == null) ? false : true;
	  }

	  public void describeTo(Description description) {
	    description.appendText("should not be an Empty Collection/Set");
	  }
	  
	  @Factory
	  public static <T> Matcher<Collection<?>> isNotNull() {
	    return new IsNotNullMatcher();
	  }
}