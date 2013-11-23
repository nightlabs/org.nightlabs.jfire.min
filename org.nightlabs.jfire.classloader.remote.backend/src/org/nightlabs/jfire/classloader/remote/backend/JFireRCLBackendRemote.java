package org.nightlabs.jfire.classloader.remote.backend;

import java.util.List;
import java.util.Map;

import javax.ejb.Remote;

@Remote
public interface JFireRCLBackendRemote
{
	/**
	 * @param rmd the meta-data describing the requested resource.
	 * @return the requested resource in binary form.
	 * @throws ClassLoaderException if an error occurs while scanning or while reading the requested resource.
	 */
	byte[] getResourceBytes(ResourceMetaData rmd) throws ClassLoaderException;

	/**
	 * @return A zipped serialized instance of {@link Map} with the key <code>String fileName</code> and the value
	 *		<code>{@link List}&lt;{@link ResourceMetaData}&gt; fileMetaData</code>. <code>fileName</code> is relative to the repository
	 *		directory or within the jar (never starting with "/"!).
	 */
	byte[] getResourcesMetaDataMapBytes() throws ClassLoaderException;

	/**
	 * This method can be used to check whether the resource's meta-data as returned by
	 * {@link #getResourcesMetaDataMapBytes()} needs to be downloaded or whether the client
	 * already has a version that's up-to-date.
	 *
	 * @return The timestamp of the map returned by {@link #getResourcesMetaDataMapBytes()}.
	 */
	long getResourcesMetaDataMapBytesTimestamp() throws ClassLoaderException;

	String ping(String message);
}