package org.nightlabs.jfire.security;

/**
 * This is a tagging interface. All instances implementing this interface must extend
 * the class {@link AuthorizedObject}.
 * <p>
 * This interface is a workaround for JDO constraints: JDO works with persistence-capable classes implementing the same interface
 * and being referenced by this interface, but it does not work with a common non-persistence-capable super-class.
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public interface IAuthorizedObject {
}
