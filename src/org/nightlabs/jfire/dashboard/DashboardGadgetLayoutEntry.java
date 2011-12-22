package org.nightlabs.jfire.dashboard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.jdo.JDODetachedFieldAccessException;
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
 * Instances of this class are used by {@link DashboardLayoutConfigModule} to
 * configure the GridData and content of a JFireDashboard gadget. Each entry has
 * a GridData (from its superclass) and a {@link #getName() name}. Additionally
 * each entry can store a configuration of the gadget. This configuration can be
 * a POJO as it is stored in the database by serializing it.
 * 
 * @author abieber
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

	private static final long serialVersionUID = 20111221L;

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
	
	/** used to check access to config */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean configDetached;
	
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

	/**
	 * {@inheritDoc}
	 * <p>
	 * This delegates to {@link #getConfig()}
	 * </p>
	 */
	@Override
	public T getObject() {
		return getConfig();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This delegates to {@link #setConfig(Object)}
	 * </p>
	 */
	@Override
	public void setObject(T object) {
		setConfig(object);
	}
	
	/**
	 * Serialize the given config so the resulting byte[] can be stored in the database.
	 */
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

	/**
	 * Deserialize the stored config.
	 */
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
	
	/**
	 * @return The configuration object for this entry.
	 */
	public T getConfig() {
		if (JDOHelper.isDetached(this) && !configDetached)
			throw new JDODetachedFieldAccessException("config was not detached");
		return config;
	}

	/**
	 * Set the configuration object for this entry.
	 */
	public void setConfig(T config) {
		this.config = config;
	}
	
	@Override
	public void jdoPostDetach(Object persistent) {
		PersistenceManager pm = JDOHelper.getPersistenceManager(persistent);
		if (pm.getFetchPlan().getGroups().contains(AbstractEditLayoutConfigModule.FETCH_GROUP_EDIT_LAYOUT_ENTRIES)) {
			this.config = deserializeConfig(serialisedConfig);
			this.serialisedConfig = null;
			this.configDetached = true;
		} else {
			this.configDetached = false;
		}
	}

	@Override
	public void jdoPreDetach() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void jdoPostAttach(Object detached) {
		if (((DashboardGadgetLayoutEntry<T>)detached).configDetached) {
			this.serialisedConfig = serializeConfig(((DashboardGadgetLayoutEntry<T>)detached).getConfig());
			this.config = null;
		}
	}

	@Override
	public void jdoPreAttach() {
	}
	

}
