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

package org.nightlabs.jfire.jdo.organisationsync;

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
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(DirtyObjectIDBufferFileSystem.class);

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
				"jfire" + File.separatorChar + "dirtyObjectIDs",
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
	 * @see org.nightlabs.jfire.jdo.organisationsync.DirtyObjectIDBuffer#addDirtyObjectIDs(java.util.Collection)
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
							logger.error("Deleting incomplete file \"" + objectIDFile.getAbsolutePath() + "\" failed for unknown reason!");

					} catch (Throwable t) {
						logger.error("Deleting incomplete file \"" + objectIDFile.getAbsolutePath() + "\" failed with exception!", t);
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
	 * @see org.nightlabs.jfire.jdo.organisationsync.DirtyObjectIDBuffer#fetchDirtyObjectIDs()
	 */
	public synchronized Set fetchDirtyObjectIDs() throws DirtyObjectIDBufferException
	{
		if (filesInProcess != null)
			logger.warn("fetchDirtyObjectIDs() called again before clearFetchedDirtyObjectIDs()! Maybe there was an error during last execution cycle.", new Exception());

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
	 * @see org.nightlabs.jfire.jdo.organisationsync.DirtyObjectIDBuffer#clearFetchedDirtyObjectIDs()
	 */
	public synchronized void clearFetchedDirtyObjectIDs() throws DirtyObjectIDBufferException
	{
		if (filesInProcess != null) {
			for (Iterator it = filesInProcess.iterator(); it.hasNext(); ) {
				File file = (File) it.next();
				if (!file.delete())
					logger.error("Could not delete file: " + file.getAbsolutePath());
			}
		}

		filesInProcess = null;
	}

}
