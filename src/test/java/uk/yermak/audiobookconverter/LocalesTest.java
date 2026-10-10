package uk.yermak.audiobookconverter;

import org.testng.annotations.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.testng.Assert.assertEquals;

public class LocalesTest {

    @Test
    public void languageCodesResolveToOwnBundle() {
        ResourceBundle.Control noFallback = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);
        for (String code : new String[]{"uk", "be", "kk", "el"}) {
            ResourceBundle bundle = ResourceBundle.getBundle("locales/messages", Locale.forLanguageTag(code), noFallback);
            assertEquals(bundle.getLocale().getLanguage(), code);
        }
    }

    @Test
    public void legacyCountryCodesMapToLanguageCodes() {
        assertEquals(new Settings().setLanguage("ua").getLanguage(), "uk");
        assertEquals(new Settings().setLanguage("by").getLanguage(), "be");
        assertEquals(new Settings().setLanguage("kz").getLanguage(), "kk");
        assertEquals(new Settings().setLanguage("gr").getLanguage(), "el");
        assertEquals(new Settings().setLanguage("de").getLanguage(), "de");
    }
}
