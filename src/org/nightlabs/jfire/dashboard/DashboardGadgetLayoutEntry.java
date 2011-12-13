/**
 * 
 */
package org.nightlabs.jfire.dashboard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;
import org.nightlabs.jfire.layout.AbstractEditLayoutEntry;

import com.thoughtworks.xstream.XStream;

/**
 * @author abieber
 *
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDashboard_DashboardGadgetLayoutEntry")
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
@FetchGroup(
		name = AbstractEditLayoutConfigModule.FETCH_GROUP_EDIT_LAYOUT_ENTRIES, 
		members = {
				@Persistent(name = "serialisedConfig"), 
				@Persistent(name = "name")}
)
public class DashboardGadgetLayoutEntry<T> extends AbstractEditLayoutEntry<T> implements AttachCallback, DetachCallback {

	private static final long serialVersionUID = 20111212L;

	@Persistent(mappedBy="entry", dependent="true")
	private DashboardGadgetLayoutEntryName name;

	/**
	 * This is the serialized actual config for this entry. (Read in detach-callback and set in attach-callback)
	 */
	@Persistent(defaultFetchGroup = "true", persistenceModifier = PersistenceModifier.PERSISTENT)
	@Column(sqlType = "BLOB")
	private byte[] serialisedConfig;

	/**
	 * This is transfered to the client (set in detach-callback) but not stored in datastore (serialized in attach-callback)
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private T config;
	
	/**
	 * @param configModule
	 * @param entryID
	 * @param entryType
	 */
	public DashboardGadgetLayoutEntry(
			AbstractEditLayoutConfigModule<T, ?> configModule,
			long entryID, String entryType) {
		super(configModule, entryID, entryType);
		name = new DashboardGadgetLayoutEntryName(this);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.layout.AbstractEditLayoutEntry#getName()
	 */
	@Override
	public String getName() {
		return name.getText();
	}
	
	public DashboardGadgetLayoutEntryName getEntryName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.layout.AbstractEditLayoutEntry#getObject()
	 */
	@Override
	public T getObject() {
		return config;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.layout.AbstractEditLayoutEntry#setObject(java.lang.Object)
	 */
	@Override
	public void setObject(T object) {
		this.config = object;
	}
	
	protected byte[] serializeConfig(T config) {
		if (config == null)
			return null;
		final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] result = null;
		DeflaterOutputStream zipStream = new DeflaterOutputStream(outStream);
		try {
			XStream xStream = new XStream();
			xStream.toXML(config, zipStream);
			zipStream.close();
			result = outStream.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	protected T deserializeConfig(byte[] serialisedConfig) {
		if (serialisedConfig == null)
			return null;
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(serialisedConfig);
		final InflaterInputStream zipStream = new InflaterInputStream(inputStream);
		T result = null;
		try {
			XStream xStream = new XStream();
			result = (T) xStream.fromXML(zipStream);
			zipStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
	public T getConfig() {
		return config;
	}
	
	public void setConfig(T config) {
		this.config = config;
	}
	
	@Override
	public void jdoPostDetach(Object persistent) {
		PersistenceManager pm = JDOHelper.getPersistenceManager(persistent);
		if (pm.getFetchPlan().getGroups().contains(AbstractEditLayoutConfigModule.FETCH_GROUP_EDIT_LAYOUT_ENTRIES)) {
			this.config = deserializeConfig(serialisedConfig);
			this.serialisedConfig = null;
		}
	}

	@Override
	public void jdoPreDetach() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void jdoPostAttach(Object detached) {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm.getFetchPlan().getGroups().contains(AbstractEditLayoutConfigModule.FETCH_GROUP_EDIT_LAYOUT_ENTRIES)) {
			this.serialisedConfig = serializeConfig(((DashboardGadgetLayoutEntry<T>)detached).getObject());
			this.config = null;
		}
	}

	@Override
	public void jdoPreAttach() {
	}
	

}
