package org.nightlabs.jfire.prop;

public interface IDataField {

	/**
	 * Determines if this field is empty. Returning false here leads to the field not being stored in the datastore but instead
	 * being thrown away while deflating.
	 */
	public abstract boolean isEmpty();

	public abstract boolean supportsInputType(Class<?> inputType);

	/**
	 * Set the data of this {@link DataField}.
	 * <p>
	 * Whether a specific input type is supported by the implementation 
	 * of {@link DataField} can be queried using {@link #supportsInputType(Class)}.
	 * </p>
	 * <p>
	 * Implementations should throw an {@link IllegalArgumentException}
	 * when unsupported data is passed here.
	 * </p> 
	 * <p>
	 * Each implementation of DataField should, however, support an instance of the 
	 * same implementation to be passed to this method and copy the data from the
	 * passed instance then.
	 * </p>
	 * <p>
	 * Additionally each implementation must support the data type in {@link #setData(Object)}
	 * that it returns in {@link #getData()}.
	 * </p>
	 * <p>
	 * Implementations should be able to manage <code>null</code> arguments. 
	 * </p>
	 * @param data The data to set.
	 */
	public abstract void setData(Object data);

	/**
	 * Returns the data of this {@link DataField} in the most appropriate way.
	 * <p>
	 * Note that each DataField must support the data returned here in {@link #setData(Object)}.
	 * </p>
	 * <p>
	 * Also note, that the data passed here might not reveal the complete information of a {@link DataField}.
	 * </p> 
	 * @return The data of this {@link DataField}.
	 */
	public abstract Object getData();

}