/**
 * Created Aug 23, 2005, 5:51:35 PM by nick
 */
package org.nightlabs.jfire.security;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 */
public class RoleGroupIDListCarrier implements Serializable
{
  public RoleGroupIDListCarrier()
  {
    excluded = new HashSet();
    assignedToUser = new HashSet();
    assignedToUserGroups = new HashSet();
  }
  
  public Set excluded;
  public Set assignedToUser;
  public Set assignedToUserGroups;
}
