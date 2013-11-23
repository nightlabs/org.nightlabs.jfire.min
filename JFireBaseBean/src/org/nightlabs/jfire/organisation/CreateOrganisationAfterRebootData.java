package org.nightlabs.jfire.organisation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;

import org.nightlabs.jfire.server.data.dir.JFireServerDataDirectory;

/**
 * @author marco schulze - marco at nightlabs dot de
 */
public class CreateOrganisationAfterRebootData
{
	public static class Descriptor
	implements Serializable
	{
		private static final long serialVersionUID = 1L;

		public String organisationID;
		public String organisationDisplayName;
		public String userID;
		public String password;
		public boolean isServerAdmin;
	}

	private LinkedList<Descriptor> descriptors = null;

	private File serFile;

	public boolean isEmpty()
	{
		return descriptors == null || descriptors.isEmpty();
	}

	@SuppressWarnings("unchecked")
	public CreateOrganisationAfterRebootData() // File j2eeDeployBaseDirectory)
	throws IOException
	{
//		File dir = new File(new File(j2eeDeployBaseDirectory, JFireBaseEAR.MODULE_NAME + ".ear"), "config");
		File dir = JFireServerDataDirectory.getJFireServerDataDirFile();
		if (!dir.isDirectory())
			throw new IllegalStateException("The directory does not exist: " + dir.getAbsolutePath());

		serFile = new File(dir, "createOrganisationsAfterReboot.ser");
		if (serFile.exists()) {
			FileInputStream in = new FileInputStream(serFile);
			try {
				ObjectInputStream oin = new ObjectInputStream(in);
				try {
					try {
						descriptors = (LinkedList<Descriptor>) oin.readObject();
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(e); // this should really never happen
					}
				} finally {
					oin.close();
				}
			} finally {
				in.close();
			}
		}
	}

//	public CreateOrganisationAfterRebootData(JFireServerManager jfsm)
//	throws IOException
//	{
//		this(new File(jfsm.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory()));
//	}

	public void addOrganisation(String organisationID, String organisationDisplayName, String userID, String password, boolean isServerAdmin) throws IOException
	{
		Descriptor d = new Descriptor();
		d.organisationID = organisationID;
		d.organisationDisplayName = organisationDisplayName;
		d.userID = userID;
		d.password = password;
		d.isServerAdmin = isServerAdmin;

		addOrganisation(d);
	}

	public void addOrganisation(Descriptor descriptor) throws IOException
	{
		if (descriptors == null)
			descriptors = new LinkedList<Descriptor>();

		descriptors.add(descriptor);

		writeSerFile();
	}

	private void writeSerFile()
	throws IOException
	{
		if (descriptors != null && descriptors.isEmpty())
			descriptors = null;

		File tmpFile = null;
		if (descriptors != null) {
			tmpFile = new File(serFile.getParentFile(), "createOrganisationsAfterReboot.tmp");
			FileOutputStream out = new FileOutputStream(tmpFile);
			try {
				ObjectOutputStream oout = new ObjectOutputStream(out);
				try {
					oout.writeObject(descriptors);
				} finally {
					oout.close();
				}
			} finally {
				out.close();
			}
		}

		if (serFile.exists())
			serFile.delete();

		if (tmpFile != null) {
			if (!tmpFile.renameTo(serFile))
				throw new IOException("Could not rename tmp file to real file! tmpFile=\"" + tmpFile.getAbsolutePath() +
						"\" realFile=\"" + serFile.getAbsolutePath() + "\"");
		}
	}

	public Descriptor fetchOrganisationCreationDescriptor()
	throws IOException
	{
		if (descriptors == null)
			return null;

		Descriptor res = null;

		if (!descriptors.isEmpty())
			res = descriptors.removeFirst();

		writeSerFile();

		return res;
	}
}
