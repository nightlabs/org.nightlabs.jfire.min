package org.nightlabs.jfire.testsuite.language;

import java.util.Collection;
import java.util.Locale;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.language.Language;
import org.nightlabs.jfire.language.LanguageManagerRemote;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.testsuite.JFireTestSuite;
import org.nightlabs.language.LanguageCf;

/**
*
* @author Fitas Amine - fitas [at] nightlabs [dot] de
*
*/
@JFireTestSuite(JFireBaseLanguageTestSuite.class)
public class LanguageTestCase extends TestCase{
	
	Logger logger = Logger.getLogger(LanguageTestCase.class);	

	@Test
	public void testLanguage() throws Exception{	
		LanguageCf newLanguageCf = new LanguageCf("epo");
		newLanguageCf.init(null);
		newLanguageCf.setNativeName("Esperanto");
		newLanguageCf.getName().setText(Locale.ENGLISH.getLanguage(),"Esperanto");
		LanguageManagerRemote  lm = JFireEjb3Factory.getRemoteBean(LanguageManagerRemote.class, SecurityReflector.getInitialContextProperties());
		// creates the new language and if it exists it does nothing
		Language newLanguage = lm.createLanguage(newLanguageCf, true, false);	
		Collection<Language> languages = lm.getLanguages();		
		// List all the languages available in the system.
		logger.info("The following Languages was found on the system.");
		for (Language lang : languages) {
			logger.info("Language = "+ lang.getName().getText());
		}			
	}
}
