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

package org.nightlabs.jfire.classloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.DeflaterOutputStream;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.io.DataBuffer;
import org.nightlabs.jfire.base.JFireBasePrincipal;
import org.nightlabs.jfire.classloader.CLRegistryCfMod.ResourceRepository;
import org.nightlabs.jfire.classloader.xml.CLRepositoryFileFilter;
import org.nightlabs.jfire.classloader.xml.CLRepositoryMan;
import org.nightlabs.jfire.classloader.xml.CLRepositoryMan.Publication;
import org.nightlabs.util.CacheDirTag;
import org.nightlabs.util.IOUtil;
import org.nightlabs.xml.XMLReadException;
import org.xml.sax.SAXException;

/**
 * @author marco schulze - marco at nightlabs dot de
 */
public class CLRegistrarFactory
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(CLRegistrarFactory.class);
	
	protected CLRegistryCfMod clRegistryCfMod;

	public CLRegistrarFactory(CLRegistryCfMod _clRegistryCfMod)
	{
		this.clRegistryCfMod = _clRegistryCfMod;
	}
	
	protected boolean loaded = false;

	/**
	 * This method returns always a new instance of <tt>CLRegistrar</tt>.
	 */
	public CLRegistrar getCLRegistrar(JFireBasePrincipal principal)
	{
		return new CLRegistrar(this, principal);
	}

	/**
	 * key: String fileName<br/>
	 * value: List of ResourceMetaData fileMetaData
	 * <br/><br/>
	 * fileName is relative to the repository directory or within the jar (never starting with "/"!).
	 */
	protected Map<String, List<ResourceMetaData>> resources = new HashMap<String, List<ResourceMetaData>>();

	/**
	 * This method returns a List of ResourceMetaData for all resources that match
	 * the given name. There may be many, because the j2ee server has multiple
	 * repositories (at least lib and deploy) and even within one repository, there may
	 * be multiple jars providing the same resource.
	 *
	 * @param name
	 * @return
	 * @throws IOException
	 */
	protected synchronized List<ResourceMetaData> getResourcesMetaData(String name)
		throws ModuleException
	{
		try {
			if (!loaded)
				scan();

			return resources.get(name);
		} catch (Exception e) {
			throw new ModuleException(e);
		}
	}

	protected synchronized byte[] getResourcesMetaDataMapBytes()
		throws ModuleException
	{
		if (resourcesMetaDataMapBytes != null)
			return resourcesMetaDataMapBytes;

		try {
			if (!loaded)
				scan();

			DataBuffer dbuf = new DataBuffer();
			ObjectOutputStream out = new ObjectOutputStream(new DeflaterOutputStream(dbuf.createOutputStream()));
			try {
				out.writeObject(resources);
			} finally {
				out.close();
			}

			resourcesMetaDataMapBytes = dbuf.createByteArray();
			resourcesMetaDataMapBytes_timestamp = System.currentTimeMillis();
			return resourcesMetaDataMapBytes;
		} catch (Exception e) {
			throw new ModuleException(e);
		}
	}
	
	protected synchronized long getResourcesMetaDataMapBytesTimestamp()
		throws ModuleException
	{
		getResourcesMetaDataMapBytes(); // to initialize our byte array and its timestamp
		return resourcesMetaDataMapBytes_timestamp;
	}

	protected byte[] resourcesMetaDataMapBytes = null;
	protected long resourcesMetaDataMapBytes_timestamp = 0;

	protected synchronized void clear()
	{
		loaded = false;
		resourcesMetaDataMapBytes = null;
		resources.clear();
		resourceRepositories.clear();

		// we recursively delete our temp repository if it exists
		File tempRepositoryFile = new File(clRegistryCfMod.getTempRepository().getPath());
		if (!IOUtil.deleteDirectoryRecursively(tempRepositoryFile))
			logger.error("Deleting temporary repository \""+clRegistryCfMod.getTempRepository().getPath()+"\" failed!");
	}

	/**
	 * key: String repositoryName<br/>
	 * value: ResourceRepository repository
	 */
	protected Map<String, ResourceRepository> resourceRepositories = new HashMap<String, ResourceRepository>();

	/**
	 * The jars that have been temporarily extracted. Instances of File.
	 */
	protected List<File> tempJarFiles = null;

	protected synchronized void scan()
		throws XMLReadException, SAXException, IOException, TransformerException
	{
		logger.info("CLRegistrarFactory.scan(): start indexing all resources...");
		clRegistryCfMod.acquireReadLock();
		try {
			clear();

			// tag the temp repository with CacheDirTag
			File tempRepositoryFile = new File(clRegistryCfMod.getTempRepository().getPath());
			new CacheDirTag(tempRepositoryFile).tag("http://JFire.org - JFireRemoteClassLoader (Temporary Repository)", true, false);

			resourceRepositories.put(clRegistryCfMod.getTempRepository().getName(), clRegistryCfMod.getTempRepository());
			tempJarFiles = new ArrayList<File>();
			for (Iterator<ResourceRepository> it = clRegistryCfMod.getResourceRepositories().iterator(); it.hasNext(); ) {
				ResourceRepository repository = it.next();
				resourceRepositories.put(repository.getName(), repository);
				scanDirectory(repository, new File(repository.getPath()).getAbsoluteFile(), null);
			}
			// Iterate through all temporarily extracted jars.
			// We cannot use an iterator here, because items are added to the List while
			// it is iterated.
			for (int i = 0; i < tempJarFiles.size(); ++i) {
				File tmpJarFile = tempJarFiles.get(i); // must be within temp repository
				scanDirectory(clRegistryCfMod.getTempRepository(), tmpJarFile.getAbsoluteFile(), null);
			}
			tempJarFiles = null;
			loaded = true;
		} finally {
			clRegistryCfMod.releaseLock();
		}
		logger.info("CLRegistrarFactory.scan(): completed indexing all resources!");
	}

	private CLRepositoryFileFilter clRepositoryFileFilter = null;

	/**
	 * This method scans a directory. If the given repository has the recursiveSubDirs
	 * flag set, this method dives into subdirs by calling itself recursively.
	 *
	 * @param repository
	 * @param directory
	 * @throws XMLReadException
	 * @throws IOException
	 * @throws TransformerException
	 * @throws SAXException
	 */
	protected void scanDirectory(ResourceRepository repository, File directory, CLRepositoryMan clRepositoryMan)
		throws XMLReadException, SAXException, IOException, TransformerException
	{
		if (!directory.isDirectory())
			throw new IllegalArgumentException("directory \""+directory.getAbsolutePath()+"\" is not a directory!");

		File absoluteRepositoryFile = new File(repository.getPath()).getAbsoluteFile();
		
//	 read all CL Repository XML files, we have in the current directory
		if (clRepositoryFileFilter == null)
			clRepositoryFileFilter = new CLRepositoryFileFilter();

		String[] clRepositoryFiles = directory.list(clRepositoryFileFilter);
		for (int i = 0; i < clRepositoryFiles.length; ++i) {
			File clRepositoryFile = new File(directory, clRepositoryFiles[i]);
			if (clRepositoryMan == null)
				clRepositoryMan = new CLRepositoryMan(null);

			clRepositoryMan.readCLRepositoryXML(clRepositoryFile);
		}

		String[] dirs = directory.list();
		iterateSubdirs: for (int i = 0; i < dirs.length; ++i) {
			File dir = new File(directory, dirs[i]);

			List<CLRepositoryMan.Publication> applicablePublications = null;
			if (clRepositoryMan != null)
				applicablePublications = clRepositoryMan.getApplicablePublications(dir.getName());

			if (applicablePublications != null) {
				for (CLRepositoryMan.Publication publication : applicablePublications) {
					if (publication.isIgnore()) {
						if (logger.isDebugEnabled())
							logger.debug("scanDirectory: Ignoring file/directory completely: " + dir.getAbsolutePath());

						continue iterateSubdirs;
					}
				}
			}

			if (dir.isDirectory()) {
				if (repository.isRecursiveSubDirs()) {
					scanDirectory(repository, dir, new CLRepositoryMan(applicablePublications));
				}
			}
			else {
				String relativePath = IOUtil.getRelativePath(absoluteRepositoryFile, dir.getPath());
				ResourceMetaData fmd = new ResourceMetaData(repository.getName(), null, relativePath, dir.length(), dir.lastModified());

				boolean publishResource = false;
				if (applicablePublications != null) {
					for (Iterator<Publication> it = applicablePublications.iterator(); it.hasNext(); ) {
						CLRepositoryMan.Publication publication = it.next();
						if (publication.getCompositeResourcePattern().matcher(relativePath).matches()) {
							publishResource = true;
							break;
						}
					} // for (Iterator it = applicableTargets.iterator(); it.hasNext(); ) {
				} // if (applicablePublications != null) {

				if (publishResource)
					registerResource(fmd);

				JarFile jf = null;
				try {
					jf = new JarFile(dir, false);
				} catch (IOException x) {
					if (logger.isDebugEnabled())
						logger.debug("scanDirectory: " + dir.getAbsolutePath() + " is not a jar.");
				}
				if (jf != null) {
					try {
						scanJar(repository, fmd, jf, new CLRepositoryMan(applicablePublications));
					} catch (Throwable t) {
						logger.error(dir.getAbsolutePath() + " could not be read - it seems to be corrupt!", t);
					} finally {
						jf.close();
					}
				}
			}
		}
	}

	/**
	 * All files with these file extensions are extracted out of jars and
	 * put into a temp dir for being available to the client. Note, that
	 * the file extension does not matter for jars in the first search level (means
	 * directly in a directory), but only applys to jars that are wrapped within
	 * other jars.
	 */
	public static final String[] jarFileExts = new String[] {
			".jar",
			".rar"};
	
	/**
	 * This method scans a jar library and registers all resources from it. If it finds
	 * other jar files within the current lib, it extracts them to the temp repository
	 * and scans them afterwards.
	 * <br/><br/>
	 * The *clrepository.xml files must be in the root of the jar!
	 *
	 * @param repository
	 * @param jarFileMetaData
	 * @param jar
	 * @throws TransformerException
	 * @throws IOException
	 * @throws SAXException
	 * @throws XMLReadException
	 */
	protected void scanJar(ResourceRepository repository, ResourceMetaData jarFileMetaData, JarFile jar, CLRepositoryMan clRepositoryMan)
		throws XMLReadException, SAXException, IOException, TransformerException
	{
		// search and register clrepository xml files
		for (Enumeration<JarEntry> enumeration = jar.entries(); enumeration.hasMoreElements(); ) {
			JarEntry je = enumeration.nextElement();

			if (!je.isDirectory()
					&& je.getName().indexOf('/') < 0 // TODO Is it always a slash? Even under standard-abusive windows?
					&& je.getName().endsWith(CLRepositoryFileFilter.CLREPOSITORYFILESUFFIX))
			{
				if (clRepositoryMan == null)
					clRepositoryMan = new CLRepositoryMan(null);

				clRepositoryMan.readCLRepositoryXML(jar, je);
			}
		}

		List<Publication> applicableTargets = clRepositoryMan.getApplicablePublications(null); // with param null, it returns all targets

		// register resources in jar (if published) and extract nested jars
		for (Enumeration<JarEntry> enumeration = jar.entries(); enumeration.hasMoreElements(); ) {
			JarEntry je = enumeration.nextElement();
			if (!je.isDirectory()) {
				ResourceMetaData fmd = new ResourceMetaData(repository.getName(), jarFileMetaData.getPath(), je.getName(), je.getSize(), je.getTime());
				String relativePath = fmd.getPath();

				boolean publishResource = false;
				if (applicableTargets != null) {
					for (Iterator<Publication> it = applicableTargets.iterator(); it.hasNext(); ) {
						Publication target = it.next();
						if (target.getCompositeResourcePattern().matcher(relativePath).matches()) {
							publishResource = true;
							break;
						}
					} // for (Iterator it = applicableTargets.iterator(); it.hasNext(); ) {
				} // if (applicableTargets != null) {

				if (publishResource)
					registerResource(fmd);

				String lowerCaseFileName = je.getName().toLowerCase();
				boolean isJar = false;
				for (int i = 0; i < jarFileExts.length; ++i) {
					if (lowerCaseFileName.endsWith(jarFileExts[i])) {
						isJar = true;
						break;
					}
				}
				try {
					if (isJar) {
						// We extract wrapped jars into a temp repository to index them and make their content available
						// to the clients.
						File tempExtractedJar = new File(
								clRegistryCfMod.getTempRepository().getPath(),
								repository.getName() + File.separatorChar +
								jarFileMetaData.getPath() + File.separatorChar +
								je.getName() + File.separatorChar + "data.jar");

						boolean doExtract = !tempExtractedJar.exists();
						if (!doExtract) {
							if (tempExtractedJar.length() != je.getSize())
								doExtract = true;
							else if (Math.abs(tempExtractedJar.lastModified() - je.getTime()) > 10000)
								doExtract = true;
						}
						if (doExtract) {
							File dir = tempExtractedJar.getParentFile();
							if (!dir.exists() && !dir.mkdirs()) {
								logger.error("Failed to create the directory \"" + dir.getPath() + "\"!");
							}

							OutputStream out = new FileOutputStream(tempExtractedJar);
							try {
								InputStream in = jar.getInputStream(je);
								try {
									IOUtil.transferStreamData(in, out);
								} finally {
									in.close();
								}
							} finally {
								out.close();
							}
							tempExtractedJar.setLastModified(je.getTime());
						} // if (doExtract) {

						clRepositoryMan.writeCLRepositoryXML(
								new File(tempExtractedJar.getParent(), CLRepositoryFileFilter.CLREPOSITORYFILESUFFIX),
								"data.jar", false);

						tempJarFiles.add(tempExtractedJar.getParentFile());
					} // if (isJar) {
				} catch (IOException e) {
					logger.error("Extracting jar failed! repository=\"" + repository.getName() + "\" outerJar=\"" + jarFileMetaData.getPath() + "\" innerJar=\"" + je.getName() + "\"", e);
				}
			} // if (!je.isDirectory()) {
		}
	}
	protected void registerResource(ResourceMetaData fmd)
	{
		List<ResourceMetaData> resList = resources.get(fmd.getPath());
		if (resList == null) {
			resList = new ArrayList<ResourceMetaData>();
			resources.put(fmd.getPath(), resList);
		}
		resList.add(fmd);
	}

	protected byte[] getResourceBytes(ResourceMetaData rmd)
		throws ModuleException
	{
		try {
			synchronized (this) {
				if (!loaded)
					scan();
			}

			ResourceRepository repository = resourceRepositories.get(rmd.getRepositoryName());
			if (repository == null)
				throw new IllegalArgumentException("There is no resource repository existing with the name \"" + rmd.getRepositoryName() + "\"!");

			InputStream in = null;
			JarFile jf = null;
			try {
				if (rmd.getJar() == null) {
					File f = new File(repository.getPath(), rmd.getPath());
	
					if (!f.exists())
						throw new FileNotFoundException("File \"" + rmd.getPath() + "\" in repository \"" + rmd.getRepositoryName() + "\" does not exist!");
	
					if (rmd.getSize() >= 0 && f.length() != rmd.getSize())
						throw new FileNotFoundException("File \"" + rmd.getPath() + "\" in repository \"" + rmd.getRepositoryName() + "\" has a different size (" + f.length() + ") than defined by the ResourceMetaData (" + rmd.getSize() + ")! ResourceMetaData.size must be -1 or match exactly!");
	
					rmd.setSize(f.length());
					in = new FileInputStream(f);
				} // if (rmd.getJar() == null) {
				else {
				// if (rmd.getJar() != null) {
					File jarLocation = new File(repository.getPath(), rmd.getJar());
					jf = new JarFile(jarLocation);
					JarEntry je = jf.getJarEntry(rmd.getPath());
					if (je == null)
						throw new FileNotFoundException("File \"" + rmd.getPath() + "\" in jar \"" + rmd.getJar() + "\" in repository \"" + rmd.getRepositoryName() + "\" does not exist!");

					if (rmd.getSize() >= 0 && je.getSize() != rmd.getSize())
						throw new FileNotFoundException("File \"" + rmd.getPath() + "\" in jar \"" + rmd.getJar() + "\" in repository \"" + rmd.getRepositoryName() + "\" has a different size (" + je.getSize() + ") than defined by the ResourceMetaData (" + rmd.getSize() + ")! ResourceMetaData.size must be -1 or match exactly!");

					rmd.setSize(je.getSize());
					in = jf.getInputStream(je);
				}
				byte[] buf = new byte[(int)rmd.getSize()];
				int pos = 0;
				int bytesRead;
				do {
					bytesRead = in.read(buf, pos, buf.length - pos);
					pos += bytesRead;
				} while (bytesRead >= 0 && pos < buf.length);

				if (pos != buf.length)
					throw new IOException("End of InputStream occured at the wrong position! " + buf.length + " bytes expected, but " + pos + " bytes read!");

				return buf;
			} finally {
				if (in != null)
					in.close();
				
				if (jf != null)
					jf.close();
			}
		} catch (Exception e) {
			throw new ModuleException(e);
		}
	}
}
