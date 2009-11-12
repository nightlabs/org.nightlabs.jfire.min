package org.nightlabs.jfire.person;

import org.nightlabs.jfire.prop.search.PropSearchFilter;

public class PersonSearchFilter extends PropSearchFilter
{
	private static final long serialVersionUID = 1L;

	public PersonSearchFilter() {
	}

	public PersonSearchFilter(int _conjunction) {
		super(_conjunction);
	}

	@Override
	protected Class<?> initCandidateClass() {
		return Person.class;
	}

}
