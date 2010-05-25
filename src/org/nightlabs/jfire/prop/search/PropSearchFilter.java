/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.prop.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jdo.search.ISearchFilterItem;
import org.nightlabs.jdo.search.SearchFilter;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * Builds and executes a {@link javax.jdo.Query} for searching {@link org.nightlabs.jfire.prop.PropertySet}s.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class PropSearchFilter
	extends SearchFilter
{
	/**
	 * The prefix of the data field variables in the JDO query.
	 */
	public static final String QUERY_DATAFIELD_VARNAME_PREFIX = "dataField";

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1L;

	public static class ResultField implements Serializable {
		/**
		 * The serial version of this class.
		 */
		private static final long serialVersionUID = 1L;

		private Class<? extends StructField<? extends DataField>> fieldClass;
		private StructFieldID fieldID;
		private String propMember;
		private boolean dataFieldType;

		public ResultField(Class<? extends StructField<? extends DataField>> fieldClass, StructFieldID fieldID) {
			setFieldClass(fieldClass);
			setFieldID(fieldID);
			setPropMember("");
			setDataFieldType(true);
		}

		public ResultField(String propMember) {
			setPropMember("");
			setFieldClass(null);
			setFieldID(null);
			setDataFieldType(false);
		}

		public Class<? extends StructField<? extends DataField>> getFieldClass() {
			return fieldClass;
		}
		public void setFieldClass(Class<? extends StructField<? extends DataField>> fieldClass) {
			this.fieldClass = fieldClass;
		}
		public StructFieldID getFieldID() {
			return fieldID;
		}
		public void setFieldID(StructFieldID fieldID) {
			this.fieldID = fieldID;
		}
		public boolean isDataFieldType() {
			return dataFieldType;
		}
		public void setDataFieldType(boolean dataFieldType) {
			this.dataFieldType = dataFieldType;
		}
		public String getPropMember() {
			return propMember;
		}
		public void setPropMember(String propMember) {
			this.propMember = propMember;
		}
	}

	public PropSearchFilter() {
		super(CONJUNCTION_DEFAULT);
	}

	public PropSearchFilter(int _conjunction) {
		super(_conjunction);
	}

	/**
	 * value: ResultField
	 */
	private List<ResultField> resultFields = new ArrayList<ResultField>();

	public void addResultStructFieldID(Class<? extends StructField<? extends DataField>> resultFieldClass, StructFieldID resultFieldID) {
		resultFields.add(new ResultField(resultFieldClass, resultFieldID));
	}
	public void addResultField(ResultField resultField) {
		resultFields.add(resultField);
	}
	public void clearResultFields() {
		resultFields.clear();
	}

	protected boolean hasDataFieldTypeResultField() {
		for (Iterator<ResultField> iter = resultFields.iterator(); iter.hasNext();) {
			ResultField field = iter.next();
			if (field.isDataFieldType())
				return true;
		}
		return false;
	}

	protected boolean hasResultFields() {
		return resultFields.size() > 0;
	}

	protected void declarePropVariable(StringBuffer vars) {
		vars.append(PropertySet.class.getName()+" queryProp");
	}

	/**
	 * Should be used for setPersonVariableCondition
	 */
	public static final String PROPERTY_VARNAME = "queryProp";

	public void setPropVariableCondition(StringBuffer filter){
		filter.append(PROPERTY_VARNAME+" == this");
	}

	public void addPropFilterItems(Set<Class<?>> imports, StringBuffer vars, StringBuffer filter, StringBuffer params, Map<String, Object> paramMap) {
//		boolean firstItemProcessed = false;
		Map<Class<?>, List<AbstractStructFieldSearchFilterItem>> filterItemsPerClass = new HashMap<Class<?>, List<AbstractStructFieldSearchFilterItem>>();

		// If there are no FilterItems registered, the search is unconstrained and we add a true expression and return
		if (getFilterItems().isEmpty()) {
			filter.append("1 == 1");
			return;
		}

		// Otherwise, we build the query
		for (Iterator<ISearchFilterItem> it = getFilterItems().iterator(); it.hasNext(); ) {
			ISearchFilterItem item = it.next();
			if (!AbstractStructFieldSearchFilterItem.class.isAssignableFrom(item.getClass()))
				continue;

			AbstractStructFieldSearchFilterItem searchFilterItem = (AbstractStructFieldSearchFilterItem) item;

			//		Change in the API: Now the SearchFilterItemEditor indicates whether it has a constraint
			//		and SearchFilterItems are only added for those with constraint. Therefore this check is skipped. Tobias.
			//			if (item.isConstraint()) {
			//				if (!firstItemProcessed) { firstItemProcessed = true;}
			//			}

			List<AbstractStructFieldSearchFilterItem> itemList = filterItemsPerClass.get(searchFilterItem.getDataFieldClass());
			if (itemList == null) {
				itemList = new ArrayList<AbstractStructFieldSearchFilterItem>();
				filterItemsPerClass.put(searchFilterItem.getDataFieldClass(),itemList);
			}
			itemList.add(searchFilterItem);
		}

//		if (!firstItemProcessed) {
//			// no PersonSearchFilterItem found
//			// or none was constraining
//			// find nothing
////			filter.append("1 == 0");
//
//			// Empty query should now return ALL records.
//			// 2009-11-12, Kai
//			filter.append("1 == 1");
//			return;
//		}
//
//		firstItemProcessed = false;
//
//		vars.append("; ");
//		vars.append(PersonDataBlockGroup.class.getName()+" personDataBlockGroup");
//		vars.append("; ");
//		vars.append(PersonDataBlock.class.getName()+" personDataBlock");

//		filter.append("(");
//		filter.append(PERSON_VARNAME+".personDataBlockGroups.containsValue(personDataBlockGroup)");
//		filter.append(" && ");
//		filter.append("personDataBlockGroup.personDataBlocks.containsValue(personDataBlock)");

//		filter.append((PERSON_VARNAME+".personDataFields.contains(personDataField)");
//		filter.append(" && ");
//		filter.append("(");

		if (params.length() > 0)
			params.append(", ");

//		int n = 0;
//		int fieldClassNo = 0;
		int itemIndex = 0;
		// itearte a second time to fill the query
		for (Iterator<ISearchFilterItem> iter = getFilterItems().iterator(); iter.hasNext(); ) {
			if (params.length() > 0)
				params.append(", ");
			IStructFieldSearchFilterItem item = (IStructFieldSearchFilterItem) iter.next();

			Class<?> itemDataFieldClass = item.getDataFieldClass();
			if (itemDataFieldClass == null)
				throw new IllegalArgumentException("Some SearchFilterItem returned null value in getDataFieldClass().");
			if (!DataField.class.isAssignableFrom(itemDataFieldClass))
				throw new IllegalArgumentException("Some SearchFilterItem did not return an inheritor of AbstractPersonDataField in getDataFieldClass() but instead "+itemDataFieldClass.getName());
			vars.append("; ");
			vars.append(itemDataFieldClass.getName()+" "+QUERY_DATAFIELD_VARNAME_PREFIX+itemIndex);

			filter.append("(");
			filter.append(PROPERTY_VARNAME+".dataFields.contains("+QUERY_DATAFIELD_VARNAME_PREFIX+itemIndex+")");
			filter.append(" && (");

			filter.append("(");

			filter.append("(");
			int sub_n = 0;
			for (Iterator<StructFieldID> iterator = item.getStructFieldIDs().iterator(); iterator.hasNext();) {
				String structIdentifier = itemIndex+"_"+sub_n;

				filter.append("(");
				StructFieldID structFieldID = iterator.next();

				if (sub_n > 0)
					params.append(", ");
				// begin primary key PersonStructField
				params.append(String.class.getName()+" structBlockOrganisationID"+structIdentifier);
				paramMap.put("structBlockOrganisationID"+structIdentifier, structFieldID.structBlockOrganisationID);

				params.append(", ");
				params.append(String.class.getName()+" structBlockID"+structIdentifier);
				paramMap.put("structBlockID"+structIdentifier, structFieldID.structBlockID);

				params.append(", ");
				params.append(String.class.getName()+" structFieldOrganisationID"+structIdentifier);
				paramMap.put("structFieldOrganisationID"+structIdentifier, structFieldID.structFieldOrganisationID);

				params.append(", ");
				params.append(String.class.getName()+" structFieldID"+structIdentifier);
				paramMap.put("structFieldID"+structIdentifier, structFieldID.structFieldID);

				// begin primary key of PersonStructField
				filter.append(QUERY_DATAFIELD_VARNAME_PREFIX+itemIndex+".structBlockOrganisationID == structBlockOrganisationID"+structIdentifier);
				filter.append(" && ");
				filter.append(QUERY_DATAFIELD_VARNAME_PREFIX+itemIndex+".structBlockID == structBlockID"+structIdentifier);
				filter.append(" && ");
				filter.append(QUERY_DATAFIELD_VARNAME_PREFIX+itemIndex+".structFieldOrganisationID == structFieldOrganisationID"+structIdentifier);
				filter.append(" && ");
				filter.append(QUERY_DATAFIELD_VARNAME_PREFIX+itemIndex+".structFieldID == structFieldID"+structIdentifier);
				filter.append(")");
				if (iterator.hasNext())
					filter.append(" || ");
				sub_n++;
			} // for (Iterator iterator = item.getPersonStructFieldIDs().iterator(); iterator.hasNext();) {
			filter.append(")");

			filter.append(" && ");

			item.appendSubQuery(itemIndex, imports, vars, filter, params, paramMap);

			filter.append(")");

			filter.append(")");
			filter.append(")");

			if (iter.hasNext()) {
				switch (getConjunction())
				{
				case SearchFilter.CONJUNCTION_AND:
					filter.append(" && ");
					break;
				case SearchFilter.CONJUNCTION_OR:
					filter.append(" || ");
					break;
				default:
					throw new IllegalStateException("conjunction invalid!");
				}
			}
			itemIndex++;
//			n++;
//			fieldClassNo++;
		}  // for (Iterator iter = itemList.iterator(); iter.hasNext();)

	}

	@Override
	protected void prepareQuery(
			Set<Class<?>> imports, StringBuffer vars, StringBuffer filter, StringBuffer params,
			Map<String, Object> paramMap, StringBuffer result)
	{
		declarePropVariable(vars);
		setPropVariableCondition(filter);

		// result stuff
		filter.append(" && ");
		if (hasDataFieldTypeResultField()) {
			filter.append("( ( ");
			filter.append(" (1 == 2) && (");
			if (vars.length() > 0)
				vars.append("; ");
		}
		int n = 0;
		for (Iterator<ResultField> iter = resultFields.iterator(); iter.hasNext();) {
			ResultField resultField = iter.next();
			if (hasDataFieldTypeResultField()) {
				imports.add(resultField.getFieldClass());
				vars.append(resultField.getFieldClass().getName()+" resultField"+n);
				if (iter.hasNext())
					vars.append("; ");

				if (params.length() > 0)
					params.append(", ");
				params.append(String.class.getName()+" resultStructFieldKey"+n);
				paramMap.put(
						"resultStructFieldKey"+n,
						StructField.getPrimaryKey(
								resultField.getFieldID().structBlockOrganisationID,
								resultField.getFieldID().structBlockID,
								resultField.getFieldID().structFieldOrganisationID,
								resultField.getFieldID().structFieldID
						));
				filter.append("( "+PROPERTY_VARNAME+".dataFields.contains(resultField"+n+") && resultField"+n+".structFieldKey == resultStructFieldKey"+n+" )");
				if (iter.hasNext())
					filter.append(" && ");
				if (result.length() > 0)
					result.append(", ");
				result.append(" resultField"+n);
			}
			else {
				if (result.length() > 0)
					result.append(", ");
				result.append(" "+PROPERTY_VARNAME+"."+resultField.getPropMember());
			}
			n++;
		}
		if (hasDataFieldTypeResultField()) {
			filter.append(")");
			filter.append(") || ");
		}

		// real person criteria
		filter.append("(");
		addPropFilterItems(imports,vars,filter,params,paramMap);
		filter.append(")");
		if (resultFields.size() > 0)
			filter.append(")");

		result.append("DISTINCT this");
	}

	@Override
	protected Class<?> initCandidateClass()
	{
		return PropertySet.class;
	}

}
