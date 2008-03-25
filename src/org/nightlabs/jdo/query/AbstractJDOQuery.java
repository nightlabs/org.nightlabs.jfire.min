package org.nightlabs.jdo.query;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;


/**
 * This is a light-weight base class that you should subclass when you intend to have your
 * client define arbitrary parameters for a server-side jdo query.
 * <p>
 * For security reasons, it's urgently recommended not to allow a client to directly specify
 * JDOQL. Although, it's impossible to modify data using JDOQL, it's possible to retrieve data
 * that this user otherwise isn't allowed to see.
 * </p>
 * <p>
 * That's why it's best practice to implement the {@link #prepareQuery()} method with secure(!) JDOQL
 * and only to allow the client to specify some parameters. Because the client cannot make the server
 * load classes, it is restricted then to the predefined JDOQL (in your implementation of this class).
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @param <R> The type of the query's result.
 */
public abstract class AbstractJDOQuery<R>
	extends AbstractSearchQuery<R>
{
	private static final long serialVersionUID = 1L;
	private transient PersistenceManager persistenceManager = null;

	/**
	 * Use this method in your implementation of {@link #prepareQuery()} to obtain
	 * a persistence manager for creation of your query.
	 * <p>
	 * The {@link PersistenceManager} is stored in a <i>transient</i> field.
	 * </p>
	 *
	 * @return Returns the persistence manager which has previously been assigned by
	 *		{@link #setPersistenceManager(PersistenceManager)}.
	 */
	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	/**
	 * When using an instance of a JDOQuery's subclass - for example in your EJB
	 * method, you must assign a <code>PersistenceManager</code> before calling
	 * {@link #getResult()}.
	 * <p>
	 * The {@link PersistenceManager} is stored in a <i>transient</i> field.
	 * </p>
	 *
	 * @param pm The PersistenceManager instance that shall be used for accessing the
	 *		datastore.
	 */
	public void setPersistenceManager(PersistenceManager pm)
	{
		this.persistenceManager = pm;
	}

	/**
	 * This method checks, whether a {@link PersistenceManager} has been assigned before.
	 *
	 * @throws IllegalStateException Thrown if {@link #setPersistenceManager(PersistenceManager)} has not been called yet.
	 */
	protected void assertPersistenceManager()
	throws IllegalStateException
	{
		if (persistenceManager == null)
			throw new IllegalStateException("No PersistenceManager assigned! The method setPersistenceManager(...) must be called before!");
	}

	private Collection<R> result = null;

	/**
	 * This method delegates - if necessary - to {@link #executeQuery()} and
	 * returns the result. A subsequent call to this method returns the previously returned
	 * instance and does not call {@link #executeQuery()} again.
	 * <p>
	 * You should not override this method. It's probably always better to override {@link #executeQuery()} if
	 * implementing {@link #prepareQuery()} isn't sufficient.
	 * </p>
	 *
	 * @return Returns the result of the query. Never returns <code>null</code>.
	 */
	@Override
	public Collection<R> getResult()
	{
		if (result == null) {
			assertPersistenceManager();

			result = executeQuery();
			if (result == null)
				throw new IllegalStateException("The method executeQuery() of class "+this.getClass().getName()+" is incorrectly implemented and returned null!");
		}

		return result;
	}

	/**
	 * This method first calls {@link #prepareQuery()} in order to obtain a
	 * {@link Query} that's ready to be executed. The candidates specified by
	 * {@link #setCandidates(Collection)} are passed to the <code>Query</code>.
	 * Then, it collects all fields
	 * of your subclass and puts their names together with their values into
	 * the parameter map which is passed to {@link Query#executeWithMap(Map)}.
	 * <p>
	 * If you need to post-process your query result (e.g. perform security related stuff like
	 * removing passwords), you should extend this method: First call the super implementation, then
	 * post-process its result before returning it.
	 * </p>
	 * <p>
	 * This method is called by {@link #getResult()}, if necessary (i.e. only once - the result is cached).
	 * </p>
	 * 
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	protected Collection<R> executeQuery()
	{
		Map<String, Object> params = null;
		Query q = null;
		try {
			q = prepareQuery();
			if (q == null)
				throw new IllegalStateException("Your implementation of prepareQuery() in class " +
					this.getClass().getName() + " returned null!");

			q.setCandidates(getCandidates());
			q.setRange(getFromInclude(), getToExclude());
			
			params = new HashMap<String, Object>();
			Class<?> clazz = getClass();
			while (clazz != AbstractJDOQuery.class) {
				Field[] fields = clazz.getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					Field field = fields[i];
					if ((field.getModifiers() & Modifier.STATIC) != 0)
						continue;

//					if ((field.getModifiers() & Modifier.TRANSIENT) != 0)
//					continue;

					if (!field.isAccessible())
						field.setAccessible(true);

					String fieldName = field.getName();
					if (params.containsKey(fieldName))
						throw new IllegalStateException("The class " + this.getClass() + " has a duplicate field declaration (probably private) for the field " + fieldName + " in its inheritance structure! Every field name must be unique in the whole inheritance structure (including private fields)!");

					Object value;
					try {
						value = field.get(this);
					} catch (IllegalAccessException e) {
						// this exception should never happen when accessing _this_ only
						throw new RuntimeException(e);
					}
					params.put(fieldName, value);
				}
				clazz = clazz.getSuperclass();
			}

			Object result = q.executeWithMap(params);
			if (result instanceof Collection)
			{
				final Collection<?> jdoResult = (Collection<?>) result;
				for (Object element : jdoResult)
				{
					// check if the elements in the returned collection fit the declared return type.
					getResultType().cast(element);
				}
				
				// important to not wrap this, because wrapping in a new ArrayList or similar might be very
				// expensive and prevents JPOX to optimise candidates-handling
				return (Collection<R>)jdoResult;
			}

			return Collections.singletonList((R) getResultType().cast(result));
		} catch (Throwable t) {
			Logger.getLogger(AbstractJDOQuery.class).error("Executing JDOQuery defined by an instance of " + this.getClass().getName() + " failed!\nQuery: " + q + "\nParams: " + params, t);
			if (t instanceof RuntimeException)
				throw (RuntimeException)t;

			if (t instanceof Error)
				throw (Error)t;

			throw new RuntimeException(t);
		}
	}
	
	/**
	 * Implement this method by constructing a JDO query which takes your subclass' fields
	 * as parameters into account. Each field is (using java reflection) put into a
	 * parameter map. Hence, you have access to all fields of your subclass via their simple
	 * names as implicit parameter (don't forget the colon ":" prefix).
	 * <p>
	 * Example: You declare a field in your subclass:<br/>
	 * <code>
	 * private Date minimumBirthday;
	 * </code>
	 * <br/>
	 * You can access this field in your query using the simple name <i>":minimumBirthday"</i>
	 * (implicit parameter).
	 * </p>
	 * <p>
	 * This method is called by {@link #getResult()}.
	 * </p>
	 *
	 * @return Returns a JDO query that's ready to be executed. Never returns <code>null</code>.
	 */
	protected abstract Query prepareQuery();

}
