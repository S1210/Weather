package ru.alex.weather;

import android.app.Activity;
import android.content.SharedPreferences;

public class CityPreference {

    public static final String APP_PREFERENCES_CITY = "city";

    private SharedPreferences prefs;

    public CityPreference(Activity activity) {
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    String getCity() {
        return prefs.getString(APP_PREFERENCES_CITY, "Санкт-Петербург");
    }

    void setCity(String city) {
        prefs.edit().putString(APP_PREFERENCES_CITY, city).apply();
    }

}
