package org.nightlabs.jfire.prop.validation;

import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;

/**
 * Concretion of {@link IPropertyElementValidator} for the validation of data fields.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @param <DataFieldType>
 * @param <StructFieldType>
 */
public interface IDataFieldValidator<DataFieldType extends DataField, StructFieldType extends StructField<DataFieldType>>
extends IPropertyElementValidator<DataFieldType, StructFieldType>
{

}
