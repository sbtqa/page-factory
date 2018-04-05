package ru.sbtqa.tag.pagefactory.api;

import java.util.Locale;
import ru.sbtqa.tag.pagefactory.Page;
import ru.sbtqa.tag.qautils.i18n.I18N;

public class I18n {

    public static String getI18nActionTitle(Locale locale, String actionTitle) {
        I18N i18n = I18N.getI18n(Page.class, locale, I18N.DEFAULT_BUNDLE_PATH);
        return i18n.get(actionTitle);
    }

}
