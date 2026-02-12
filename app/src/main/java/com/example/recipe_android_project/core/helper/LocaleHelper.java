package com.example.recipe_android_project.core.helper;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_LANGUAGE_CODE = "language_code";

    public static Context applyLocale(Context context) {
        String languageCode = getLanguage(context);
        return updateResources(context, languageCode);
    }

    public static String getLanguage(Context context) {
        SharedPreferences prefs = context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE_CODE, "en");
    }

    public static void setLanguage(Context context, String languageCode) {
        SharedPreferences prefs = context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_LANGUAGE_CODE, languageCode)
                .apply();
    }

    private static Context updateResources(
            Context context, String languageCode) {

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration(
                context.getResources().getConfiguration());
        config.setLocale(locale);
        config.setLayoutDirection(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(new LocaleList(locale));
            return context.createConfigurationContext(config);
        } else {
            context.getResources().updateConfiguration(
                    config,
                    context.getResources().getDisplayMetrics()
            );
            return context;
        }
    }
}
