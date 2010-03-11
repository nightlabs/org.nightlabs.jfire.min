package org.nightlabs.jfire.testsuite.hamcrest;


import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;


/**
*
* a custom Hamcrest Matcher to assert a certain collection does not have empty content.
* 
* @author Fitas Amine - fitas [at] nightlabs [dot] de
*/
public class IsNotEmptyMatcher  extends TypeSafeMatcher<Collection<?>> {

	  @Override
	  public boolean matchesSafely(Collection<?> collection) {
		  return !collection.isEmpty();
	  }

	  public void describeTo(Description description) {
	    description.appendText("should not be an Empty Collection/Set");
	  }
	  
	  @Factory
	  public static <T> Matcher<Collection<?>> isNotEmpty() {
	    return new IsNotEmptyMatcher();
	  }
}