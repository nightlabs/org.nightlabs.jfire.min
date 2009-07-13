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

import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.i18n.I18nTextBuffer;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructFieldValueID;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.prop.structfield.StructFieldValue;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 *
 * {@link DataField} that holds one member of type {@link StructFieldValue} defining the users
 * choice from the list within {@link SelectionStructField}.
 *
 * @jdo.persistence-capable identity-type="application"
 *                          persistence-capable-superclass="org.nightlabs.jfire.prop.DataField"
 *                          detachable="true"
 *                          table="JFireBase_Prop_SelectionDataField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.fetch-group name="FetchGroupsProp.fullData" fetch-groups="default"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_SelectionDataField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsProp.fullData",
		members={})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class SelectionDataField
extends DataField
implements DetachCallback, II18nTextDataField
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 20090116L;
	
	/** @jdo.field persistence-modifier="none" */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient I18nTextBuffer textBuffer = null;

	/**
	 * For JDO only.
	 */
	protected SelectionDataField()
	{
		super();
	}

	/**
	 * Create a new {@link SelectionDataField} for the given {@link DataBlock}
	 * that represents the given {@link StructField}.
	 * 
	 * @param dataBlock The {@link DataBlock} the new {@link SelectionDataField} will be part of.
	 * @param structField The {@link StructField} the new {@link SelectionDataField} represents in the data structure.
	 */
	public SelectionDataField(DataBlock dataBlock, StructField<SelectionDataField> structField)
	{
		super(dataBlock, structField);
	}

	/**
	 * Used for cloning.
	 */
	protected SelectionDataField(String organisationID, long propertySetID, SelectionDataField cloneField)
	{
		super(organisationID, propertySetID, cloneField);
	}

	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	protected String structFieldValueID;

		/** @jdo.field persistence-modifier="none" */
		@Persistent(persistenceModifier=PersistenceModifier.NONE)
	protected StructFieldValue structFieldValue;
	

	/**
	 * Set the {@link StructFieldValue} selected for this {@link SelectionDataField}.
	 * Note, that only the {@link StructFieldValue#getStructFieldValueID()}
	 * is stored in this data field.
	 * 
	 * @param selection The selection to set.
	 */
	public void setSelection(StructFieldValue value)
	{
		if (value == null)
			structFieldValueID = null;
		else
			structFieldValueID = value.getStructFieldValueID();
		structFieldValue = value;
		updateTextBuffer();
	}

	/**
	 * Returns the {@link StructFieldValue#getStructFieldValueID()} of the selected
	 * {@link StructFieldValue} selected for this {@link SelectionDataField}.
	 * The {@link StructFieldValue} corresponding to the selection can be
	 * retrieved using {@link #getStructFieldValue()}.
	 * 
	 * @return The structFieldValueID of the selected {@link StructFieldValue}.
	 */
	public String getStructFieldValueID() {
		return structFieldValueID;
	}

	/**
	 * Returns the selected {@link StructFieldValue} of this SelectionStructField.
	 * <p>
	 * If this is called on a persistent instance the value is resolved from datastore.
	 * </p>
	 * <p>
	 * If this is called on a detached instance the value must have been resolved during
	 * the detach process. This will be done if the fetch plan contains {@link PropertySet#FETCH_GROUP_FULL_DATA}.
	 * (see {@link #jdoPostDetach(Object)}).
	 * </p>
	 * @return The selected {@link StructFieldValue} of this SelectionStructField.
	 */
	public StructFieldValue getStructFieldValue() {
		if (JDOHelper.isPersistent(this))
			return resolveStructFieldValue(JDOHelper.getPersistenceManager(this), this);
		return structFieldValue;
	}

	/**
	 * Casts {@link DataField#getStructField()} as {@link SelectionStructField}
	 *
	 * @throws ClassCastException
	 *           If class cast fails
	 * @return The associated struct field as {@link SelectionStructField}
	 */
	public SelectionStructField getSelectionStructField(IStruct structure) throws ClassCastException
	{
		IllegalStateException ill = null;
		SelectionStructField field = null;
		try
		{
			field = (SelectionStructField) structure.getStructField(getStructBlockOrganisationID(), getStructBlockID(),
					getStructFieldOrganisationID(), getStructFieldID());
		}
		catch (Exception e)
		{
			ill = new IllegalStateException("Caught exception while accessing PropStructFactory");
			ill.initCause(e);
		}
		if (ill != null)
			throw ill;
		return field;
	}

	@Override
	public boolean isEmpty()
	{
		if (getStructField() == null)
			throw new IllegalStateException("isEmpty() was called on deflated data field.");
		
		return getStructFieldValueID() == null && ((SelectionStructField) getStructField()).getDefaultValue() == null;
	}

	@Override
	public DataField cloneDataField(PropertySet propertySet) {
		SelectionDataField newField = new SelectionDataField(getOrganisationID(), propertySet.getPropertySetID(), this);
		newField.structFieldValueID = this.structFieldValueID;
		return newField;
	}

	private void updateTextBuffer() {
		StructFieldValue value = getStructFieldValue();
		if (value != null) {
			getI18nTextBuffer().copyFrom(value.getValueName());
		} else {
			getI18nTextBuffer().clear();
		}
	}

	private I18nTextBuffer getI18nTextBuffer() {
		if (textBuffer == null) {
			textBuffer = new I18nTextBuffer();
		}
		return textBuffer;
	}
	@Override
	public I18nTextBuffer getI18nText() {
		if (textBuffer == null) {
			getI18nTextBuffer();
			updateTextBuffer();
		}
		return textBuffer;
	}

	/**
	 * In the post detach callback the {@link SelectionDataField} will check whether
	 * {@link PropertySet#FETCH_GROUP_FULL_DATA} is in the fetchplan and resolve the
	 * {@link StructFieldValue} that is selected for this field. It will then detach
	 * the value and set the non-persistent member structFieldValue (see {@link #getStructFieldValue()}).
	 */
	@Override
	public void jdoPostDetach(Object detached) {
		SelectionDataField attached = (SelectionDataField) detached;
		PersistenceManager pm = JDOHelper.getPersistenceManager(attached);
		if (attached.getStructFieldValueID() != null && pm.getFetchPlan().getGroups().contains(PropertySet.FETCH_GROUP_FULL_DATA)) {
			// this is a set of String, but we don't need the type information here - so we need no cast
			Set<?> oldFetchGroups = pm.getFetchPlan().getGroups();
			int oldFetchDepth = pm.getFetchPlan().getMaxFetchDepth();
			try {
				StructFieldValueID structFieldValueID = StructFieldValueID.create(
						attached.getStructBlockOrganisationID(), attached.getStructBlockID(),
						attached.getStructFieldOrganisationID(), attached.getStructFieldID(), attached.getStructFieldValueID());

				pm.getFetchPlan().setGroups(new String[] {FetchPlan.DEFAULT, StructFieldValue.FETCH_GROUP_VALUE_NAME});
				pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
				StructFieldValue value = (StructFieldValue) pm.getObjectById(structFieldValueID);
				value = pm.detachCopy(value);
				this.structFieldValue = value;
			} finally {
				pm.getFetchPlan().setGroups(oldFetchGroups);
				pm.getFetchPlan().setMaxFetchDepth(oldFetchDepth);
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
	 * Resolves the {@link StructFieldValue} that is selected by the given dataField.
	 *
	 * @param pm The PersistenceManager to use.
	 * @param dataField The {@link SelectionDataField} to resolve the selection for.
	 * @return The {@link StructFieldValue} that is selected by the given dataField.
	 */
	public static StructFieldValue resolveStructFieldValue(PersistenceManager pm, SelectionDataField dataField) {
		if (dataField.getStructFieldValueID() == null)
			return null;
		StructFieldValueID structFieldValueID = StructFieldValueID.create(
				dataField.getStructBlockOrganisationID(), dataField.getStructBlockID(),
				dataField.getStructFieldOrganisationID(), dataField.getStructFieldID(), dataField.getStructFieldValueID());
		return (StructFieldValue) pm.getObjectById(structFieldValueID);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This is equal to calling {@link #getStructFieldValue()}.
	 * </p>
	 */
	@Override
	public Object getData() {
		return getStructFieldValue();
	}

	@Override
	public void setData(Object data) {
		if (data == null) {
			setSelection(null);
		} else if (data instanceof StructFieldValue) {
			setSelection((StructFieldValue) data);
		} else if (data instanceof SelectionDataField) {
			this.structFieldValueID = ((SelectionDataField) data).structFieldValueID;
		}
	}

	@Override
	public boolean supportsInputType(Class<?> inputType) {
		return 
			StructFieldValue.class.isAssignableFrom(inputType) || 
			SelectionDataField.class.isAssignableFrom(inputType);
	}
}
