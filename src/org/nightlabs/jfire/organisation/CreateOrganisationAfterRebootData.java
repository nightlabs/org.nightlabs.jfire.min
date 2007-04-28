package org.nightlabs.jfire.organisation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.nightlabs.jfire.base.JFireBaseEAR;
import org.nightlabs.jfire.servermanager.JFireServerManager;

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

	private Map<String, Descriptor> organisationID2Descriptor = null;

	private File serFile;

	public boolean isEmpty()
	{
		return organisationID2Descriptor == null || organisationID2Descriptor.isEmpty();
	}

	@SuppressWarnings("unchecked")
	public CreateOrganisationAfterRebootData(JFireServerManager jfsm)
	throws IOException
	{
		File dir = new File(jfsm.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory(), JFireBaseEAR.MODULE_NAME + ".ear");
		if (!dir.isDirectory())
			throw new IllegalStateException("The directory does not exist: " + dir.getAbsolutePath());

		serFile = new File(dir, "createOrganisationsAfterReboot.ser");
		if (serFile.exists()) {
			FileInputStream in = new FileInputStream(serFile);
			try {
				ObjectInputStream oin = new ObjectInputStream(in);
				try {
					try {
						organisationID2Descriptor = (Map<String, Descriptor>) oin.readObject();
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
		if (organisationID2Descriptor == null)
			organisationID2Descriptor = new HashMap<String, Descriptor>();

		organisationID2Descriptor.put(descriptor.organisationID, descriptor);

		writeSerFile();
	}

	private void writeSerFile()
	throws IOException
	{
		if (organisationID2Descriptor != null && organisationID2Descriptor.isEmpty())
			organisationID2Descriptor = null;

		File tmpFile = null;
		if (organisationID2Descriptor != null) {
			tmpFile = new File(serFile.getParentFile(), "createOrganisationsAfterReboot.tmp");
			FileOutputStream out = new FileOutputStream(tmpFile);
			try {
				ObjectOutputStream oout = new ObjectOutputStream(out);
				try {
					oout.writeObject(organisationID2Descriptor);
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
		if (organisationID2Descriptor == null)
			return null;

		Descriptor res = null;

		Iterator<Descriptor> it = organisationID2Descriptor.values().iterator();
		if (it.hasNext()) {
			res = it.next();
			it.remove();
		}

		writeSerFile();

		return res;
	}
}
