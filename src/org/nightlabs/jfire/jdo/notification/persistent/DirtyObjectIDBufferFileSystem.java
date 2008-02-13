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

package org.nightlabs.jfire.jdo.notification.persistent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.util.Util;

public class DirtyObjectIDBufferFileSystem
implements DirtyObjectIDBuffer
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(DirtyObjectIDBufferFileSystem.class);

	private PersistentNotificationManagerFactory persistentNotificationManagerFactory;
	private File workDirectory;

	private String fileNamePrefix = Long.toHexString(System.currentTimeMillis()) + '-';
	private long nextFileID = 0;
	private Object nextFileIDMutex = new Object();

	/**
	 * @return Returns a new File instance that references a file which does NOT yet exist.
	 */
	protected File createDirtyObjectIDFile()
	{
		long fileID;
		synchronized (nextFileIDMutex) {
			fileID = nextFileID++;
		}

		return new File(
				workDirectory,
				fileNamePrefix + Util.addLeadingZeros(Long.toHexString(fileID), 8) + ".ser");
	}

	public void init(PersistentNotificationManagerFactory persistentNotificationManagerFactory) throws DirtyObjectIDBufferException
	{
		this.persistentNotificationManagerFactory = persistentNotificationManagerFactory;
		try {
			workDirectory = new File(
					new File(Util.getTempDir(), "jfire" + File.separatorChar + "dirtyObjectIDsRaw"),
					this.persistentNotificationManagerFactory.getOrganisationID());
		} catch (Exception x) {
			throw new DirtyObjectIDBufferException(x);
		}
		if (!workDirectory.exists() && !workDirectory.mkdirs())
			throw new DirtyObjectIDBufferException("Could not create directory: " + workDirectory.getAbsolutePath());
	}

	/**
	 * While a file is written in {@link #addDirtyObjectIDs(Map)}, it is locked here and
	 * ignored in {@link #fetchDirtyObjectIDs()}. The file is unlocked at the end of
	 * <code>addDirtyObjectIDs(Collection)</code> when it is closed and ready to be processed.
	 */
	private Set<File> lockedFiles = Collections.synchronizedSet(new HashSet<File>());

	@Implement
	public void addDirtyObjectIDs(Map<JDOLifecycleState, Map<Object, DirtyObjectID>> dirtyObjectIDs) throws DirtyObjectIDBufferException
	{
		try {
			File dirtyObjectIDFile = createDirtyObjectIDFile();
			boolean successful = false;
			lockedFiles.add(dirtyObjectIDFile);
			try {
				OutputStream out = new BufferedOutputStream(new FileOutputStream(dirtyObjectIDFile));
				try {
					ObjectOutputStream o = new ObjectOutputStream(out);
					try {
						o.writeObject(dirtyObjectIDs);
					} finally {
						o.close();
					}
				} finally {
					out.close();
				}
//				FileWriter fw = new FileWriter(objectIDFile);
//				try {
//					for (Iterator it = objectIDs.iterator(); it.hasNext(); ) {
//						fw.append(it.next().toString());
//						fw.append('\n');
//					}
//				} finally {
//					fw.close();
//				}

				successful = true;
			} finally {
				if (!successful) { // if the file was not successfully created, we delete it.
					try {
						if (dirtyObjectIDFile.exists() && !dirtyObjectIDFile.delete())
							logger.error("Deleting incomplete file \"" + dirtyObjectIDFile.getAbsolutePath() + "\" failed for unknown reason!");

					} catch (Throwable t) {
						logger.error("Deleting incomplete file \"" + dirtyObjectIDFile.getAbsolutePath() + "\" failed with exception!", t);
					}
				} // if (!successful) {
				lockedFiles.remove(dirtyObjectIDFile);
			}
		} catch (Exception x) {
			throw new DirtyObjectIDBufferException(x);
		}
	}

	/**
	 * {@link #fetchDirtyObjectIDs()} stores all read files into this Set in order to delete these files
	 * (after successful processing) in {@link #clearFetchedDirtyObjectIDs()}.
	 */
	private Set<File> filesInProcess = null;

	@SuppressWarnings("unchecked")
	@Implement
	public synchronized Collection<Map<JDOLifecycleState, Map<Object, DirtyObjectID>>> fetchDirtyObjectIDs() throws DirtyObjectIDBufferException
	{
		if (filesInProcess != null)
			logger.warn("fetchDirtyObjectIDs() called again before clearFetchedDirtyObjectIDs()! Maybe there was an error during last execution cycle.", new Exception());

		filesInProcess = new HashSet<File>();
		ArrayList<Map<JDOLifecycleState, Map<Object, DirtyObjectID>>> res = new ArrayList<Map<JDOLifecycleState,Map<Object,DirtyObjectID>>>();

		try {
			File[] files = workDirectory.listFiles();
			for (int i = 0; i < files.length; ++i) {
				File file = files[i];

				if (lockedFiles.contains(file))
					continue;

				filesInProcess.add(file);

				InputStream in = new BufferedInputStream(new FileInputStream(file));
				try {
					ObjectInputStream ois = new ObjectInputStream(in);
					try {
						res.add((Map<JDOLifecycleState, Map<Object, DirtyObjectID>>)ois.readObject());
					} finally {
						ois.close();
					}
				} finally {
					in.close();
				}
			}

			return res;
		} catch (Exception x) {
			throw new DirtyObjectIDBufferException(x);
		}
	}

	@Implement
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
