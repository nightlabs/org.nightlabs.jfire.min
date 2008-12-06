package org.nightlabs.jfire.jboss.cascadedauthentication;

import java.security.Principal;
import java.util.Set;

import org.jboss.security.RunAsIdentity;
import org.nightlabs.jfire.base.JFireBasePrincipal;
import org.nightlabs.jfire.base.JFirePrincipalContainer;

class CascadedAuthenticationRunAsIdentity
extends RunAsIdentity
implements JFirePrincipalContainer
{
	private static final long serialVersionUID = 1L;

	private JFireBasePrincipal jfirePrincipal;
	private RunAsIdentity delegate;

	public CascadedAuthenticationRunAsIdentity(RunAsIdentity delegate, JFireBasePrincipal jfirePrincipal) {
		super("", delegate.getName());

		if (delegate == null)
			throw new IllegalArgumentException("delegate == null");

		if (jfirePrincipal == null)
			throw new IllegalArgumentException("jfirePrincipal == null");

		if (!delegate.getName().equals(jfirePrincipal.getName()))
			throw new IllegalArgumentException("runAsIdentity.name != jfirePrincipal.name :: " + delegate.getName() + " != " + jfirePrincipal.getName());

		this.delegate = delegate;
		this.jfirePrincipal = jfirePrincipal;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		CascadedAuthenticationRunAsIdentity clone = (CascadedAuthenticationRunAsIdentity) super.clone();
		clone.delegate = (RunAsIdentity) delegate.clone();
		return clone;
	}

	@Override
	public boolean doesUserHaveRole(Principal role) {
		return delegate.doesUserHaveRole(role);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean doesUserHaveRole(Set methodRoles) {
		return delegate.doesUserHaveRole(methodRoles);
	}

	@Override
	public boolean equals(Object another) {
		if (this == another) return true;
		if (this.getClass() == another.getClass()) {
			CascadedAuthenticationRunAsIdentity o = (CascadedAuthenticationRunAsIdentity) another;
			return this.delegate.equals(o.delegate);
		}
		if (!(another instanceof RunAsIdentity))
			return false;

		RunAsIdentity o = (RunAsIdentity) another;
		return delegate.equals(o);
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set getPrincipalsSet() {
		return delegate.getPrincipalsSet();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set getRunAsRoles() {
		return delegate.getRunAsRoles();
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public JFireBasePrincipal getJFirePrincipal() {
		return jfirePrincipal;
	}
}
