/**
 * 
 */
package org.nightlabs.jfire.base.prop.structedit;

import java.rmi.RemoteException;
import java.util.Collection;

import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.prop.PropertyManager;
import org.nightlabs.jfire.prop.PropertyManagerUtil;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.id.StructID;
import org.nightlabs.jfire.prop.id.StructLocalID;

/**
 * @author alex
 *
 */
public class StructEditorUtil {

	public static PropertyManager getPropertyManager() {
		try {
			return PropertyManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static Collection<StructID> getAvailableStructIDs() {
		try {
			return getPropertyManager().getAvailableStructIDs();
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static Collection<StructLocalID> getAvailableStructLocalIDs() {
		try {
			return getPropertyManager().getAvailableStructLocalIDs();
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static long getDataFieldInstanceCount(StructFieldID structFieldID) {
		try {
			return getPropertyManager().getDataFieldInstanceCount(structFieldID);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
