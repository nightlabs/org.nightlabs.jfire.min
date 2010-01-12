package org.nightlabs.jfire.timer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DetachCallback;
import javax.jdo.listener.StoreCallback;
import javax.jdo.spi.PersistenceCapable;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jdo.timepattern.TimePatternSetJDOImpl;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.math.Base36Coder;
import org.nightlabs.timepattern.TimePattern;
import org.nightlabs.timepattern.TimePatternSet;
import org.nightlabs.util.Util;

/**
 * A <code>Task</code> specifies an action that will be executed once or repeatedly at certain
 * configurable times.
 * <p>
 * Therefore it defines the JNDI name of a session bean, one of its methods, the user who owns
 * this task and a {@link TimePatternSet} which controls the execution time(s).
 * </p>
 * <p>
 * A howto can be found on https://www.jfire.org/modules/phpwiki/index.php/HowToUseTheTimer
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.timer.id.TaskID"
 *		detachable="true"
 *		table="JFireBase_Task"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, taskTypeID, taskID"
 *
 * @jdo.version strategy="version-number"
 *
 * @jdo.fetch-group name="Task.user" fields="user"
 * @jdo.fetch-group name="Task.timePatternSet" fields="timePatternSet"
 * @jdo.fetch-group name="Task.name" fields="name"
 * @jdo.fetch-group name="Task.description" fields="description"
 * @jdo.fetch-group name="Task.this" fields="user, timePatternSet, name, description"
 *
 * @jdo.query
 *		name="getTasksToDo"
 *		query="SELECT
 *				WHERE this.enabled && !this.executing && this.nextExecDT <= :untilTime
 *				ORDER BY this.nextExecDT ascending"
 *
 * @jdo.query
 *		name="getTasksToRecalculateNextExecDT"
 *		query="SELECT
 *				WHERE this.nextCalculateNextExecDT <= :untilTime
 *				ORDER BY this.nextCalculateNextExecDT ascending"
 *
 * @jdo.query
 *		name="getTasksByExecuting"
 *		query="SELECT
 *				WHERE this.executing == :executing"
 *
 *
 * @jdo.query
 *		name="getTasksByTaskTypeID"
 *		query="SELECT
 *				WHERE this.taskTypeID == :pTaskTypeID"
 */
@javax.jdo.annotations.PersistenceCapable(
	objectIdClass=TaskID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Task")
@Version(strategy=VersionStrategy.VERSION_NUMBER)
@FetchGroups({
	@FetchGroup(
		name=Task.FETCH_GROUP_USER,
		members=@Persistent(name="user")),
	@FetchGroup(
		name=Task.FETCH_GROUP_TIME_PATTERN_SET,
		members=@Persistent(name="timePatternSet")),
	@FetchGroup(
		name=Task.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name=Task.FETCH_GROUP_DESCRIPTION,
		members=@Persistent(name="description")),
	@FetchGroup(
		name=Task.FETCH_THIS_TASK,
		members={@Persistent(name="user"), @Persistent(name="timePatternSet"), @Persistent(name="name"), @Persistent(name="description")})
})
@Queries({
	@Query(
		name="getTasksToDo",
		value="SELECT WHERE this.enabled && !this.executing && this.nextExecDT <= :untilTime ORDER BY this.nextExecDT ascending"),
	@Query(
		name="getTasksToRecalculateNextExecDT",
		value="SELECT WHERE this.nextCalculateNextExecDT <= :untilTime ORDER BY this.nextCalculateNextExecDT ascending"),
	@Query(
		name="getTasksByExecuting",
		value="SELECT WHERE this.executing == :executing"),
	@Query(
		name="getTasksByTaskTypeID",
		value="SELECT WHERE this.taskTypeID == :pTaskTypeID")
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Task
implements Serializable, DetachCallback, AttachCallback, StoreCallback
{
	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(Task.class);

	public static final String FETCH_GROUP_USER = "Task.user";
	public static final String FETCH_GROUP_TIME_PATTERN_SET = "Task.timePatternSet";
	public static final String FETCH_GROUP_NAME = "Task.name";
	public static final String FETCH_GROUP_DESCRIPTION = "Task.description";
	public static final String FETCH_THIS_TASK = "Task.this";

	/**
	 * This is no real fetch-group and will instead be processed manually by {@link #jdoPostDetach(Object)}.
	 */
	public static final String FETCH_GROUP_PARAM = "Task.param";

	/**
	 * This is a predefined task type identifier (see {@link #getTaskTypeID()}) specifying
	 * system internal tasks. You should declare and use own types for other purposes (e.g.
	 * scheduled reports), while it's urgently recommended to use this type for all
	 * system jobs (e.g. clean up temporary data, synchronize with external systems etc.).
	 */
	public static final String TASK_TYPE_ID_SYSTEM = "System";

	/**
	 * Queries all those {@link Task}s that are <code>enabled</code>, not in
	 * state <code>executing</code> and have their <code>nextExecDT</code>
	 * either before or at the time specified by <code>until</code>.
	 *
	 * @param pm The persistence manager used to access the datastore.
	 * @param until The timestamp at which the tasks are due (normally NOW - i.e. <code>new Date()</code>).
	 * @return Returns instances of {@link Task}.
	 */
	public static List<Task> getTasksToDo(PersistenceManager pm, Date until)
	{
		return (List<Task>) pm.newNamedQuery(Task.class, "getTasksToDo").execute(until);
	}

	/**
	 * Queries all those {@link Task}s that need their <code>nextExecDT</code> recalculated,
	 * because it was too far in the future at the last calculation.
	 *
	 * @param pm The persistence manager used to access the datastore.
	 * @param until The timestamp at which the recalculation is due (normally NOW - i.e. <code>new Date()</code>).
	 * @return Returns instances of {@link Task}.
	 */
	public static List<Task> getTasksToRecalculateNextExecDT(PersistenceManager pm, Date until)
	{
		return (List<Task>) pm.newNamedQuery(Task.class, "getTasksToRecalculateNextExecDT").execute(until);
	}

	public static List<Task> getTasksByExecuting(PersistenceManager pm, boolean executing)
	{
		return (List<Task>) pm.newNamedQuery(Task.class, "getTasksByExecuting").execute(Boolean.valueOf(executing));
	}

	public static List<Task> getTasksByTaskTypeID(PersistenceManager pm, String taskTypeID)
	{
		return (List<Task>) pm.newNamedQuery(Task.class, "getTasksByTaskTypeID").execute(taskTypeID);
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String taskTypeID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String taskID;

	/*
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="task"
	 */

	/**
	 * @!jdo.field persistence-modifier="persistent" embedded="true" null-value="exception"
	 * @!jdo.embedded owner-field="task"
	 *
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="task"
	 */
	@Persistent(
		dependent="true",
		mappedBy="task",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private TaskName name;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="task"
	 */
	@Persistent(
		dependent="true",
		mappedBy="task",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private TaskDescription description;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected Task()
	{
	}

	/**
	 * Constructor. Note, that you must configure the {@link TimePatternSet} and enable the task,
	 * before it will be executed!
	 *
	 * @param taskID The jdo-object-id of the new task.
	 * @param user the user who owns the task (the task will be executed as this user).
	 * @param bean the remote (or local) interface of the bean that shall be triggered.
	 * @param method The method name of the specified bean. It must exactly take one parameter of type {@link TaskID}.
	 *
	 * @see #getTimePatternSet()
	 * @see #setEnabled(boolean)
	 */
	public Task(
			TaskID taskID,
			User user,
			Class<?> bean,
			String method)
	{
		this(taskID.organisationID, taskID.taskTypeID, taskID.taskID, user, bean.getName(), method);
	}

	/**
	 * Constructor. Note, that you must configure the {@link TimePatternSet} and enable the task,
	 * before it will be executed!
	 *
	 * @param taskID The jdo-object-id of the new task.
	 * @param user the user who owns the task (the task will be executed as this user).
	 * @param bean the fully qualified class name of the bean's remote (or local) interface that shall be triggered.
	 * @param method The method name of the specified bean. It must exactly take one parameter of type {@link TaskID}.
	 *
	 * @see #getTimePatternSet()
	 * @see #setEnabled(boolean)
	 */
	protected Task(
			TaskID taskID,
			User user,
			String bean,
			String method)
	{
		this(taskID.organisationID, taskID.taskTypeID, taskID.taskID, user, bean, method);
	}

	/**
	 * Constructor. Note, that you must configure the {@link TimePatternSet} and enable the task,
	 * before it will be executed!
	 *
	 * @param organisationID 1st part of the PK. The owner organisation.
	 * @param taskTypeID 2nd part of the PK. What kind of task is it. See {@link #getTaskTypeID()} for details.
	 * @param taskID 3rd part of the PK. An identifier.
	 * @param user the user who owns the task (the task will be executed as this user).
	 * @param bean the fully qualified class name of the bean's remote (or local) interface that shall be triggered.
	 * @param method The method name of the specified bean. It must exactly take one parameter of type {@link TaskID}.
	 *
	 * @see #Task(TaskID, User, String, String)
	 * @see #getTimePatternSet()
	 * @see #setEnabled(boolean)
	 */
	public Task(
			String organisationID,
			String taskTypeID,
			String taskID,
			User user,
			Class<?> bean,
			String method)
	{
		this(organisationID, taskTypeID, taskID, user, bean.getName(), method);
	}

	/**
	 * Constructor. Note, that you must configure the {@link TimePatternSet} and enable the task,
	 * before it will be executed!
	 *
	 * @param organisationID 1st part of the PK. The owner organisation.
	 * @param taskTypeID 2nd part of the PK. What kind of task is it. See {@link #getTaskTypeID()} for details.
	 * @param taskID 3rd part of the PK. An identifier.
	 * @param user the user who owns the task (the task will be executed as this user).
	 * @param bean the fully qualified class name of the bean's remote (or local) interface that shall be triggered.
	 * @param method The method name of the specified bean. It must exactly take one parameter of type {@link TaskID}.
	 *
	 * @see #Task(TaskID, User, String, String)
	 * @see #getTimePatternSet()
	 * @see #setEnabled(boolean)
	 */
	protected Task(
			String organisationID,
			String taskTypeID,
			String taskID,
			User user,
			String bean,
			String method)
	{
		ObjectIDUtil.assertValidIDString(organisationID, "organisationID");
		ObjectIDUtil.assertValidIDString(taskTypeID, "taskTypeID");
		ObjectIDUtil.assertValidIDString(taskID, "taskID");

		if (user == null)
			throw new IllegalArgumentException("user must not be null!");

		this.organisationID = organisationID;
		this.taskTypeID = taskTypeID;
		this.taskID = taskID;
		this.user = user;
		setBean(bean);
		setMethod(method);
		name = new TaskName(this);
		description = new TaskDescription(this);
		timePatternSet = new TimePatternSetJDOImpl(
				organisationID + '/' + Base36Coder.sharedInstance(false).encode(IDGenerator.nextID(TimePatternSetJDOImpl.class), 1));
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * Every task has a type which can be any valid ID String. <code>Task</code> itself defines
	 * only {@link #TASK_TYPE_ID_SYSTEM}, but you can define whatevery types you like (e.g. one
	 * for scheduled reports).
	 *
	 * @return the task's type identifier.
	 */
	public String getTaskTypeID()
	{
		return taskTypeID;
	}
	public String getTaskID()
	{
		return taskID;
	}

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	@Persistent(
		dependent="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private TimePatternSetJDOImpl timePatternSet;

	/**
	 * The {@link TimePatternSet} defines (by multiple {@link TimePattern}s) when
	 * this task shall be executed. It can be executed once or regularly. The
	 * definition is similar to UNIX cron.
	 *
	 * @return the time configuration of this task.
	 */
	public TimePatternSetJDOImpl getTimePatternSet()
	{
		return timePatternSet;
	}

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private User user;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Workstation workstation;

	/**
	 * @return The user who owns this task. The EJB method will be called as this user.
	 */
	public User getUser()
	{
		return user;
	}

	/**
	 * Get the optional workstation or <code>null</code> if none is defined.
	 *
	 * @return the workstation or <code>null</code>.
	 */
	public Workstation getWorkstation() {
		return workstation;
	}
	/**
	 * Set the optional workstation or <code>null</code>.
	 *
	 * @param workstation the workstation or <code>null</code>
	 */
	public void setWorkstation(Workstation workstation) {
		if (workstation != null && !workstation.getOrganisationID().equals(user.getOrganisationID()))
			throw new IllegalArgumentException("workstation.organisationID != user.organisationID");

		this.workstation = workstation;
	}

	/**
	 * Every task should have a name. This is an I18nText (max 255 chars).
	 *
	 * @return the task's name.
	 */
	public TaskName getName()
	{
		return name;
	}

	/**
	 * It's good practice not only to specify a name (see {@link #getName()}), but as
	 * well a longer description that makes clear in detail what exactly the Task is
	 * good for.
	 *
	 * @return the task's description.
	 */
	public TaskDescription getDescription()
	{
		return description;
	}

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private String bean;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private String method;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String paramObjectIDStr;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private Object param = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date lastExecDT = null;

	private long lastExecDurationMSec;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean lastExecFailed;

	/**
	 * This is <code>null</code>, if the last execution was fine, otherwise
	 * this is the Exception message (no stacktrace).
	 *
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column sql-type="CLOB"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="CLOB")
	private String lastExecMessage;

	/**
	 * This is <code>null</code>, if the last execution was fine, otherwise
	 * this is the Exception stacktrace.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column sql-type="CLOB"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="CLOB")
	private String lastExecStackTrace;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date nextExecDT = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date nextCalculateNextExecDT = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean enabled;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String activeExecID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean executing;

	/**
	 * @return The JNDI name of the EJB that will be called.
	 */
	public String getBean()
	{
		return bean;
	}
	public void setBean(String bean)
	{
		if (bean == null)
			throw new IllegalArgumentException("bean must not be null!");

		this.bean = bean;
	}

	/**
	 * @return The method name of the EJB that will be called. This method must have exactly one parameter
	 *		of type {@link TaskID}. The ID of the Task to be called will be passed this way allowing to access
	 *		the real parameter (by {@link #getParam()}) or otherwise to manipulate (e.g. deactivate) the task.
	 */
	public String getMethod()
	{
		return method;
	}
	public void setMethod(String method)
	{
		if (method == null)
			throw new IllegalArgumentException("method must not be null!");

		this.method = method;
	}
	public String getParamObjectIDStr()
	{
		return paramObjectIDStr;
	}

	public Object getParam()
	{
		if (param == null) {
			if (paramObjectIDStr == null)
				return null;

			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
			if (pm == null)
				throw new IllegalStateException("param has not yet been loaded (e.g. when detaching) and this instance of Task is currently not persistent!");

			Object objectID = ObjectIDUtil.createObjectID(paramObjectIDStr);
			param = pm.getObjectById(objectID);
		}
		return param;
	}

	public void setParam(Object param)
	{
		if (param == null) {
			this.paramObjectIDStr = null;
			return;
		}

		if (!(param instanceof PersistenceCapable))
			throw new IllegalArgumentException("param is not persistence-capable!");

		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm != null && JDOHelper.getPersistenceManager(param) == null)
			param = pm.makePersistent(param);

		Object objectID = JDOHelper.getObjectId(param);
//		if (objectID == null)
//			throw new IllegalArgumentException("Param must either be null or a JDO object that has an objectID assigned: " + param);

		this.param = param;
		this.paramObjectIDStr = objectID == null ? null : objectID.toString();
	}

	public void setParamObjectIDStr(String paramObjectIDStr)
	{
		this.paramObjectIDStr = paramObjectIDStr;
		this.param = null;
	}

	public Date getLastExecDT()
	{
		return lastExecDT;
	}

	public void setLastExecDT(Date lastExecDT)
	{
		this.lastExecDT = lastExecDT;
//		this.lastExecDT = (Date) lastExecDT.clone();
//		// A Task cannot be called more than once a minute - see TimePatternSet.
//		// Hence, we don't save seconds and instead round down to the minute.
//		this.lastExecDT.setTime(
//				((long) Math.floor(this.lastExecDT.getTime() / 60000L)) * 60000L
//		);
	}

	/**
	 * @return Returns the timestamp of this task's next execution. It will be calculated by
	 *		{@link #calculateNextExecDT()} or on attachment.
	 */
	public Date getNextExecDT()
	{
		return nextExecDT;
	}

	/**
	 * Because {@link #calculateNextExecDT()} is quite expensive, it calculates the
	 * <code>nextExecDT</code> only until 2 times {@link #CALCULATE_NEXT_EXEC_DT_FUTURE_YEARS}
	 * years in the future. If <code>nextExecDT</code> cannot be found in this time period, it will
	 * set the field <code>nextCalculateNextExecDT</code> to a timestamp
	 * {@link #CALCULATE_NEXT_EXEC_DT_FUTURE_YEARS} in the future.
	 * This means with the current values for these constants, it causes re-calculation of
	 * the <code>nextExecDT</code> 2 years later, if it is not within 4 years in the future.
	 * <p>
	 * In other words: If this method returns sth. <b>not</b> <code>null</code>, the <code>nextExecDT</code>
	 * is at least two years ({@link #CALCULATE_NEXT_EXEC_DT_FUTURE_YEARS}) in the future.
	 * </p>
	 *
	 * @return the timestamp of the next time that {@link #calculateNextExecDT()} will be called.
	 */
	public Date getNextCalculateNextExecDT()
	{
		return nextCalculateNextExecDT;
	}

	public static final int CALCULATE_NEXT_EXEC_DT_FUTURE_YEARS = 2;

	/**
	 * This method calculates the next time this Task shall be triggered. It is automatically called,
	 * if you call {@link #setEnabled(boolean)}; but you <b>must call it manually</b>, if you modified the
	 * TimePatternSet!
	 * <p>
	 * If calculation fails because of a {@link JDODetachedFieldAccessException}, then it will be tried again
	 * once the object is stored to the datastore.
	 * </p>
	 * <p>
	 * For load-reduction reasons, this method calculates the <code>nextExecDT</code> only a certain time in
	 * the future. See {@link #getNextCalculateNextExecDT()}.
	 * </p>
	 */
	public void calculateNextExecDT()
	{
		if (executing)
			return;

		nextExecDT = null;
		if (!enabled)
			return;

		try {
			long next;
			if (lastExecDT == null)
				next = System.currentTimeMillis();
			else
				next = lastExecDT.getTime() + 60000;

			next = ((long) Math.floor(next / 60000L)) * 60000L; // a Task cannot be called more than once a minute - see TimePatternSet

			TimePatternSet tps = getTimePatternSet();
			long max = 1000L * 3600L * 24L * 365L * (tps.getFirstAndLastYear().getTo() + 1L - 1970L);
			long calcFutureYearsMSec = 1000L * 3600L * 24L * 365L * CALCULATE_NEXT_EXEC_DT_FUTURE_YEARS;
			long postponeCalcMax =
				System.currentTimeMillis()
				+ calcFutureYearsMSec * 2;
			while (next < max) {
				if (tps.matches(next)) {
					nextCalculateNextExecDT = null;
					nextExecDT = new Date(next);
					return;
				}
				next += 60000;
//				if (next % (1000L * 3600L * 24L * 365L) == 0)
//					LOGGER.info("No nextDT found yet: " + new Date(next));

				if (next > postponeCalcMax) {
					nextCalculateNextExecDT = new Date(next - calcFutureYearsMSec);
					nextExecDT = null;
					return;
				}
			}
			nextCalculateNextExecDT = null;
			nextExecDT = null;
		} catch (JDODetachedFieldAccessException x) {
			logger.warn("Calculating nextExecDT failed! It will be calculated when the object is stored to the database.", x);
		}
	}

	public boolean isEnabled()
	{
		return enabled;
	}
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
		calculateNextExecDT();
	}

	/**
	 * @return <code>true</code>, if this <code>Task</code> is right now in process and <code>false</code>, if it is not currently
	 *		executed.
	 */
	public boolean isExecuting()
	{
		return executing;
	}
	public void setExecuting(boolean executing)
	{
		this.executing = executing;
		calculateNextExecDT();
	}

	/**
	 * When a Task is enqueued for execution via <code>TimerAsyncInvoke</code>, a new activeExecID
	 * is generated. This will be kept until the task is triggered again. In case, an old execution
	 * is still in the queue, it will succeed as noop (and thus, be removed from the queue).
	 * <p>
	 * This basically means, a Task will be retried (in case of an error) until it is re-enqueued by a newer
	 * timer event (or until the message is seen as "undeliverable" and dumped by the Queue).
	 * </p>
	 *
	 * @return Returns the activeExecID as previously set.
	 */
	public String getActiveExecID()
	{
		return activeExecID;
	}
	public void setActiveExecID(String activeExecID)
	{
		this.activeExecID = activeExecID;
	}

	public boolean isLastExecFailed()
	{
		return lastExecFailed;
	}

	public void lastExecSuccessful(long durationMSec)
	{
		this.lastExecDT = new Date();
		this.lastExecFailed = false;
		this.lastExecMessage = null;
		this.lastExecStackTrace = null;
		this.lastExecDurationMSec = durationMSec;
		this.activeExecID = null;
		setExecuting(false);
	}

	public void lastExecFailed(Throwable error)
	{
		this.lastExecDT = new Date();
		this.lastExecFailed = true;
		this.lastExecDurationMSec = 0;

		if (error == null) {
			this.lastExecMessage = "Why the hell is error == null?!?!?";
			this.lastExecStackTrace = "Why the hell is error == null?!?!?";
		}
		else {
			Throwable t = error;
			String msg;
			do {
				msg = t.getLocalizedMessage();
				if (msg == null || "".equals(msg))
					msg = t.getMessage();

				if (msg != null && !"".equals(msg))
					break;

				t = ExceptionUtils.getCause(t);
			} while (t != null);
			this.lastExecMessage = msg;
			this.lastExecStackTrace = Util.getStackTraceAsString(error);
		}
		setExecuting(false);
	}

	public void lastExecFailed(String lastExecMessage, String lastExecStackTrace)
	{
		this.lastExecDT = new Date();
		this.lastExecFailed = true;
		this.lastExecDurationMSec = 0;
		this.lastExecMessage = lastExecMessage;
		this.lastExecStackTrace = lastExecStackTrace;
		setExecuting(false);
	}

	public long getLastExecDurationMSec()
	{
		return lastExecDurationMSec;
	}

	public String getLastExecMessage()
	{
		return lastExecMessage;
	}

	public String getLastExecStackTrace()
	{
		return lastExecStackTrace;
	}

	public void jdoPreDetach()
	{
		// nothing to do here
	}

	public void jdoPostDetach(Object _attached)
	{
		Task attached = (Task) _attached;
		Task detached = this;

		PersistenceManager pm = attached.getPersistenceManager();
		if (pm.getFetchPlan().getGroups().contains(FETCH_GROUP_PARAM))
			detached.param = pm.detachCopy(attached.getParam());
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("Could not obtain PersistenceManager!");

		return pm;
	}

	public void jdoPreAttach()
	{
		// nothing to do here
	}

	public void jdoPostAttach(Object detached)
	{
		Object param;
		try {
			param = ((Task)detached).param;
		} catch (JDODetachedFieldAccessException x) {
			param = null;
		}
		if (param != null) {
			setParam(
					getPersistenceManager().makePersistent(param));
		}
	}

	public void jdoPreStore()
	{
		// jdoPreStore is called in both cases: a local change or attach
		if (param != null) {
			setParam(
					getPersistenceManager().makePersistent(param));
		}
		if (enabled && nextExecDT == null)
			calculateNextExecDT();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof Task))
			return false;

		Task o = (Task)obj;

		return
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.taskTypeID, o.taskTypeID) &&
				Util.equals(this.taskID, o.taskID);
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) ^ Util.hashCode(taskTypeID) ^ Util.hashCode(taskID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + taskTypeID + ',' + taskID + ']';
	}
}
