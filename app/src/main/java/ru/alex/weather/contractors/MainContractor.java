package ru.alex.weather.contractors;

import android.view.Menu;

import java.util.List;

import ru.alex.weather.adpters.RecyclerViewCitiesAdapter;
import ru.alex.weather.models.city.City;
import ru.alex.weather.models.current.Weather;
import ru.alex.weather.models.forecast.Forecast;

public interface MainContractor {
    interface View {

        void showSnackbar(String message);

        void closeAlertDialog();

        void showAlertDialog(RecyclerViewCitiesAdapter citiesAdapter);

        void setWeather(Weather weather, City city);

        void setNavigationMenu(List<City> cities);

        void showProgressBar();

        void hideProgressBar();

        void showToast(String message);

        void setForecast(Forecast forecast);

        void showContent();
    }

    interface Presenter {

        void checkWeather(String city);

        void checkWeatherToLocation(City city);

        void getMenu(Menu menu);

        void saveMenu(City city);
    }

    interface Repository {

        List<City> getCities();

        void insertCity(City city);
    }
}
