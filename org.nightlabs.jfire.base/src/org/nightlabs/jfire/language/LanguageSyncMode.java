package org.nightlabs.jfire.language;

import org.nightlabs.jfire.language.resource.Messages;

public enum LanguageSyncMode {
	oneOnly,
	on,
	off

	;

	@Override
	public String toString() {
		return Messages.getString("org.nightlabs.jfire.language.LanguageSyncMode[" + name() + ']'); //$NON-NLS-1$
	}
}
