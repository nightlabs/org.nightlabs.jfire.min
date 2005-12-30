/**
 * Created Aug 24, 2005, 6:20:55 PM by nick
 */
package org.nightlabs.jfire.security;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 */
public class UserGroupListCarrier implements Serializable
{
  public Set assigned;
  public Set excluded;
  
  public UserGroupListCarrier()
  {
    assigned = new HashSet();
    excluded = new HashSet();
  }
}
