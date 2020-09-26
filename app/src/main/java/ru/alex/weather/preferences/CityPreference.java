package ru.alex.weather.preferences;

import android.app.Activity;
import android.content.SharedPreferences;

public class CityPreference {

    public static final String APP_PREFERENCES_CITY_NAME = "cityName";
    public static final String APP_PREFERENCES_CITY_KEY = "cityKey";
    public static final String APP_PREFERENCES_CITY_COUNTRY = "cityCountry";

    private SharedPreferences prefs;

    public CityPreference(Activity activity) {
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    public String getCity() {
        return prefs.getString(APP_PREFERENCES_CITY_NAME, "Санкт-Петербург");
    }

    public void setCity(String city) {
        prefs.edit().putString(APP_PREFERENCES_CITY_NAME, city).apply();
    }

    public String getKey() {
        return prefs.getString(APP_PREFERENCES_CITY_KEY, "295212");
    }

    public void setKey(String key) {
        prefs.edit().putString(APP_PREFERENCES_CITY_KEY, key).apply();
    }

    public String getCountry() {
        return prefs.getString(APP_PREFERENCES_CITY_COUNTRY, "Россия");
    }

    public void setCountry(String country) {
        prefs.edit().putString(APP_PREFERENCES_CITY_COUNTRY, country).apply();
    }

}
