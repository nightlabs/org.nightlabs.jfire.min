package org.nightlabs.jfire.testsuite.language;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.nightlabs.jfire.testsuite.hamcrest.IsNotEmptyMatcher.isNotEmpty;
import static org.nightlabs.jfire.testsuite.hamcrest.IsNotNullMatcher.isNotNull;

import java.util.Collection;
import java.util.Locale;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.language.Language;
import org.nightlabs.jfire.language.LanguageConfig;
import org.nightlabs.jfire.language.LanguageManagerRemote;
import org.nightlabs.jfire.language.LanguageSyncMode;
import org.nightlabs.jfire.language.id.LanguageID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.testsuite.JFireTestSuite;
import org.nightlabs.jfire.testsuite.TestCase;
import org.nightlabs.language.LanguageCf;

/**
 *
 * a simple test case to test the API of the Language Manager Bean
 * 
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 *
 */
@JFireTestSuite(JFireBaseLanguageTestSuite.class)
public class LanguageTestCase extends TestCase{

	Logger logger = Logger.getLogger(LanguageTestCase.class);	
	private static String NEW_LANGUAGE = "NEWLANGUAGE";


	@Test
	public void testAddNewLanguage() throws Exception{	
		LanguageManagerRemote  lm = JFireEjb3Factory.getRemoteBean(LanguageManagerRemote.class, SecurityReflector.getInitialContextProperties());	
		LanguageCf newLanguageCf = new LanguageCf("mak");
		newLanguageCf.init(null);
		newLanguageCf.setNativeName("Makasar");
		newLanguageCf.getName().setText(Locale.ENGLISH.getLanguage(),"Makasar");
		// creates the new language and if it exists it does nothing
		Language newLang = lm.createLanguage(newLanguageCf, false, true);	
		assertThat(newLang,notNullValue());
		assertThat(newLang.getLanguageID(),equalTo(newLanguageCf.getLanguageID()));
		Collection<Language> languages = lm.getLanguages();		
		assertThat(languages,both(isNotNull()).and(isNotEmpty())); 
		// List all the languages available in the system.
		boolean found = false;
		for (Language lang : languages) 
		{
			if(lang.getLanguageID().equals(newLang.getLanguageID()))
				found = true;
		}
		assertThat(found,equalTo(true));
		setTestCaseContextObject(NEW_LANGUAGE, (LanguageID)JDOHelper.getObjectId(newLang));
		// calls the method with get set to false
		newLang = lm.createLanguage(newLanguageCf, false, false);	
		assertThat(newLang,nullValue());
	}	

	@Test
	public void testLanguageConfig() throws Exception{	

		LanguageManagerRemote  lm = JFireEjb3Factory.getRemoteBean(LanguageManagerRemote.class, SecurityReflector.getInitialContextProperties());

		// Get language manager, configuration and current sync mode.
		lm = JFireEjb3Factory.getRemoteBean(LanguageManagerRemote.class, SecurityReflector.getInitialContextProperties());
		LanguageConfig languageConfig = lm.getLanguageConfig(new String[]{FetchPlan.DEFAULT}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		assertThat(languageConfig,notNullValue());
		LanguageSyncMode syncmode = languageConfig.getLanguageSyncMode();
		languageConfig.setLanguageSyncMode(LanguageSyncMode.off);
		languageConfig = lm.storeLanguageConfig(languageConfig, 
				true, 
				new String[]{FetchPlan.DEFAULT},
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		// switch language sync mode to Off
		assertThat(languageConfig.getLanguageSyncMode(),equalTo(LanguageSyncMode.off));
		languageConfig.setLanguageSyncMode(syncmode);	
		// restores back the original value
		languageConfig = lm.storeLanguageConfig(languageConfig, 
				false, 
				null,
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		assertThat(languageConfig,nullValue());
	}

	
	@Test
	public void testDeleteLanguage() throws Exception{	
		LanguageManagerRemote  lm = JFireEjb3Factory.getRemoteBean(LanguageManagerRemote.class, SecurityReflector.getInitialContextProperties());
		LanguageID newLanguageID = (LanguageID)getTestCaseContextObject(NEW_LANGUAGE);
		assertThat(newLanguageID,notNullValue()); 
		// delete the language !!!!			
		lm.deleteLanguage(newLanguageID);
		// verifiy that the language has been definitly deleted from the data store
		Collection<Language> languages = lm.getLanguages();		
		for (Language lang : languages) 
			assertThat(lang.getLanguageID(),not(equalTo(newLanguageID.languageID)));
	}
}
