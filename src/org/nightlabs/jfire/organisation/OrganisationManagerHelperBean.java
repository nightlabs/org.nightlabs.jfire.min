package org.nightlabs.jfire.organisation;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.FetchPlan;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.moduleregistry.UpdateHistoryItem;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeProblem;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.expression.AndCondition;
import org.nightlabs.jfire.base.expression.Negation;
import org.nightlabs.jfire.base.expression.OrCondition;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructBlockOrderItem;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.validation.ExpressionPropertySetValidator;
import org.nightlabs.jfire.prop.validation.GenericDataFieldNotEmptyExpression;
import org.nightlabs.jfire.prop.validation.ScriptPropertySetValidator;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.AuthorizedObjectRef;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupRef;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.jfire.security.listener.SecurityChangeController;
import org.nightlabs.jfire.server.LocalServer;
import org.nightlabs.jfire.server.Server;
import org.nightlabs.jfire.server.id.ServerID;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.RoleImportSet;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.servermanager.config.ServerCf;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.util.reflect.ReflectUtil;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @ejb.bean
 *		name="jfire/ejb/JFireBaseBean/OrganisationManagerHelper"
 *		jndi-name="jfire/ejb/JFireBaseBean/OrganisationManagerHelper"
 *		type="Stateless"
 *		view-type="local"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 **/
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class OrganisationManagerHelperBean
extends BaseSessionBeanImpl
implements OrganisationManagerHelperLocal
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(OrganisationManagerBean.class);

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_System_")
	@Override
	public void internalInitializeEmptyOrganisation_step0()
	throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			if(logger.isDebugEnabled())
				logger.debug("Initializing JDO meta-data...");

			pm.getExtent(UpdateHistoryItem.class); // This table *must* exist, since it is accessed via plain SQL.
			pm.getExtent(Server.class);
			pm.getExtent(LocalServer.class);
			pm.getExtent(TemporaryOrganisation.class);
			pm.getExtent(Organisation.class);
			pm.getExtent(LocalOrganisation.class);
			pm.getExtent(User.class);
			pm.getExtent(AsyncInvokeProblem.class);

			// It's important to initialise the following classes in this order, because otherwise
			// implementations of IPropertySetValidator / IExpression are said to be missing and an exception.
			// is thrown by DataNucleus. Marco.
			pm.getExtent(Negation.class);
			pm.getExtent(GenericDataFieldNotEmptyExpression.class);
			pm.getExtent(AndCondition.class);
			pm.getExtent(OrCondition.class);

			pm.getExtent(ExpressionPropertySetValidator.class);
			pm.getExtent(ScriptPropertySetValidator.class);

			pm.getExtent(Struct.class);
			pm.getExtent(StructLocal.class);
			pm.getExtent(StructBlockOrderItem.class);

			// BEGIN added 2009-12-07 for PostgreSQL (complete initialisation of all meta-data)
			Set<Class<?>> classesInPackage = new HashSet<Class<?>>();
			Collection<Class<?>> c;

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.asyncinvoke.AsyncInvokeProblem.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.base.DuplicateKeyException.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.base.expression.AndCondition.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.config.Config.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.editlock.EditLock.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.idgenerator.IDNamespace.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.jdo.notification.AbsoluteFilterID.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.jdo.notification.persistent.NotificationBundle.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.language.Language.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.multitxjob.MultiTxJob.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.organisation.Organisation.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.person.Person.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.prop.AbstractStruct.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutConfigModule.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.prop.datafield.DateDataField.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.prop.i18n.I18nDataFieldText.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.prop.search.DisplayNameSearchFilterItem.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.prop.structfield.DateStructField.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.prop.validation.DataBlockValidator.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.security.Authority.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.security.listener.SecurityChangeController.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.security.search.UserQuery.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.server.LocalServer.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.timer.Task.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.workstation.Workstation.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			c = ReflectUtil.listClassesInPackage(org.nightlabs.jfire.workstation.search.WorkstationQuery.class.getPackage().getName(), false);
			classesInPackage.addAll(c);

			for (Class<?> clazz : classesInPackage) {
				boolean isPersistenceCapable = false;
				Annotation[] declaredAnnotations = clazz.getDeclaredAnnotations();
				for (Annotation annotation : declaredAnnotations) {
					if (javax.jdo.annotations.PersistenceCapable.class == annotation.annotationType())
						isPersistenceCapable = true;
				}
//				if (!PersistenceCapable.class.isAssignableFrom(clazz)) {
				if (!isPersistenceCapable) {
					if(logger.isDebugEnabled())
						logger.debug("internalInitializeEmptyOrganisation_step0: Ignoring non-persistence-capable class: " + clazz.getName());

					continue;
				}

				if(logger.isDebugEnabled())
					logger.debug("internalInitializeEmptyOrganisation_step0: Initializing meta-data for class: " + clazz.getName());

				try {
					pm.getExtent(clazz);
				} catch (Exception x) {
					logger.warn("internalInitializeEmptyOrganisation_step0: Initializing meta-data for class \"" + clazz.getName() + "\" failed: " + x.getClass().getName() + ": " + x.getMessage(), x);
				}
			}
			// END added 2009-12-07 for PostgreSQL (complete initialisation of all meta-data)

			if(logger.isDebugEnabled())
				logger.debug("Initializing JDO meta-data done.");

		} catch (Exception x) {
			logger.error("internalInitializeEmptyOrganisation_step0: " + x.getClass().getName() + ": " + x.getMessage(), x);
			throw x;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.organisation.OrganisationManagerHelperLocal#internalInitializeEmptyOrganisation_step1(org.nightlabs.jfire.servermanager.config.ServerCf, org.nightlabs.jfire.servermanager.config.OrganisationCf, java.lang.String, java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_System_")
	@Override
	public void internalInitializeEmptyOrganisation_step1(
			ServerCf localServerCf,
			OrganisationCf organisationCf,
			String userID, String password
	)
	throws Exception
	{
		String organisationID = getOrganisationID();
		PersistenceManager pm = createPersistenceManager();
		try {
			if(logger.isDebugEnabled())
				logger.debug("Initializing default person structure...");

			PersonStruct.getPersonStructLocal(pm);

			if(logger.isDebugEnabled())
				logger.debug("Initializing default person structure done.");


			ServerID serverID = ServerID.create(localServerCf.getServerID());
			try {
				Server server = (Server) pm.getObjectById(serverID);
				server.getServerName();

				// It seems this method was already executed successfully => return.
				if(logger.isDebugEnabled())
					logger.debug("internalInitializeEmptyOrganisation_step1: This step was already done successfully before; skipping it.");

				return;
			} catch (JDOObjectNotFoundException x) {
				// fine, it doesn't exist.
			}

			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object LocalServer...");
			Server server = localServerCf.createServer(pm);
			LocalServer localServer = new LocalServer(server);
			if (logger.isDebugEnabled()) {
				logger.debug("Persisting LocalServer: ");
				logger.debug("  serverID                 = " + localServer.getServerID());
				logger.debug("  server.serverName        = " + localServer.getServer().getServerName());
				logger.debug("  server.j2eeServerType    = " + localServer.getServer().getJ2eeServerType());
				for (Map.Entry<String, String> me: localServer.getServer().getProtocol2initialContextURL().entrySet()) {
					logger.debug("  server.initialContextURL[" + me.getKey() + "] = " + me.getValue());
				}
			}
			localServer = pm.makePersistent(localServer);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(localServer) done.");

			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object LocalOrganisation...");

			pm.getFetchPlan().setGroup(FetchPlan.ALL);

			Organisation organisation = organisationCf.createOrganisation(pm, server);
			LocalOrganisation localOrganisation = new LocalOrganisation(organisation);
			localOrganisation = pm.makePersistent(localOrganisation);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(localOrganisation) done.");

			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object User with ID \""+User.USER_ID_OTHER+"\"...");
			User otherUser = new User(organisationID, User.USER_ID_OTHER);
			new UserLocal(otherUser);
			otherUser = pm.makePersistent(otherUser);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(otherUser) done.");

			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object User with ID \""+userID+"\"...");
			User user = new User(organisationID, userID);
			UserLocal userLocal = new UserLocal(user);
			userLocal.setPasswordPlain(password);
			user = pm.makePersistent(user);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(user) done.");

			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object Workstation with ID \"" + Workstation.WORKSTATION_ID_FALLBACK + "\"...");
			Workstation workstation = new Workstation(organisationID, Workstation.WORKSTATION_ID_FALLBACK);
			workstation = pm.makePersistent(workstation);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(workstation) done.");

			// AuthorityType.AUTHORITY_TYPE_ID_SELF
			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object AuthorityType with ID \""+AuthorityType.AUTHORITY_TYPE_ID_SELF+"\"...");
			AuthorityType authorityType = new AuthorityType(AuthorityType.AUTHORITY_TYPE_ID_SELF);
			// All properties of this authority-type are now configured in the jfire-security.xml - we create it here, though, because
			// this way it's guaranteed to exist and consistent with how we handle the AuthorityType.AUTHORITY_TYPE_ID_ORGANISATION.
			authorityType = pm.makePersistent(authorityType);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(authorityType) done.");


			// AuthorityType.AUTHORITY_TYPE_ID_ORGANISATION
			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object AuthorityType with ID \""+AuthorityType.AUTHORITY_TYPE_ID_ORGANISATION+"\"...");
			authorityType = new AuthorityType(AuthorityType.AUTHORITY_TYPE_ID_ORGANISATION);
			// All properties of this authority-type are now configured in the jfire-security.xml - we create it here, though, because
			// this way it's guaranteed to exist and we need it to create the special Authority _Organisation_ below.
			authorityType = pm.makePersistent(authorityType);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(authorityType) done.");


			// Authority.AUTHORITY_ID_ORGANISATION
			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object Authority with ID \""+Authority.AUTHORITY_ID_ORGANISATION+"\"...");
			Authority authority = new Authority(organisationID, Authority.AUTHORITY_ID_ORGANISATION, authorityType);

			authority.getName().setText(Locale.ENGLISH.getLanguage(), "Organisation");
			authority.getDescription().setText(Locale.ENGLISH.getLanguage(), "This authority controls the access rights to the organisation as a whole.");

			authority.getName().setText(Locale.GERMAN.getLanguage(), "Organisation");
			authority.getDescription().setText(Locale.GERMAN.getLanguage(), "Diese Vollmacht kontrolliert den Zugriff auf die Organisation im ganzen.");

			authority = pm.makePersistent(authority);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(authority) done.");

		} catch (Exception x) {
			logger.error("internalInitializeEmptyOrganisation_step1: " + x.getClass().getName() + ": " + x.getMessage(), x);
			throw x;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.organisation.OrganisationManagerHelperLocal#internalInitializeEmptyOrganisation_step2()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_System_")
	@Override
	public void internalInitializeEmptyOrganisation_step2() throws Exception
	{
		String organisationID = getOrganisationID();
		JFireServerManager jfsm = getJFireServerManager();
		try {
			// import all roles
			if(logger.isDebugEnabled())
				logger.debug("Importing all roles and role groups...");
			RoleImportSet roleImportSet = jfsm.roleImport_prepare(organisationID);
			jfsm.roleImport_commit(roleImportSet);
			if(logger.isDebugEnabled())
				logger.debug("Import of roles and role groups done.");
		} catch (Exception x) {
			logger.error("internalInitializeEmptyOrganisation_step2: " + x.getClass().getName() + ": " + x.getMessage(), x);
			throw x;
		} finally {
			jfsm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.organisation.OrganisationManagerHelperLocal#internalInitializeEmptyOrganisation_step3(org.nightlabs.jfire.servermanager.config.ServerCf, org.nightlabs.jfire.servermanager.config.OrganisationCf, java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_System_")
	@Override
	public void internalInitializeEmptyOrganisation_step3(
			ServerCf localServerCf,
			OrganisationCf organisationCf,
			String userID
	)
	throws Exception
	{
		String organisationID = getOrganisationID();
		PersistenceManager pm = createPersistenceManager();
		try {
			boolean successful = false;
			SecurityChangeController.beginChanging();
			try {
//				if(logger.isDebugEnabled())
//				logger.debug("Loading previously created AuthorityType (" + AuthorityType.AUTHORITY_TYPE_ID_SELF + ") from datastore and assigning role-groups...");

//				AuthorityType authorityType = (AuthorityType) pm.getObjectById(AuthorityType.AUTHORITY_TYPE_ID_SELF);
//				authorityType.addRoleGroup((RoleGroup) pm.getObjectById(RoleGroupConstants.securityManager_editAuthority));

//				if(logger.isDebugEnabled())
//				logger.debug("Loading previously created AuthorityType (" + AuthorityType.AUTHORITY_TYPE_ID_SELF + ") from datastore and assigning role-groups done.");


				if(logger.isDebugEnabled())
					logger.debug("Loading previously created Authority from datastore...");

				Authority authority = (Authority) pm.getObjectById(AuthorityID.create(
						organisationID, Authority.AUTHORITY_ID_ORGANISATION));

				if(logger.isDebugEnabled())
					logger.debug("Loading previously created Authority from datastore done.");


				if(logger.isDebugEnabled())
					logger.debug("Creating instances of AuthorizedObjectRef for both Users within the default authority...");

				UserLocal otherUserLocal = (UserLocal) pm.getObjectById(UserLocalID.create(organisationID, User.USER_ID_OTHER, organisationID));
				UserLocal userLocal = (UserLocal) pm.getObjectById(UserLocalID.create(organisationID, userID, organisationID));
				authority.createAuthorizedObjectRef(otherUserLocal);
				AuthorizedObjectRef userRef = authority.createAuthorizedObjectRef(userLocal);
				if(logger.isDebugEnabled())
					logger.debug("Creating instances of AuthorizedObjectRef for both Users within the default authority done.");

				// Give the user all RoleGroups.
				if(logger.isDebugEnabled())
					logger.debug("Assign all RoleGroups to the user \""+userID+"\"...");
				for (Iterator<RoleGroup> it = pm.getExtent(RoleGroup.class).iterator(); it.hasNext(); ) {
					RoleGroup roleGroup = it.next();
					RoleGroupRef roleGroupRef = authority.createRoleGroupRef(roleGroup);
					userRef.addRoleGroupRef(roleGroupRef);
				}
				if(logger.isDebugEnabled())
					logger.debug("Assigning all RoleGroups to user \""+userID+"\" done.");

				// create system user
				if(logger.isDebugEnabled())
					logger.debug("Creating system user...");
				User systemUser = new User(organisationID, User.USER_ID_SYSTEM);
				new UserLocal(systemUser);
				pm.makePersistent(systemUser);
				if(logger.isDebugEnabled())
					logger.debug("System user created.");

				successful = true;
			} finally {
				SecurityChangeController.endChanging(successful);
			}

		} catch (Exception x) {
			logger.error("internalInitializeEmptyOrganisation_step3: " + x.getClass().getName() + ": " + x.getMessage(), x);
			throw x;
		} finally {
			pm.close();
		}
	}
}
