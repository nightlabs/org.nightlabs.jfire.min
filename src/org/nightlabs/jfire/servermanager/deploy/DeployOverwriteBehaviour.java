package org.nightlabs.jfire.servermanager.deploy;

/**
 * A parameter of this type controls what should happen, if a deployed file already
 * exists. There are three behaviours:
 * <ul>
 * {@link #EXCEPTION}: Do not touch the already deployed file and throw a {@link DeployedFileAlreadyExistsException}.
 * {@link #KEEP}: Do not touch the already deployed file, but keep it and silently return.
 * {@link #OVERWRITE}: Replace the destination file.
 * </ul>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public enum DeployOverwriteBehaviour {
	EXCEPTION,
	KEEP,
	OVERWRITE
}
