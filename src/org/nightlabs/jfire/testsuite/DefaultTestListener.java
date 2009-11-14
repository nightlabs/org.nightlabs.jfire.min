package org.nightlabs.jfire.testsuite;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.testsuite.TestSuite.Status;
import org.nightlabs.jfire.testsuite.internal.TestCaseResult;
import org.nightlabs.jfire.testsuite.internal.TestResult;
import org.nightlabs.jfire.testsuite.internal.TestSuiteResult;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.Util;
import org.nightlabs.xml.NLDOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The default {@link JFireTestListener} gathers all {@link Test} data
 * and is capable of generating a XML file from the results and
 * also send the test results as email.
 * <p>
 * The listener accepts the following configuration properties (file references are relative to the ear directory)
 * <ul>
 *   <li>report.enabled: Whether to create report files</li>
 *   <li>report.todir: The directory to store the report files in (default: a temporary directory)</li>
 *   <li>report.filenameprefix: A file name prefix for generated report files (default: a random prefix)</li>
 *   <li>report.stylesheets: A comma separated list of XSLT stylesheet identifiers</li>
 *   <li>report.stylesheet.&lt;identifier&gt;.location: location of XSLT stylesheet</li>
 *   <li>report.stylesheet.&lt;identifier&gt;.filesuffix: The report file suffix (default: ".xml")
 *   <li>mail.alwaysSend.enabled: Whether an email notification should be send for every test run.</li>
 *   <li>mail.onFailure.enabled: Whether an email notification should be send, when at least one suite fails.</li>
 *   <li>mail.onSkip.enabled: Whether an email notification should be send, when at least one suite is skipped.</li>
 *   <li>mail.smtp.host: The SMTP host to send mail with.</li>
 *   <li>mail.from: The sender of the mail, default info@jfire.org</li>
 *   <li>mail.to: The recipients of the mail (,-separated list).</li>
 *   <li>mail.subject: The mail subject, default "JFireTestSuite Testreport"</li>
 *   <li>mail.stylesheet: The XSLT stylesheet identifier to render the xml to the html mail body. If not defined or empty, the internal default resource "htmlReport.xsl" will be used.</li>
 * </ul>
 * <p>
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author marco schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DefaultTestListener
implements JFireTestListener
{
	protected static final String INTERNAL_LOCATION_PREFIX = "internal:";
	protected static final String PROPERTY_KEY_SMTP_HOST = "mail.smtp.host";
	protected static final String PROPERTY_KEY_MAIL_TO = "mail.to";
	protected static final String PROPERTY_KEY_MAIL_FROM = "mail.from";
	protected static final String PROPERTY_KEY_MAIL_SUBJECT = "mail.subject";

	protected static final String PROPERTY_KEY_SMTP_AUTH = "mail.smtp.auth";
	protected static final String PROPERTY_KEY_SMTP_USER = "mail.smtp.user";

	protected static final String PROPERTY_KEY_MAIL_STYLESHEET = "mail.stylesheet";
	protected static final String DEFAULT_MAIL_STYLESHEET_LOCATION = "internal:htmlReport.xsl";
	
	/**
	 * The password to be used for authentication. Note that this is not understood by
	 * the java mail API as property, but needs to be handled manually (see code below).
	 */
	protected static final String PROPERTY_KEY_SMTP_PASSWORD = "mail.smtp.password";

	protected static final String PROPERTY_KEY_REPORT_ENABLED = "report.enabled";
	protected static final String PROPERTY_KEY_REPORT_TODIR = "report.todir";
	protected static final String PROPERTY_KEY_REPORT_FILENAME_PREFIX = "report.filenameprefix";
	protected static final String PROPERTY_KEY_STYLESHEETS = "report.stylesheets";
	protected static final String PROPERTY_KEY_STYLESHEET_PREFIX = "report.stylesheet.";
	protected static final String PROPERTY_KEY_STYLESHEET_LOCATION_SUFFIX = ".location";
	protected static final String PROPERTY_KEY_STYLESHEET_FILESUFFIX_SUFFIX = ".filesuffix";
	
	/**
	 * Log4J Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(DefaultTestListener.class);

	/**
	 * Date format used for output.
	 */
	private static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	/**
	 * The gathered results.
	 */
	private List<TestSuiteResult> testSuiteResults;

	/**
	 * Used while running.
	 */
	private Date startTime;
	/**
	 * Used while running.
	 */
	private Date endTime;
	/**
	 * Used while running.
	 */
	private TestSuiteResult currSuiteResult;
	/**
	 * Used while running.
	 */
	private TestCaseResult currTestCaseResult;
	/**
	 * Used while running.
	 */
	private TestResult currTestResult;

	/**
	 * The configuration of this listener.
	 */
	private Properties config = null;

	private final String organisationID;

	private File reportDir = null;
	private Document reportDocument = null;

	/**
	 * Default constructor used by the framework to instantiate {@link JFireTestListener}s.
	 */
	public DefaultTestListener() {
		organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.testsuite.JFireTestListener#configure(java.util.Properties)
	 */
	@Override
	public void configure(final Properties config) 
	{
		this.config = config;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.testsuite.JFireTestListener#endTestRun()
	 */
	@Override
	public void endTestRun() throws Exception {
		endTime = new Date();
		processTestRun();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.testsuite.JFireTestListener#startTestRun()
	 */
	@Override
	public void startTestRun() throws Exception {
		testSuiteResults = new LinkedList<TestSuiteResult>();
		startTime = new Date();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.testsuite.JFireTestListener#testSuiteStatus(org.nightlabs.jfire.testsuite.TestSuite, org.nightlabs.jfire.testsuite.TestSuite.Status)
	 */
	@Override
	public void testSuiteStatus(final TestSuite suite, final Status status) throws Exception {
		if (status == Status.START || status == Status.SKIP) {
			TestSuiteResult suiteResult = new TestSuiteResult();
			suiteResult.setStartTime(new Date());
			suiteResult.setStatus(status);
			if (status == Status.SKIP) {
				suiteResult.setEndTime(new Date());
			}
			testSuiteResults.add(suiteResult);
			suiteResult.setSuite(suite);
			currSuiteResult = suiteResult;
		} else if (status == Status.END) {
			if (currSuiteResult == null)
				return;
			currSuiteResult.setEndTime(new Date());
		}

	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.testsuite.JFireTestListener#addSuiteStartError(org.nightlabs.jfire.testsuite.TestSuite, java.lang.Throwable)
	 */
	@Override
	public void addSuiteStartError(final TestSuite suite, final Throwable t) {
		if (currSuiteResult != null) {
			currSuiteResult.setSuiteStartError(t);
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestListener#addError(junit.framework.Test, java.lang.Throwable)
	 */
	@Override
	public void addError(final Test test, final Throwable t) {
		if (currTestResult == null)
			return;
		currTestResult.setSuccess(false);
		currTestResult.setError(t);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestListener#addFailure(junit.framework.Test, junit.framework.AssertionFailedError)
	 */
	@Override
	public void addFailure(final Test test, final AssertionFailedError t) {
		if (currTestResult == null)
			return;
		currTestResult.setSuccess(false);
		currTestResult.setError(t);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestListener#endTest(junit.framework.Test)
	 */
	@Override
	public void endTest(final Test test) {
		if (currTestResult == null)
			return;
		if (currTestCaseResult != null)
			currTestCaseResult.setEndTime(new Date());
		currTestResult.setEndTime(new Date());
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestListener#startTest(junit.framework.Test)
	 */
	@Override
	public void startTest(final Test test) {
		if (currTestCaseResult == null || !currTestCaseResult.getTestCaseClass().equals(test.getClass())) {
			if (currTestCaseResult != null) {
				currTestCaseResult.setEndTime(new Date());
			}
			currTestCaseResult = new TestCaseResult();
			currTestCaseResult.setTestSuiteResult(currSuiteResult);
			currSuiteResult.getTestCaseResults().add(currTestCaseResult);
			currTestCaseResult.setTestCaseClass(test.getClass());
			currTestCaseResult.setStartTime(new Date());
		}
		if (currTestCaseResult == null)
			return;
		currTestResult = new TestResult();
		currTestResult.setTestCaseResult(currTestCaseResult);
		currTestCaseResult.getTestResults().add(currTestResult);
		currTestResult.setStartTime(new Date());
		String name = test.toString();
		String testName = name.substring(0, name.indexOf('('));
		currTestResult.setTestName(testName);
	}


	protected Document getReportDocument() throws ParserConfigurationException 
	{
		if(reportDocument == null)
			reportDocument = createReportDocument();
		return reportDocument;
	}
	
	private Document createReportDocument() throws ParserConfigurationException 
	{
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element rootNode = doc.createElement("JFireServerTestResult");
		rootNode.setAttribute("startTime", ISO_DATE_FORMAT.format(startTime));
		rootNode.setAttribute("endTime", ISO_DATE_FORMAT.format(endTime));
		rootNode.setAttribute("organisationID", organisationID);
		for (TestSuiteResult suiteResult : testSuiteResults) {
			Element suiteElement = doc.createElement("TestSuiteResult");
			suiteElement.setAttribute("suiteClass", suiteResult.getSuite().getClass().getName());
			suiteElement.setAttribute("suiteName", suiteResult.getSuite().getName() != null ? suiteResult.getSuite().getName() : suiteResult.getSuite().getClass().getSimpleName());
			suiteElement.setAttribute("suiteStatus", suiteResult.getStatus().toString());
			suiteElement.setAttribute("startTime", ISO_DATE_FORMAT.format(suiteResult.getStartTime()));
			suiteElement.setAttribute("endTime", ISO_DATE_FORMAT.format(suiteResult.getEndTime()));
			suiteElement.setAttribute("executionTime", Long.toString(suiteResult.getEndTime().getTime() - suiteResult.getStartTime().getTime()));
			suiteElement.setAttribute("hasFailures", Boolean.toString(suiteResult.isHasFailures()));
			if (suiteResult.getSuiteStartError() != null) {
				Element exceptionElement = doc.createElement("TestSuiteResultDetail");
				exceptionElement.setAttribute("message", suiteResult.getSuiteStartError().getLocalizedMessage());
				Node stackTrace = doc.createCDATASection(Util.getStackTraceAsString(suiteResult.getSuiteStartError()));
				exceptionElement.appendChild(stackTrace);
				suiteElement.appendChild(exceptionElement);
			}
			rootNode.appendChild(suiteElement);
			for (TestCaseResult caseResult : suiteResult.getTestCaseResults()) {
				Element caseElement = doc.createElement("TestCaseResult");
				caseElement.setAttribute("testCaseClass", caseResult.getTestCaseClass().getName());
				caseElement.setAttribute("startTime", ISO_DATE_FORMAT.format(caseResult.getStartTime()));
				caseElement.setAttribute("endTime", ISO_DATE_FORMAT.format(caseResult.getEndTime()));
				caseElement.setAttribute("executionTime", Long.toString(caseResult.getEndTime().getTime() - caseResult.getStartTime().getTime()));
				caseElement.setAttribute("hasFailures", Boolean.toString(caseResult.isHasFailures()));
				suiteElement.appendChild(caseElement);
				for (TestResult testResult : caseResult.getTestResults()) {
					Element testElement = doc.createElement("TestResult");
					testElement.setAttribute("testName", testResult.getTestName());
					testElement.setAttribute("startTime", ISO_DATE_FORMAT.format(testResult.getStartTime()));
					testElement.setAttribute("endTime", ISO_DATE_FORMAT.format(testResult.getEndTime()));
					testElement.setAttribute("executionTime", Long.toString(testResult.getEndTime().getTime() - testResult.getStartTime().getTime()));
					testElement.setAttribute("success", Boolean.toString(testResult.isSuccess()));
					if (testResult.getError() != null) {
						Element exceptionElement = doc.createElement("TestResultDetail");
						exceptionElement.setAttribute("message", testResult.getError().getLocalizedMessage());
						Node stackTrace = doc.createCDATASection(Util.getStackTraceAsString(testResult.getError()));
						exceptionElement.appendChild(stackTrace);
						testElement.appendChild(exceptionElement);
					}
					caseElement.appendChild(testElement);
				}
			}
		}
		doc.appendChild(rootNode);
		return doc;
	}
	
	private static File createTempDir() throws IOException
	{
		File tmpDir = IOUtil.getUserTempDir("JFireTestSuite.", null);
		if (!tmpDir.isDirectory()) {
			boolean success = tmpDir.mkdirs();
			if(!success)
				throw new IOException("Create directory failed: "+tmpDir.getAbsolutePath());
		}
		return tmpDir;
	}

	private String getTempFilePrefix()
	{
		return Long.toString(System.currentTimeMillis(), 36) + '-' + Long.toString(System.identityHashCode(this)) + '-';
	}

	private File getReportDir() throws IOException
	{
		if(reportDir == null) {
			String dirString = getProperty(PROPERTY_KEY_REPORT_TODIR, null);
			if(dirString == null || dirString.isEmpty()) {
				reportDir = createTempDir();
			} else {
				File dir = new File(dirString);
				if(!dir.isDirectory()) {
					boolean success = dir.mkdirs();
					if(!success)
						throw new IOException("Create directory failed: "+dirString);
				}
				reportDir = dir;
			}
		}
		return reportDir;
	}

	protected void applyStylesheetById(final String stylesheet, final OutputStream out) throws IOException, TransformerException, ParserConfigurationException
	{
		String stylesheetLocation = getProperty(PROPERTY_KEY_STYLESHEET_PREFIX + stylesheet + PROPERTY_KEY_STYLESHEET_LOCATION_SUFFIX, null);
		if(stylesheetLocation == null || stylesheetLocation.isEmpty()) {
			throw new RuntimeException("Undefined stylesheet location for stylesheet '"+stylesheet+"'");
		}
		applyStylesheetByLocation(stylesheetLocation, out);
	}

	private void applyStylesheetByLocation(final String stylesheetLocation, final OutputStream out) throws IOException, TransformerException, ParserConfigurationException 
	{
		InputStream in;
		if(stylesheetLocation.startsWith(INTERNAL_LOCATION_PREFIX)) {
			in = getClass().getResourceAsStream(stylesheetLocation.substring(INTERNAL_LOCATION_PREFIX.length()));
			if(in == null)
				throw new IOException("Internal stylesheet not found: "+stylesheetLocation);
		} else {
			in = new FileInputStream(stylesheetLocation);
		}

		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer(new StreamSource(in));
			transformer.transform(new DOMSource(getReportDocument()), new StreamResult(out));
		} finally {
			in.close();
		}
	}
	
	private void createReportFiles() throws IOException, TransformerException, ParserConfigurationException
	{
		String filePrefix = getProperty(PROPERTY_KEY_REPORT_FILENAME_PREFIX, getTempFilePrefix());
		File defaultOutputFile = new File(getReportDir(), filePrefix + "report.xml");
		logger.info("Creating default test report file: "+defaultOutputFile.getAbsolutePath());
		FileOutputStream defaultOut = new FileOutputStream(defaultOutputFile);
		try {
			NLDOMUtil.writeDocument(getReportDocument(), defaultOut, "UTF-8");
		} finally {
			defaultOut.close();
		}
		
		List<String> stylesheets = getStylesheets();
		for (String stylesheet : stylesheets) {
			String fileSuffix = getProperty(PROPERTY_KEY_STYLESHEET_PREFIX + stylesheet + PROPERTY_KEY_STYLESHEET_FILESUFFIX_SUFFIX, ".xml");
			
			File outputFile = new File(getReportDir(), filePrefix + stylesheet + "-report" + fileSuffix);
			logger.info("Creating "+stylesheet+" test report file: "+outputFile.getAbsolutePath());
			FileOutputStream out = new FileOutputStream(outputFile);
			try {
				applyStylesheetById(stylesheet, out);
			} finally {
				out.close();
			}
		}
	}
	
	private List<String> getStylesheets()
	{
		String stylesheets = getProperty(PROPERTY_KEY_STYLESHEETS, null);
		if(stylesheets == null || stylesheets.isEmpty())
			return Collections.emptyList();
		List<String> stylesheetList = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(stylesheets, ",");
		while(st.hasMoreTokens()) {
			String stylesheet = st.nextToken().trim();
			if(!stylesheet.isEmpty())
				stylesheetList.add(stylesheet);
		}		
		return stylesheetList;
	}

	/**
	 * According to the contract of {@link JFireTestListener}, an instance is created for each test run.
	 * Therefore, we assert it really is this way and we have no cached data yet.
	 */
	private void assertClearCache()
	{
		if (reportDir != null)
			throw new IllegalStateException("reportDir != null");
		if (reportDocument != null)
			throw new IllegalStateException("reportDocument != null");
	}

	/**
	 * Checks whether the results should be written to XML and does so if desired.
	 * Also checks whether the results should be send by email and does so.
	 */
	private void processTestRun() throws IOException, TransformerException, ParserConfigurationException
	{
		assertClearCache();

		boolean reportEnabled = getProperty(PROPERTY_KEY_REPORT_ENABLED, true);
		if(reportEnabled)
			createReportFiles();
		
		boolean sendMailOnFailure = getProperty("mail.onFailure.enabled", false);
		boolean sendMailOnSuccess = getProperty("mail.alwaysSend.enabled", false);
		boolean sendMailOnSkip = getProperty("mail.onSkip.enabled", false);
		boolean failure = false;
		boolean skipped = false;
		for (TestSuiteResult suiteResult : testSuiteResults) {
			if (suiteResult.isHasFailures()) {
				failure = true;
			} else if (suiteResult.getStatus() == Status.SKIP) {
				skipped = true;
			}
		}
		boolean sendMail = sendMailOnSuccess || (failure && sendMailOnFailure) || (skipped && sendMailOnSkip);
		if (sendMail) {
			try {
				sendReportAsMail();
			} catch (Exception e) {
				logger.error("Failed sending notification email!", e);
			}
		}
	}

	/**
	 * Get the listeners boolean property with the given key.
	 * Return the default value if not set.
	 *
	 * @param key The key of the property.
	 * @param def The properties default value.
	 */
	protected boolean getProperty(final String key, final boolean def)
	{
		if(config == null)
			throw new IllegalStateException("Test listener is not configured");
		boolean result = def;
		String str = config.getProperty(key);
		if (str != null && !"".equals(str)) {
			try {
				result = Boolean.parseBoolean(str);
			} catch (Exception e) {
				logger.error("Wrong boolean property for " + key + " = " + str);
				result = def;
			}
		}
		return result;
	}

	/**
	 * Get the listeners String property with the given key.
	 * Return the default value if not set.
	 *
	 * @param key The key of the property.
	 * @param def The properties default value.
	 */
	protected String getProperty(final String key, final String def)
	{
		if(config == null)
			throw new IllegalStateException("Test listener is not configured");
		String result = def;
		String str = config.getProperty(key);
		if (str != null) {
			result = str;
		}
		return result;
	}

	/**
	 * Sends the gathered results as email.
	 * Sender, recipient etc. is configured in the listener properties.
	 */
	private void sendReportAsMail() throws ParserConfigurationException, IOException, TransformerException, MessagingException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String mailStylesheet = getProperty(PROPERTY_KEY_MAIL_STYLESHEET, null);
		if(mailStylesheet == null || mailStylesheet.isEmpty()) {
			String location = getProperty(PROPERTY_KEY_MAIL_STYLESHEET, DEFAULT_MAIL_STYLESHEET_LOCATION);
			applyStylesheetByLocation(location, out);
		} else {
			applyStylesheetById(mailStylesheet, out);
		}
		
		byte[] xml = NLDOMUtil.getDocumentAsByteArray(getReportDocument(), "UTF-8");
		String html = out.toString("UTF-8");
		
		
		String smtpHost = getProperty(PROPERTY_KEY_SMTP_HOST, null);
		String mailFrom = getProperty(PROPERTY_KEY_MAIL_FROM, "info@jfire.org");
		String mailTo = getProperty(PROPERTY_KEY_MAIL_TO, "info@jfire.org");
		
		if (smtpHost == null || smtpHost.isEmpty())
			throw new IllegalStateException("There is no SMTP host defined! Check your jfireTestSuite.properties (key suffix " + PROPERTY_KEY_SMTP_HOST + ") and your included properties-files!");

		// create/send the message
		Session session = Session.getInstance(config, null);
		MimeMessage message = new MimeMessage(session);

		message.setFrom(new InternetAddress(mailFrom));

		if(mailTo.contains(",")) {
			StringTokenizer t = new StringTokenizer(mailTo, ",");
			while(t.hasMoreTokens())
				message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(t.nextToken().trim()));
		} else {
			message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(mailTo));
		}

		String subject = getProperty(PROPERTY_KEY_MAIL_SUBJECT, "JFireTestSuite Testreport");
		message.setSubject(subject);

		//		message.setContent(writer.toString(), "text/html");

		// set html report
		MimeBodyPart mimebodypart = new MimeBodyPart();
		mimebodypart.setContent(html, "text/html");

		// attach xml report
		MimeMultipart mimemultipart = new MimeMultipart();
		mimemultipart.addBodyPart(mimebodypart);
		mimebodypart = new MimeBodyPart();
		DataSource filedatasource = new ByteArrayDataSource(xml, "text/xml");
		mimebodypart.setDataHandler(new DataHandler(filedatasource));

		mimebodypart.setFileName("jfire-test-report.xml");
		mimemultipart.addBodyPart(mimebodypart);
		message.setContent(mimemultipart);
		message.saveChanges(); // according to the docs, this should not be forgotten - but it worked without, too - nevertheless it doesn't hurt to use it.

		boolean authenticate = getProperty(PROPERTY_KEY_SMTP_AUTH, false);

		if (!authenticate) {
			logger.info("sendReportAsMail: Sending TestSuite report email without authentication via SMTP host " + smtpHost + " to: "+mailTo);
			Transport.send(message); // use simple API since this is tested well and seems to work fine.
		}
		else {
			logger.info("sendReportAsMail: Sending TestSuite report email with authentication via SMTP host " + smtpHost + " to: "+mailTo);

			String smtpUsername = config.getProperty(PROPERTY_KEY_SMTP_USER);
			String smtpPassword = config.getProperty(PROPERTY_KEY_SMTP_PASSWORD);

			if (smtpUsername == null || "".equals(smtpUsername))
				logger.warn("sendReportAsMail: property " + PROPERTY_KEY_SMTP_AUTH + " has been set to 'true', but there is no user name defined! You should add a user name using the property "+ PROPERTY_KEY_SMTP_USER +"!");

			if (smtpPassword == null || "".equals(smtpPassword))
				logger.warn("sendReportAsMail: property " + PROPERTY_KEY_SMTP_AUTH + " has been set to 'true', but there is no password defined! You should add a password using the property "+ PROPERTY_KEY_SMTP_PASSWORD +"!");

			if (logger.isTraceEnabled()) {
				logger.trace("sendReportAsMail: properties:");
				for (Map.Entry<?, ?> me : config.entrySet())
					logger.trace("sendReportAsMail:   * " + me.getKey() + '=' + me.getValue());
			}

			Transport tr = session.getTransport("smtp");
			try {
				tr.connect(smtpHost, smtpUsername, smtpPassword);
				tr.sendMessage(message, message.getAllRecipients());
			} finally {
				tr.close();
			}
		}
	}
}
