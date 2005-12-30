/*
 * Created on May 18, 2005
 *
 */
package org.nightlabs.jfire.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 */
public class RoleGroupListCarrier implements Serializable
{
  public Collection excluded;
	public Collection assigned;
	public Collection assignedByUserGroup;

  public RoleGroupListCarrier()
  {
    excluded = new HashSet();
    assigned = new HashSet();
    assignedByUserGroup = new HashSet();
  }
}
