package org.nightlabs.jfire.workstation;

/**
 * This enum controls how {@link Workstation#getWorkstation(javax.jdo.PersistenceManager, WorkstationResolveStrategy)}
 * and the other <code>getWorkstation(...)</code> methods behave when the currently logged-in principal does not have
 * a workstation specified.
 * <p>
 * There are use-cases, when a workstation is absolutely necessary (e.g. for the online-update) while other use cases
 * simply need some default values. In the latter case, they can easily be taken from a fallback configuration, if the user did not
 * specify a workstation. Since it is use-case-dependent what should happen when the workstation-id is not specified,
 * the developer of the specific use-case can define by one of the values of this enum, how the system should behave.
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public enum WorkstationResolveStrategy {
	/**
	 * Return <code>null</code>.
	 */
	NULL,
	/**
	 * Throw a {@link WorkstationNotSpecifiedException}.
	 */
	EXCEPTION,
	/**
	 * Return the fallback-workstation instead.
	 */
	FALLBACK
}
