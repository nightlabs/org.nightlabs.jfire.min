package org.nightlabs.jfire.security;

/**
 * Following our design pattern
 * <a href="https://www.jfire.org/modules/phpwiki/index.php/Design%20Pattern%20XyzLocal">Design Pattern XyzLocal</a>,
 * there's often the situation that the primary objects are queried while the corresponding local objects implement
 * the interface {@link SecuredObject}. In order to work as conveniently with the primary objects as with the local
 * objects, the primary objects should implement {@link IndirectlySecuredObject}.
 * <p>
 * This interface can be implemented by all objects that have a direct relationship to a {@link SecuredObject} and
 * authorization (= access control) is indirectly managed via this <code>SecuredObject</code>.
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public interface IndirectlySecuredObject {
	/**
	 * Get the <code>SecuredObject</code> which contains the security configuration applying to this object.
	 * Multiple objects may reference the same <code>SecuredObject</code>.
	 *
	 * @return the <code>SecuredObject</code>.
	 */
	SecuredObject getSecuredObject();
}
