package org.nightlabs.jfire.servermanager.config;

import java.io.Serializable;

import org.nightlabs.config.InitException;

/**
 * The configuration object containing the information necessary to set up the SSL connector for
 * tomcat (jboss-web) as well as the base URLs for encrypted and non-encrypted access to the
 * servlets.
 *
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class SslCf
	extends JFireServerConfigPart
	implements Serializable
{
	private static final long serialVersionUID = 2L;

	/**
	 * Damn Sun's URLJARFile doesn't work correctly when used via an URL that is pointing to a
	 * resource inside a jar. If tried, URL.openStream() fails with a
	 * "java.util.zip.ZipException: error in opening zip file".
	 * <br>
	 * Hence, we need constant to distinguish between default keystore (inside jar) and non-default
	 * keystore (hopefully a file that is not contained in a jar).
	 */
	public static final String DEFAULT_KEYSTORE = "JFIRE_DEFAULT_KEYSTORE";
	public static final String DEFAULT_TRUSTSTORE = "JFIRE_DEFAULT_TRUSTSTORE";

	private Boolean jksStoresImported;
	private String keystoreURLToImport;
	private String keystorePassword;
	private String sslServerCertificateAlias;
	private String truststoreURLToImport;
	private String truststorePassword;
	private String sslServerCertificatePassword;

	/**
	 * If this method returns an empty String "" then the keystoreURL has already been imported and
	 * is located under <code>%jboss%/server/default/config/jfire-server.keystore</code>.
	 *
	 * @return The URL.toString() pointing to a keystore that contains the servers ssl certificate.
	 */
	public String getKeystoreURLToImport()
	{
		return keystoreURLToImport;
	}

	/**
	 * If this method returns an empty String "" then the keystoreURL has already been imported and
	 * is located under <code>%jboss%/server/default/config/jfire-server.truststore</code>.
	 *
	 * @return The URL.toString() pointing to a keystore that contains the servers ssl certificate.
	 */
	public String getTruststoreURLToImport()
	{
		return truststoreURLToImport;
	}

	/**
	 * Sets the URL of the truststore that shall be imported (copied over the {@link #DEFAULT_TRUSTSTORE})
	 * on the next initialization.
	 *
	 * @param truststoreURLToImport the URL of the truststore that will act as the new
	 * 	{@link #DEFAULT_TRUSTSTORE} after the next server initialization.
	 */
	public void setTruststoreURLToImport(String truststoreURLToImport)
	{
		this.truststoreURLToImport = truststoreURLToImport;
		setChanged();
	}

	/**
	 * Returns the default truststore's password.
	 * @return the default truststore's password.
	 */
	public String getTruststorePassword()
	{
		return truststorePassword;
	}

	/**
	 * Sets the truststore password.
	 * @param keystorePassword the password to the {@link #DEFAULT_KEYSTORE}.
	 */
	public void setTruststorePassword(String truststorePassword)
	{
		this.truststorePassword = truststorePassword;
		setChanged();
	}

	/**
	 * Sets the URL of the keystore that shall be imported (copied over the {@link #DEFAULT_KEYSTORE})
	 * on the next initialization.
	 *
	 * @param keystoreURLToImport the URL of the keystore that will act as the new
	 * 	{@link #DEFAULT_KEYSTORE} after the next server initialization.
	 */
	public void setKeystoreURLToImport(String keystoreURLToImport)
	{
		this.keystoreURLToImport = keystoreURLToImport;
		setChanged();
	}

	/**
	 * Returns the default keystore's password.
	 * @return the default keystore's password.
	 */
	public String getKeystorePassword()
	{
		return keystorePassword;
	}

	/**
	 * Sets the keystore password.
	 * @param keystorePassword the password to the {@link #DEFAULT_KEYSTORE}.
	 */
	public void setKeystorePassword(String keystorePassword)
	{
		this.keystorePassword = keystorePassword;
		setChanged();
	}

	/**
	 * @return the sslServerCertificateAlias
	 */
	public String getSslServerCertificateAlias()
	{
		return sslServerCertificateAlias;
	}

	/**
	 * @param sslServerCertificateAlias the sslServerCertificateAlias to set
	 */
	public void setSslServerCertificateAlias(String sslServerCertificateAlias)
	{
		this.sslServerCertificateAlias = sslServerCertificateAlias;
		setChanged();
	}

	/**
	 * Returns the password of the private certificate used for SSL-encryption.
	 * @return the password of the private certificate used for SSL-encryption.
	 */
	public String getSslServerCertificatePassword()
	{
		return sslServerCertificatePassword;
	}

	/**
	 * Sets the password of the private certificate used for SSL-encryption.
	 * @param sslServerCertificatePassword password of the private certificate used for SSL-encryption.
	 */
	public void setSslServerCertificatePassword(String sslServerCertificatePassword)
	{
		this.sslServerCertificatePassword = sslServerCertificatePassword;
		setChanged();
	}

	/**
	 * @return <code>true</code> iff the jks stores have been imported and {@link #setKeystoreURLImported()} has been called.
	 */
	public Boolean getJksStoresImported()
	{
		return jksStoresImported;
	}

	/**
	 * This method should be called when the file pointed to by the former
	 * {@link #keystoreURLToImport} has been copied to the {@link #DEFAULT_KEYSTORE}.
	 */
	public void setJksStoresImported(Boolean imported)
	{
		this.jksStoresImported = imported;
		setChanged();
	}

	@Override
	public void init() throws InitException
	{
		super.init();

		if (getSslServerCertificateAlias() == null)
			setSslServerCertificateAlias("localhost");

		if (getSslServerCertificatePassword() == null)
			setSslServerCertificatePassword("nightlabs");

		if (getKeystoreURLToImport() == null)
		{
			setKeystoreURLToImport(DEFAULT_KEYSTORE);
//			try
//			{
//				// Damn Sun's URLJARFile doesn't work correctly when an URL is pointing to a resource inside a jar. URL.openStream() fails with a "java.util.zip.ZipException: error in opening zip file".
//				keystoreURLToImport = JFireServerConfigModule.class.getResource("/jfire-server.keystore").toURI().toString();
//			}
//			catch (URISyntaxException e)
//			{
//				System.err.println(e.getMessage());
//				throw new RuntimeException(e);
//			}
		}
		if (getKeystorePassword() == null)
			setKeystorePassword("nightlabs");

		if (getTruststoreURLToImport() == null)
		{
			setTruststoreURLToImport(DEFAULT_TRUSTSTORE);
		}
		if (getTruststorePassword() == null)
			setTruststorePassword("nightlabs");
		
		if (jksStoresImported == null)
			jksStoresImported = Boolean.FALSE;
	}
}