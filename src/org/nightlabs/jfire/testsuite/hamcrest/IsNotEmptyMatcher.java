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
public class IsNotEmptyMatcher  extends TypeSafeMatcher<Collection<?>> {

	  @Override
	  public boolean matchesSafely(Collection<?> collection) {
		  return !collection.isEmpty();
	  }

	  @Override
	  public void describeMismatchSafely(Collection<?> item, Description mismatchDescription) {
		  mismatchDescription.appendValue(item);
	  }

	  public void describeTo(Description description) {
		  description.appendText("an empty collection");
	  }

	  @Factory
	  public static <T> Matcher<Collection<?>> isNotEmpty() {
		  return new IsNotEmptyMatcher();
	  }
}