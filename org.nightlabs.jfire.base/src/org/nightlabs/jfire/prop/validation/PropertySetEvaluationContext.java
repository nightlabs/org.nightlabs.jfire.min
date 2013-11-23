package org.nightlabs.jfire.prop.validation;

import java.util.Collection;
import java.util.LinkedList;

import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.exception.PropertyException;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * Evaluation context for evaluation of expressions with {@link PropertySet}s. It holds
 * the {@link PropertySet} in which the evaluation is to take place along with the
 * corresponding {@link IStruct}.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class PropertySetEvaluationContext implements IPropertySetEvaluationContext<DataField>
{
	private PropertySet propertySet;
	private IStruct struct;

	/**
	 * Creates a new instance with the given {@link PropertySet} and {@link IStruct}.
	 * @param propertySet The {@link PropertySet} of the context.
	 * @param struct The {@link IStruct} of the context.
	 */
	public PropertySetEvaluationContext(PropertySet propertySet, IStruct struct) {
		this.propertySet = propertySet;
		this.struct = struct;
	}

	@Override
	public Collection<DataField> getDataFields(StructFieldID structFieldID) {
		Collection<DataField> dataFields = new LinkedList<DataField>();
		try {
			for (DataBlock dataBlock : propertySet.getDataBlockGroup(structFieldID.structBlockOrganisationID, structFieldID.structBlockID).getDataBlocks()) {
				dataFields.add(dataBlock.getDataField(structFieldID));
			}
		} catch (PropertyException e) {
			throw new RuntimeException(e);
		}

		return dataFields;
	}

	public IStruct getStruct() {
		return struct;
	}
}
