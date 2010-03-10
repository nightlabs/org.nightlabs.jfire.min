package org.nightlabs.jfire.testsuite.hamcrest;


import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;



public class IsEmptyMatcher  extends TypeSafeMatcher<Collection> {

	  @Override
	  public boolean matchesSafely(Collection collection) {
	    return collection.isEmpty();
	  }

	  public void describeTo(Description description) {
	    description.appendText("is not Empty Collection");
	  }
	  
	  @Factory
	  public static <T> Matcher<Collection> isEmpty() {
	    return new IsEmptyMatcher();
	  }
}