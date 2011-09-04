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

package org.nightlabs.jfire.prop.datafield;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.i18n.I18nTextBuffer;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.MultiSelectionStructFieldValueID;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructField;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructFieldValue;
import org.nightlabs.jfire.prop.structfield.StructFieldValue;

/**
 * {@link DataField} that holds one member of type {@link MultiSelectionStructFieldValue} defining the users
 * choice from the list within {@link MultiSelectionStructField}.
 *
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de --> (Original SectionDataField code)
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_MultiSelectionDataField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name=PropertySet.FETCH_GROUP_FULL_DATA,
		members={@Persistent(name="structFieldValueIDs"), @Persistent(name="structFieldValueIDs_correct")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class MultiSelectionDataField
extends DataField
implements DetachCallback, II18nTextDataField
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 20100202L;

	/**
	 * @deprecated This field is persisted as BLOB! I introduced {@link #structFieldValueIDs_correct} in order to fix this bug.
	 */
	@Deprecated
	@Persistent
	protected Set<String> structFieldValueIDs;

	@Persistent(
			table="JFireBase_Prop_MultiSelectionDataField_structFieldValueIDs"
	)
	@Join
	protected Set<String> structFieldValueIDs_correct;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient I18nTextBuffer textBuffer = null;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	protected Set<MultiSelectionStructFieldValue> structFieldValues;

	/**
	 * For JDO only.
	 */
	protected MultiSelectionDataField()
	{
		super();
	}

	/**
	 * Create a new {@link MultiSelectionDataField} for the given {@link DataBlock}
	 * that represents the given {@link StructField}.
	 *
	 * @param dataBlock The {@link DataBlock} the new {@link MultiSelectionDataField} will be part of.
	 * @param structField The {@link StructField} the new {@link MultiSelectionDataField} represents in the data structure.
	 */
	public MultiSelectionDataField(DataBlock dataBlock, StructField<MultiSelectionDataField> structField)
	{
		super(dataBlock, structField);
		structFieldValueIDs_correct = new HashSet<String>();
	}

	/**
	 * Used for cloning.
	 */
	protected MultiSelectionDataField(String organisationID, long propertySetID, int dataBlockID, MultiSelectionDataField cloneField)
	{
		super(organisationID, propertySetID, dataBlockID, cloneField);
		structFieldValueIDs_correct = new HashSet<String>();
	}

	/**
	 * Set the {@link MultiSelectionStructFieldValue} selected for this {@link MultiSelectionDataField}.
	 * Note, that only the {@link StructFieldValue#getStructFieldValueID()}
	 * is stored in this data field.
	 *
	 * @param values The selection to set.
	 */
	public void setSelection(MultiSelectionStructFieldValue values)
	{
		setSelection(Collections.singleton(values));
	}

	/**
	 * Set the {@link MultiSelectionStructFieldValue} selected for this {@link MultiSelectionDataField}.
	 * Note, that only the {@link MultiSelectionStructFieldValue#getStructFieldValueID()}
	 * is stored in this data field.
	 *
	 * @param values The selection to set.
	 */
	public void setSelection(Collection<MultiSelectionStructFieldValue> values)
	{
		structFieldValueIDs = null;

		if (values == null || values.isEmpty()) {
			structFieldValueIDs_correct.clear();
			structFieldValues = null;
		}
		else {
			structFieldValueIDs_correct.clear();
			for (MultiSelectionStructFieldValue structFieldValue : values) {
				if (!structFieldValueIDs_correct.contains(structFieldValue.getStructFieldValueID()))
					structFieldValueIDs_correct.add(structFieldValue.getStructFieldValueID());
			}
			// XXX: add hashCode() and equals() to StructFieldValue
			structFieldValues = new HashSet<MultiSelectionStructFieldValue>(values);
		}
		resetTextBuffer();
	}

	/**
	 * Returns the {@link StructFieldValue#getStructFieldValueID()}s of the selected
	 * {@link MultiSelectionStructFieldValue}s selected for this {@link MultiSelectionDataField}.
	 * The {@link MultiSelectionStructFieldValue}s corresponding to the selection can be
	 * retrieved using {@link #getStructFieldValues()}.
	 *
	 * @return The structFieldValueID of the selected {@link StructFieldValue}.
	 */
	public Set<String> getStructFieldValueIDs()
	{
		if (structFieldValueIDs != null)
			return structFieldValueIDs;

		return Collections.unmodifiableSet(structFieldValueIDs_correct);
	}

	/**
	 * Returns the selected {@link MultiSelectionStructFieldValue}s of this MultiSelectionStructField.
	 * <p>
	 * If this is called on a persistent instance the value is resolved from datastore.
	 * </p>
	 * <p>
	 * If this is called on a detached instance the value must have been resolved during
	 * the detach process. This will be done if the fetch plan contains {@link PropertySet#FETCH_GROUP_FULL_DATA}.
	 * (see {@link #jdoPostDetach(Object)}).
	 * </p>
	 * @return The selected {@link MultiSelectionStructFieldValue} of this MultiSelectionStructField.
	 */
	public Collection<MultiSelectionStructFieldValue> getStructFieldValues()
	{
		if (JDOHelper.isPersistent(this))
			return resolveStructFieldValue(JDOHelper.getPersistenceManager(this), this);

		if (structFieldValues == null)
			structFieldValues = new HashSet<MultiSelectionStructFieldValue>();

		return Collections.unmodifiableSet(structFieldValues);
	}

//	public MultiSelectionStructField getSelectionStructField(IStruct structure) throws ClassCastException
//	{
//		IllegalStateException ill = null;
//		MultiSelectionStructField field = null;
//		try
//		{
//			field = (MultiSelectionStructField) structure.getStructField(getStructBlockOrganisationID(), getStructBlockID(),
//					getStructFieldOrganisationID(), getStructFieldID());
//		}
//		catch (Exception e)
//		{
//			ill = new IllegalStateException("Caught exception while accessing PropStructFactory");
//			ill.initCause(e);
//		}
//		if (ill != null)
//			throw ill;
//		return field;
//	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#getStructField()
	 */
	@Override
	public MultiSelectionStructField getStructField()
	{
		return (MultiSelectionStructField) super.getStructField();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		if (getStructField() == null)
			throw new IllegalStateException("isEmpty() was called on deflated data field.");

		return (getStructFieldValueIDs() == null || getStructFieldValueIDs().isEmpty()) &&
		       (getStructField().getDefaultValues() == null || getStructField().getDefaultValues().isEmpty());
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#cloneDataField(org.nightlabs.jfire.prop.PropertySet, int)
	 */
	@Override
	public DataField cloneDataField(PropertySet propertySet, int dataBlockID)
	{
		MultiSelectionDataField newField = new MultiSelectionDataField(getOrganisationID(), propertySet.getPropertySetID(), dataBlockID, this);
		newField.structFieldValueIDs_correct = new HashSet<String>(this.getStructFieldValueIDs());
		return newField;
	}

	private void resetTextBuffer()
	{
		textBuffer = null;
	}

	/**
	 * Create a i18n text containing a coma seperated list of text for
	 * every language available.
	 */
	private void updateTextBuffer()
	{
		textBuffer = new I18nTextBuffer();
		Collection<MultiSelectionStructFieldValue> structFieldValues = getStructFieldValues();
		if(structFieldValues == null) {
			// do nothing
		}
		else if(structFieldValues.size() == 1) {
			textBuffer.copyFrom(structFieldValues.iterator().next().getValueName());
		}
		else {
			// create a comma separated string containing all names for all languages
			Set<String> languages = new HashSet<String>();
			for(MultiSelectionStructFieldValue value : structFieldValues)
				languages.addAll(value.getValueName().getLanguageIDs());
			for (String language : languages) {
				StringBuilder textInLanguage = new StringBuilder();
				for(MultiSelectionStructFieldValue value : structFieldValues) {
					if(textInLanguage.length() > 0)
						textInLanguage.append(", ");
					String valueNameInLanguage = value.getValueName().getText(language);
					textInLanguage.append(valueNameInLanguage);
				}
				textBuffer.setText(language, textInLanguage.toString());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.datafield.II18nTextDataField#getI18nText()
	 */
	@Override
	public I18nTextBuffer getI18nText()
	{
		if (textBuffer == null)
			updateTextBuffer();
		return textBuffer;
	}

	/**
	 * In the post detach callback the {@link MultiSelectionDataField} will check whether
	 * {@link PropertySet#FETCH_GROUP_FULL_DATA} is in the fetchplan and resolve the
	 * {@link MultiSelectionStructFieldValue}s that are selected for this field. It will then detach
	 * the values and set the non-persistent member structFieldValues (see {@link #getStructFieldValues()}).
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void jdoPostDetach(Object detached)
	{
		MultiSelectionDataField attached = (MultiSelectionDataField) detached;
		PersistenceManager pm = JDOHelper.getPersistenceManager(attached);
		if (pm.getFetchPlan().getGroups().contains(PropertySet.FETCH_GROUP_FULL_DATA)) {
			Set<MultiSelectionStructFieldValueID> structFieldValueIDs = getStructFieldValueIDs(attached);
			if(structFieldValueIDs != null && !structFieldValueIDs.isEmpty()) {
				// this is a set of String, but we don't need the type information here - so we need no cast
				Set<?> oldFetchGroups = pm.getFetchPlan().getGroups();
				int oldFetchDepth = pm.getFetchPlan().getMaxFetchDepth();
				try {
					this.structFieldValues = new HashSet<MultiSelectionStructFieldValue>(pm.detachCopyAll(pm.getObjectsById(structFieldValueIDs)));
				} finally {
					pm.getFetchPlan().setGroups(oldFetchGroups);
					pm.getFetchPlan().setMaxFetchDepth(oldFetchDepth);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.jdo.listener.DetachCallback#jdoPreDetach()
	 */
	@Override
	public void jdoPreDetach() {
	}

	/**
	 * Resolves the {@link MultiSelectionStructFieldValue}s that are selected by the given dataField.
	 *
	 * @param pm The PersistenceManager to use.
	 * @param dataField The {@link MultiSelectionDataField} to resolve the selection for.
	 * @return The {@link MultiSelectionStructFieldValue} that is selected by the given dataField.
	 */
	@SuppressWarnings("unchecked")
	public static Collection<MultiSelectionStructFieldValue> resolveStructFieldValue(PersistenceManager pm, MultiSelectionDataField dataField)
	{
		Set<MultiSelectionStructFieldValueID> structFieldValueIDs = getStructFieldValueIDs(dataField);
		if(structFieldValueIDs == null || structFieldValueIDs.isEmpty())
			return null;
		return pm.getObjectsById(structFieldValueIDs);
	}

	private static Set<MultiSelectionStructFieldValueID> getStructFieldValueIDs(MultiSelectionDataField dataField)
	{
		Set<String> structFieldValueIDs = dataField.getStructFieldValueIDs();
		if (structFieldValueIDs == null || structFieldValueIDs.isEmpty())
			return Collections.emptySet();
		Set<MultiSelectionStructFieldValueID> result = new HashSet<MultiSelectionStructFieldValueID>(structFieldValueIDs.size());
		for(String structFieldValueId : structFieldValueIDs) {
			result.add(MultiSelectionStructFieldValueID.create(
					dataField.getStructBlockOrganisationID(), dataField.getStructBlockID(),
					dataField.getStructFieldOrganisationID(), dataField.getStructFieldID(),
					structFieldValueId));
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This is equal to calling {@link #getStructFieldValues()}.
	 * </p>
	 */
	@Override
	public Object getData()
	{
		return getStructFieldValues();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IDataField#setData(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void setData(Object data)
	{
		if (data == null) {
			setSelection((Collection<MultiSelectionStructFieldValue>)null);
		} else if (data instanceof MultiSelectionStructFieldValue) {
			setSelection((MultiSelectionStructFieldValue) data);
		} else if (data instanceof Collection<?>) {
			// XXX is this ok?
			setSelection((Collection<MultiSelectionStructFieldValue>) data);
		} else if (data instanceof MultiSelectionDataField) {
			this.structFieldValueIDs = null;
			this.structFieldValueIDs_correct.clear();
			this.structFieldValueIDs_correct.addAll(
					((MultiSelectionDataField) data).getStructFieldValueIDs()
			);
			this.structFieldValues = ((MultiSelectionDataField) data).structFieldValues;
			// XXX what about the field structFieldValues ???
		}
		resetTextBuffer();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IDataField#supportsInputType(java.lang.Class)
	 */
	@Override
	public boolean supportsInputType(Class<?> inputType)
	{
		return
			MultiSelectionStructFieldValueID.class.isAssignableFrom(inputType) ||
			// XXX is this ok?
			Collection.class.isAssignableFrom(inputType) ||
			MultiSelectionDataField.class.isAssignableFrom(inputType);
	}
	
	/**
	 * Adds the given {@link Collection} of {@link MultiSelectionStructFieldValue}s to the selection.
	 * @param values the {@link MultiSelectionStructFieldValue}s to add to the selection.
	 */
	public void addSelection(Collection<MultiSelectionStructFieldValue> values) 
	{
		if (values == null || values.isEmpty())
			return;
		
		for (MultiSelectionStructFieldValue structFieldValue : values) {
			if (!structFieldValueIDs_correct.contains(structFieldValue.getStructFieldValueID()))
				structFieldValueIDs_correct.add(structFieldValue.getStructFieldValueID());	
		}
		structFieldValues = new HashSet<MultiSelectionStructFieldValue>(getStructFieldValues());
		for (MultiSelectionStructFieldValue structFieldValue : values) {
			if (!structFieldValues.contains(structFieldValue))
				structFieldValues.add(structFieldValue);
		}
		resetTextBuffer();
	}
	
	/**
	 * Adds the given {@link MultiSelectionStructFieldValue} to the selection.
	 * @param value the {@link MultiSelectionStructFieldValue} to add to the selection.
	 */
	public void addSelection(MultiSelectionStructFieldValue value) 
	{
		addSelection(Collections.singleton(value));
	}
	
	/**
	 * Removes the given {@link Collection} of {@link MultiSelectionStructFieldValue}s from the selection.
	 * @param values the {@link MultiSelectionStructFieldValue}s to remove from the selection.
	 */
	public void removeSelection(Collection<MultiSelectionStructFieldValue> values) 
	{
		if (values == null || values.isEmpty())
			return;

		for (MultiSelectionStructFieldValue structFieldValue : values) {
			structFieldValueIDs_correct.remove(structFieldValue.getStructFieldValueID());	
		}
		structFieldValues = new HashSet<MultiSelectionStructFieldValue>(getStructFieldValues());
		structFieldValues.removeAll(values);
		resetTextBuffer();
	}
	
	/**
	 * Removes the given {@link MultiSelectionStructFieldValue} from the selection.
	 * @param value the {@link MultiSelectionStructFieldValue} to remove from the selection.
	 */
	public void removeSelection(MultiSelectionStructFieldValue value) 
	{
		removeSelection(Collections.singleton(value));
	}
}
