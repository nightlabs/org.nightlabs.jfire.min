package org.nightlabs.jfire.servermanager.config;

import org.nightlabs.config.InitException;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Khaled Soliman
 */
public class SmtpMailServiceCf extends JFireServerConfigPart
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	public static final String ENCRYPTION_METHOD_NONE = "none";
	public static final String ENCRYPTION_METHOD_SSL = "ssl";
	public static final String ENCRYPTION_METHOD_TLS = "tls";
	
	private Boolean debug = null;
	private String host = null;
	private Integer port = null;
	private String mailFrom = null;
	private Boolean useAuthentication = null;
	private String username = null;
	private String password = null;
	private String encryptionMethod = null;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.servermanager.config.JFireServerConfigPart#init()
	 */
	@Override
	public void init() throws InitException {
		super.init();
		if(debug == null)
			debug = false;
		if(host == null)
			host = "127.0.0.1";
		if(encryptionMethod == null)
			encryptionMethod = ENCRYPTION_METHOD_NONE;
		if(port == null) {
			if(ENCRYPTION_METHOD_SSL.equals(encryptionMethod))
				port = 443;
			else
				port = 25;
		}
		if(mailFrom == null)
			mailFrom = "nobody@nosuchhost.nosuchdomain.com";
		if(useAuthentication == null)
			useAuthentication = false;
		if(useAuthentication) {
			if(username == null)
				username = "nobody";
			if(password == null)
				password = "nobody";
		}
	}

	/**
	 * Get the debug.
	 * @return the debug
	 */
	public Boolean getDebug() {
		return debug;
	}

	/**
	 * Set the debug.
	 * @param debug the debug to set
	 */
	public void setDebug(Boolean debug) {
		this.debug = debug;
		setChanged();
	}

	/**
	 * Get the encryptionMethod.
	 * @return the encryptionMethod
	 */
	public String getEncryptionMethod() {
		return encryptionMethod;
	}

	/**
	 * Set the encryptionMethod.
	 * @param encryptionMethod the encryptionMethod to set
	 */
	public void setEncryptionMethod(String encryptionMethod) {
		this.encryptionMethod = encryptionMethod;
		setChanged();
	}

	/**
	 * Get the host.
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Set the host.
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
		setChanged();
	}

	/**
	 * Get the mailFrom.
	 * @return the mailFrom
	 */
	public String getMailFrom() {
		return mailFrom;
	}

	/**
	 * Set the mailFrom.
	 * @param mailFrom the mailFrom to set
	 */
	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
		setChanged();
	}

	/**
	 * Get the password.
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set the password.
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
		setChanged();
	}

	/**
	 * Get the port.
	 * @return the port
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * Set the port.
	 * @param port the port to set
	 */
	public void setPort(Integer port) {
		this.port = port;
		setChanged();
	}

	/**
	 * Get the useAuthentication.
	 * @return the useAuthentication
	 */
	public Boolean getUseAuthentication() {
		return useAuthentication;
	}

	/**
	 * Set the useAuthentication.
	 * @param useAuthentication the useAuthentication to set
	 */
	public void setUseAuthentication(Boolean useAuthentication) {
		this.useAuthentication = useAuthentication;
		setChanged();
	}

	/**
	 * Get the username.
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set the username.
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
		setChanged();
	}
}
