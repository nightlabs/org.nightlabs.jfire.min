/*
 * Created 	on Aug 12, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.spi.PersistenceCapable;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.nightlabs.base.notification.SelectionManager;
import org.nightlabs.ipanema.base.jdo.JDOObjectID2PCClassMap;
import org.nightlabs.ipanema.base.login.Login;
import org.nightlabs.ipanema.config.id.ConfigID;
import org.nightlabs.ipanema.rcp.notification.SelectionNotificationProxy;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.SubjectCarrier;

/**
 * SelectionListener based on SelectionNotificationProxy that additionally
 * triggers selections for a ConfigID when an object or its ObjectID from a set 
 * of configuration-link objecttypes was selected. 
 * This set of objecttypes can be configured through the 
 * conifgtypeset extension-point. 
 * 
 * @see org.nightlabs.ipanema.base.config.ConfigTypeRegistry
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ConfigLinkSelectionNotificationProxy extends
		SelectionNotificationProxy {
	
	private static Logger LOGGER = Logger.getLogger(ConfigLinkSelectionNotificationProxy.class);

	/**
	 * @param source
	 */
	public ConfigLinkSelectionNotificationProxy(Object source) {
		super(source);
	}

	/**
	 * @param source
	 * @param zone
	 * @param ignoreInheritance
	 */
	public ConfigLinkSelectionNotificationProxy(Object source, String zone,
			boolean ignoreInheritance) {
		super(source, zone, ignoreInheritance);
	}

	/**
	 * Triggers 
	 * @param object
	 * @return
	 */
	private Object buildNotificationSubject(Object object) {
		Class objectClass = null;
		ObjectID objectID = null;
		if (object instanceof ObjectID) {
			// ask jdo service for object class name
			Class jdoObjectClass = JDOObjectID2PCClassMap.sharedInstance().getPersistenceCapableClass(object);
			if (jdoObjectClass != null) {				
				objectClass = jdoObjectClass;
				objectID = (ObjectID)object;
			}
		}
		else if (object instanceof PersistenceCapable) {
			// get ObjectID for PC class
			Object idObject = JDOHelper.getObjectId(object); 
			if (!(idObject instanceof ObjectID)) {
				LOGGER.warn("ID-object of PersistenceCapable selection object was not an instance of ObjectID but "+idObject.getClass().getName()+" and was ignored.");
				return null;				
			}
			objectID = (ObjectID)idObject;
			objectClass = object.getClass();
		}
		else
			// silently do nothing
			return null;
		
		// Exit when no registration on the current class is found 
		if (!ConfigSetupRegistry.sharedInstance().containsRegistrationForLinkClass(objectClass))
			return null;
		
		// TODO: Ensure here always the current users organisationID can be used 
		// if not, extend configTypeSet extensionpoint and registry with 
		// interface ConfigLinkIDProvider
		try {
			return ConfigID.create(Login.getLogin().getOrganisationID(), objectID, objectClass);
		} catch (LoginException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void selectionChanged(SelectionChangedEvent event) 
	{
		// first do the notification of the selected object itself
		super.selectionChanged(event);
		
		// no check for config-links
    ISelection selection = event.getSelection();
    if(!selection.isEmpty())
    {    	
			NotificationEvent e = null;
    	if(selection instanceof IStructuredSelection)
    	{
    		List subjects = new ArrayList();
    		Iterator i = ((IStructuredSelection)selection).iterator();
    		while(i.hasNext())
    		{
    			Object o = buildNotificationSubject(i.next());
					if (o != null)
						subjects.add(o);
    		}
				if (subjects.size() < 1)
					return;
				SubjectCarrier[] subjectCarriers = new SubjectCarrier[subjects.size()];
				for (int j = 0; j < subjects.size(); j++) {
					subjectCarriers[j] = new SubjectCarrier(subjects.get(j));
					subjectCarriers[j].setInheritanceIgnored(ignoreInheritance);
				}
        e = new NotificationEvent(source, zone, subjectCarriers);
    	}
    	SelectionManager.sharedInstance().notify(e);
    }
	}
}
