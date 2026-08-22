package ru.superplushkin.twofactorauthapp.subclasses;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class LocaleHelper {
    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_LANGUAGE = "language";

    public static Context updateLocale(Context context) {
        String language = getCurrentLanguage(context);

        Locale locale = language.equals("en") ? Locale.ENGLISH : Locale.forLanguageTag("ru");
        Locale.setDefault(locale);

        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }

    public static String getCurrentLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "ru");
    }
    public static void setCurrentLanguage(Context context, String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }
}
