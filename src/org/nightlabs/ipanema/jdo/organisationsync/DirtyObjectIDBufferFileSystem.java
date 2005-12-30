/*
 * Created on Sep 16, 2005
 */
package org.nightlabs.ipanema.jdo.organisationsync;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StreamTokenizer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import org.nightlabs.jdo.ObjectIDUtil;

public class DirtyObjectIDBufferFileSystem
implements DirtyObjectIDBuffer
{
	public static final Logger LOGGER = Logger.getLogger(DirtyObjectIDBufferFileSystem.class);

	private OrganisationSyncManagerFactory organisationSyncManagerFactory;
	private File workDirectory;

	private String fileNamePrefix = Long.toHexString(System.currentTimeMillis()) + '-';
	private long nextFileID = 0;
	private Object nextFileIDMutex = new Object();

	/**
	 * @return Returns a new File instance that references a file which does NOT yet exist.
	 */
	protected File createObjectIDFile()
	{
		synchronized (nextFileIDMutex) {
			return new File(workDirectory, fileNamePrefix + Long.toHexString(++nextFileID) + ".objectIDs");
		}
	}

	public DirtyObjectIDBufferFileSystem() { }

	public void init(OrganisationSyncManagerFactory organisationSyncManagerFactory) throws DirtyObjectIDBufferException
	{
		this.organisationSyncManagerFactory = organisationSyncManagerFactory;
		workDirectory = new File(
				System.getProperty("java.io.tmpdir") + File.separatorChar +
				"ipanema" + File.separatorChar + "dirtyObjectIDs",
				this.organisationSyncManagerFactory.getOrganisationID());
		if (!workDirectory.exists() && !workDirectory.mkdirs())
			throw new DirtyObjectIDBufferException("Could not create directory: " + workDirectory.getAbsolutePath());
	}

	/**
	 * While a file is written in {@link #addDirtyObjectIDs(Collection)}, it is locked here and
	 * ignored in {@link #fetchDirtyObjectIDs()}. The file is unlocked at the end of
	 * <code>addDirtyObjectIDs(Collection)</code> when it is closed and ready to be processed. 
	 */
	private Set lockedFiles = Collections.synchronizedSet(new HashSet());

	/**
	 * @see org.nightlabs.ipanema.jdo.organisationsync.DirtyObjectIDBuffer#addDirtyObjectIDs(java.util.Collection)
	 */
	public void addDirtyObjectIDs(Collection objectIDs) throws DirtyObjectIDBufferException
	{
		try {
			File objectIDFile = createObjectIDFile();
			boolean successful = false;
			lockedFiles.add(objectIDFile);
			try {
				FileWriter fw = new FileWriter(objectIDFile);
				try {
					for (Iterator it = objectIDs.iterator(); it.hasNext(); ) {
						fw.append(it.next().toString());
						fw.append('\n');
					}
				} finally {
					fw.close();
				}

				successful = true;
			} finally {
				if (!successful) { // if the file was not successfully created, we delete it.
					try {
						if (objectIDFile.exists() && !objectIDFile.delete())
							LOGGER.error("Deleting incomplete file \"" + objectIDFile.getAbsolutePath() + "\" failed for unknown reason!");

					} catch (Throwable t) {
						LOGGER.error("Deleting incomplete file \"" + objectIDFile.getAbsolutePath() + "\" failed with exception!", t);
					}
				} // if (!successful) {
				lockedFiles.remove(objectIDFile);
			}
		} catch (Exception x) {
			throw new DirtyObjectIDBufferException(x);
		}
	}

	/**
	 * {@link #fetchDirtyObjectIDs()} stores all read files into this Set in order to delete these files
	 * (after successful processing) in {@link #clearFetchedDirtyObjectIDs()}.
	 */
	private Set filesInProcess = null;

	/**
	 * @see org.nightlabs.ipanema.jdo.organisationsync.DirtyObjectIDBuffer#fetchDirtyObjectIDs()
	 */
	public synchronized Set fetchDirtyObjectIDs() throws DirtyObjectIDBufferException
	{
		if (filesInProcess != null)
			LOGGER.warn("fetchDirtyObjectIDs() called again before clearFetchedDirtyObjectIDs()! Maybe there was an error during last execution cycle.", new Exception());

		filesInProcess = new HashSet();
		HashSet res = new HashSet();

		try {
			File[] files = workDirectory.listFiles();
			for (int i = 0; i < files.length; ++i) {
				File file = files[i];

				if (lockedFiles.contains(file))
					continue;

				filesInProcess.add(file);

				FileReader fr = new FileReader(file);
				try {
					StreamTokenizer st = new StreamTokenizer(fr);
					st.resetSyntax();
					st.wordChars(0, ((int)'\n') - 1);
					st.wordChars(((int)'\n') + 1, Integer.MAX_VALUE);
//					st.whitespaceChars('\n', '\n');
					while (StreamTokenizer.TT_EOF != st.nextToken()) {
						if (StreamTokenizer.TT_EOL != st.ttype && st.sval != null) {
							Object objectID = ObjectIDUtil.createObjectID(st.sval);
							res.add(objectID);
						}
					}
				} finally {
					fr.close();
				}
			}

			return res;
		} catch (Exception x) {
			throw new DirtyObjectIDBufferException(x);
		}
	}

	/**
	 * @see org.nightlabs.ipanema.jdo.organisationsync.DirtyObjectIDBuffer#clearFetchedDirtyObjectIDs()
	 */
	public synchronized void clearFetchedDirtyObjectIDs() throws DirtyObjectIDBufferException
	{
		if (filesInProcess != null) {
			for (Iterator it = filesInProcess.iterator(); it.hasNext(); ) {
				File file = (File) it.next();
				if (!file.delete())
					LOGGER.error("Could not delete file: " + file.getAbsolutePath());
			}
		}

		filesInProcess = null;
	}

}
