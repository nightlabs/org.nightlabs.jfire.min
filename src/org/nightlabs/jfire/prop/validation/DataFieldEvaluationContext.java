package org.nightlabs.jfire.prop.validation;

import java.util.Collection;
import java.util.Collections;

import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * Implementation of  {@link IPropertySetEvaluationContext} for {@link DataField}s.
 * It holds a single {@link DataField} along with the corresponding {@link StructField}.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @param <DataFieldType>
 */
public class DataFieldEvaluationContext<DataFieldType extends DataField> implements IPropertySetEvaluationContext<DataFieldType>
{
	private Collection<DataFieldType> dataFields;
	private StructField<DataFieldType> structField;

	public DataFieldEvaluationContext(DataFieldType dataField, StructField<DataFieldType> structField) {
		this.dataFields = Collections.singleton(dataField);
		this.structField = structField;
	}

	@Override
	public Collection<DataFieldType> getDataFields(StructFieldID structFieldID) {
		return dataFields;
	}

	public StructField<DataFieldType> getStructField() {
		return structField;
	}
}
