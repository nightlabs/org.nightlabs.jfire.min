/**
 * Created Aug 23, 2005, 6:19:27 PM by nick
 */
package org.nightlabs.jfire.security;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 */
public class UserGroupIDListCarrier implements Serializable
{
  public UserGroupIDListCarrier()
  {
    excluded = new HashSet();
    assigned = new HashSet();
  }
  
  public Set excluded;
  public Set assigned;
}
