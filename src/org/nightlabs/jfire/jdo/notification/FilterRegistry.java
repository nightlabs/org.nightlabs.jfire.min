package org.nightlabs.jfire.jdo.notification;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.jdo.cache.CacheManagerFactory;

public class FilterRegistry
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(FilterRegistry.class);

	private CacheManagerFactory cacheManagerFactory;

	private Map<AbsoluteFilterID, IJDOLifecycleListenerFilter> filterID2Filter = new HashMap<AbsoluteFilterID, IJDOLifecycleListenerFilter>();

	/**
	 * key: JDOLifecycleState lifecycleStage<br/>
	 * value: Map {<br/>
	 *		key: Boolean includeSubclasses<br/>
	 *		value: Map {<br/>
	 *			key: Class candidateClass<br/>
	 *			value: Map {<br/>
	 *				key: AbsoluteFilterID filterID<br/>
	 *				value: JDOLifecycleListenerFilter filter<br/>
	 *		}<br/>
	 * }<br/>
	 */
	private Map<JDOLifecycleState, Map<Boolean, Map<Class, Map<AbsoluteFilterID, IJDOLifecycleListenerFilter>>>> groupedFilters = new HashMap<JDOLifecycleState, Map<Boolean, Map<Class, Map<AbsoluteFilterID, IJDOLifecycleListenerFilter>>>>();

	public FilterRegistry(CacheManagerFactory cacheManagerFactory)
	{
		if (cacheManagerFactory == null)
			throw new IllegalArgumentException("cacheManagerFactory must not be null");

		this.cacheManagerFactory = cacheManagerFactory;
	}

	public CacheManagerFactory getCacheManagerFactory()
	{
		return cacheManagerFactory;
	}

	public void addFilter(IJDOLifecycleListenerFilter filter)
	{
		assertOpen();

		if (filter == null)
			throw new IllegalArgumentException("filter is null");

		JDOLifecycleState[] lifecycleStages = filter.getLifecycleStates();
		if (lifecycleStages == null)
			throw new IllegalArgumentException("filter.getLifecycleStages() is null");

		if (lifecycleStages.length < 1)
			throw new IllegalArgumentException("filter.getLifecycleStages() is empty");

		Class[] candidateClasses = filter.getCandidateClasses();
		if (candidateClasses == null)
			throw new IllegalArgumentException("filter.getCandidateClasses() is null");

		if (candidateClasses.length < 1)
			throw new IllegalArgumentException("filter.getCandidateClasses() is empty");

		Boolean includeSubclasses = filter.includeSubclasses();

		synchronized (filterID2Filter) {
			filterID2Filter.put(filter.getFilterID(), filter);
		}

		synchronized (groupedFilters) {
			for (JDOLifecycleState lifecycleStage : lifecycleStages) {
				Map<Boolean, Map<Class, Map<AbsoluteFilterID, IJDOLifecycleListenerFilter>>> m1;

				m1 = groupedFilters.get(lifecycleStage);
				if (m1 == null) {
					m1 = new HashMap<Boolean, Map<Class,Map<AbsoluteFilterID, IJDOLifecycleListenerFilter>>>();
					groupedFilters.put(lifecycleStage, m1);
				}


				Map<Class, Map<AbsoluteFilterID, IJDOLifecycleListenerFilter>> m2;
				m2 = m1.get(includeSubclasses);
				if (m2 == null) {
					m2 = new HashMap<Class, Map<AbsoluteFilterID, IJDOLifecycleListenerFilter>>();
					m1.put(includeSubclasses, m2);
				}

				for (Class candidateClass : candidateClasses) {
					Map<AbsoluteFilterID, IJDOLifecycleListenerFilter> m3;

					m3 = m2.get(candidateClass);
					if (m3 == null) {
						m3 = new HashMap<AbsoluteFilterID, IJDOLifecycleListenerFilter>();
						m2.put(candidateClass, m3);
					}

					m3.put(filter.getFilterID(), filter);
				}
			} // for (JDOLifecycleState lifecycleStage : filter.getLifecycleStages()) {
		} // synchronized (groupedFilters) {

		if (logger.isDebugEnabled())
			debugLogFilters();
	}

// TODO maybe we should manage interfaces, as well. Marco.
	private void findMatchingFilters(
			Map<AbsoluteFilterID, IJDOLifecycleListenerFilter> outFilters,
			Map<Boolean, Map<Class, Map<AbsoluteFilterID, IJDOLifecycleListenerFilter>>> inFilters,
			boolean includeSubclasses,
			Class jdoObjectClass)
	{
		Map<Class, Map<AbsoluteFilterID, IJDOLifecycleListenerFilter>> class2Filters;
		class2Filters = inFilters.get(includeSubclasses);

		if (class2Filters == null)
			return;

		Class candidateClass = jdoObjectClass;
		while (candidateClass != null) {
			Map<AbsoluteFilterID, IJDOLifecycleListenerFilter> filters = class2Filters.get(candidateClass);
			if (filters != null)
				outFilters.putAll(filters);

			if (!includeSubclasses)
				return;

			candidateClass = candidateClass.getSuperclass();
		}
	}

	/**
	 * @param lifecycleStage What happened?
	 * @param jdoObjectClass What's the type of the jdo object. It will be matched against the groupedFilters' candidate classes.
	 * @return Returns all matching groupedFilters - never <code>null</code>.
	 */
	public Collection<IJDOLifecycleListenerFilter> getMatchingFilters(
			JDOLifecycleState lifecycleStage, Class jdoObjectClass)
	{
		assertOpen();

		Map<AbsoluteFilterID, IJDOLifecycleListenerFilter> res = new HashMap<AbsoluteFilterID, IJDOLifecycleListenerFilter>();
		Map<Boolean, Map<Class, Map<AbsoluteFilterID, IJDOLifecycleListenerFilter>>> m1;
		synchronized (groupedFilters) {
			m1 = groupedFilters.get(lifecycleStage);
			if (m1 == null)
				return res.values();

			findMatchingFilters(res, m1, true, jdoObjectClass);
			findMatchingFilters(res, m1, false, jdoObjectClass);
		} // synchronized (groupedFilters) {

		return res.values();
	}

	public void removeFilter(AbsoluteFilterID filterID)
	{
		assertOpen();

		IJDOLifecycleListenerFilter filter;
		synchronized (filterID2Filter) {
			filter = filterID2Filter.remove(filterID);
		}

		if (filter != null)
			_removeFilter(filter);
	}

	public void removeFilter(IJDOLifecycleListenerFilter filter)
	{
		assertOpen();

		synchronized (filterID2Filter) {
			filterID2Filter.remove(filter.getFilterID());
		}

		if (filter != null)
			_removeFilter(filter);
	}

	protected void _removeFilter(IJDOLifecycleListenerFilter filter)
	{
		if (filter == null)
			throw new IllegalArgumentException("filter is null");

		JDOLifecycleState[] lifecycleStages = filter.getLifecycleStates();
		if (lifecycleStages == null)
			throw new IllegalArgumentException("filter.getLifecycleStages() is null");

		if (lifecycleStages.length < 1)
			throw new IllegalArgumentException("filter.getLifecycleStages() is empty");

		Class[] candidateClasses = filter.getCandidateClasses();
		if (candidateClasses == null)
			throw new IllegalArgumentException("filter.getCandidateClasses() is null");

		if (candidateClasses.length < 1)
			throw new IllegalArgumentException("filter.getCandidateClasses() is empty");

		Boolean includeSubclasses = filter.includeSubclasses();

		synchronized (groupedFilters) {
			iterateLifecycleStage: for (JDOLifecycleState lifecycleStage : lifecycleStages) {
				Map<Boolean, Map<Class, Map<AbsoluteFilterID, IJDOLifecycleListenerFilter>>> m1;

				m1 = groupedFilters.get(lifecycleStage);
				if (m1 == null)
					continue iterateLifecycleStage;

				Map<Class, Map<AbsoluteFilterID, IJDOLifecycleListenerFilter>> m2;
				m2 = m1.get(includeSubclasses);
				if (m2 == null)
					continue iterateLifecycleStage;

				iterateCandidateClass: for (Class candidateClass : candidateClasses) {
					Map<AbsoluteFilterID, IJDOLifecycleListenerFilter> m3;

					m3 = m2.get(candidateClass);
					if (m3 == null)
						continue iterateCandidateClass;

					m3.remove(filter.getFilterID());

					if (m3.isEmpty())
						m2.remove(candidateClass);
				} // for (Class candidateClass : candidateClasses) {

				if (m2.isEmpty())
					m1.remove(includeSubclasses);

				if (m1.isEmpty())
					groupedFilters.remove(lifecycleStage);
			} // for (JDOLifecycleState lifecycleStage : filter.getLifecycleStages()) {
		} // synchronized (groupedFilters) {

		if (logger.isDebugEnabled())
			debugLogFilters();
	}

	private void debugLogFilters()
	{
		logger.debug("");
		logger.debug("debugLogFilters(): >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		synchronized (groupedFilters) {
			logger.debug("debugLogFilters(): groupedFilters.size=" + groupedFilters.size());

			for (Map.Entry<JDOLifecycleState, Map<Boolean, Map<Class, Map<AbsoluteFilterID, IJDOLifecycleListenerFilter>>>> me1 : groupedFilters.entrySet()) {
				logger.debug("debugLogFilters(): lifecycleStage="+me1.getKey());
				for (Map.Entry<Boolean, Map<Class, Map<AbsoluteFilterID, IJDOLifecycleListenerFilter>>> me2 : me1.getValue().entrySet()) {
					logger.debug("debugLogFilters():   includeSubclasses="+me2.getKey());
					for (Map.Entry<Class, Map<AbsoluteFilterID, IJDOLifecycleListenerFilter>> me3 : me2.getValue().entrySet()) {
						logger.debug("debugLogFilters():     candidateClass="+me3.getKey().getName());
						for (Map.Entry<AbsoluteFilterID, IJDOLifecycleListenerFilter> me4 : me3.getValue().entrySet()) {
							logger.debug("debugLogFilters():       filterID="+me4.getKey().getSessionID() + "::" + me4.getKey().getFilterID());
						}
					}
				}
			}
		} // synchronized (groupedFilters) {
		logger.debug("debugLogFilters(): <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		logger.debug("");
	}

	protected void assertOpen()
	{
		if (groupedFilters == null)
			throw new IllegalStateException("This FilterRegistry is not open!");
	}

	public void close()
	{
		groupedFilters = null;
	}
}
