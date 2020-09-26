package ru.alex.weather.presenters;

import android.content.Context;
import android.view.Menu;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.alex.weather.R;
import ru.alex.weather.adpters.RecyclerViewCitiesAdapter;
import ru.alex.weather.api.ApiManager;
import ru.alex.weather.contractors.MainContractor;
import ru.alex.weather.models.city.City;
import ru.alex.weather.models.current.Weather;
import ru.alex.weather.models.forecast.Forecast;

public class MainPresenter implements MainContractor.Presenter, Callback<List<City>> {

    public static final String API_KEY = "f4wFEkXB8c0kJTpNt919266jOD076O0L";
    public static final String LANGUAGE = "ru-ru";

    private MainContractor.View mView;
    private MainContractor.Repository mRepository;
    private RecyclerViewCitiesAdapter citiesAdapter;
    private ApiManager apiManager;

    public MainPresenter(MainContractor.View mView) {
        this.mView = mView;
        mRepository = new City((Context) mView);
        apiManager = new ApiManager();
    }

    @Override
    public void checkWeather(String city) {
        apiManager.getCityEntries(API_KEY, city, LANGUAGE, this);
    }

    @Override
    public void checkWeatherToLocation(final City city) {
        mView.showProgressBar();
        Call<List<Weather>> weatherCall = apiManager.getWeatherEntries(city.getKey(), API_KEY, LANGUAGE);
        weatherCall.enqueue(new Callback<List<Weather>>() {
            @Override
            public void onResponse(@NonNull Call<List<Weather>> call, @NonNull Response<List<Weather>> response) {
                final List<Weather> weatherList = response.body();
                Call<Forecast> forecastCall = apiManager.getForecastEntries(city.getKey(), API_KEY, LANGUAGE);
                forecastCall.enqueue(new Callback<Forecast>() {
                    @Override
                    public void onResponse(@NonNull Call<Forecast> call, @NonNull Response<Forecast> response) {
                        Forecast forecast = response.body();
                        mView.setForecast(forecast);
                        try {
                            mView.setWeather(Objects.requireNonNull(weatherList).get(0), city);
                            mView.showContent();
                        } catch (NullPointerException e) {
                            mView.showSnackbar(((Context) mView).getString(R.string.error_limited));
                        }
                        mView.hideProgressBar();
                    }

                    @Override
                    public void onFailure(@NonNull Call<Forecast> call, @NonNull Throwable t) {
                        mView.showSnackbar(((Context) mView).getString(R.string.error));
                        mView.hideProgressBar();
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<List<Weather>> call, @NonNull Throwable t) {
                mView.showSnackbar(((Context) mView).getString(R.string.error));
                mView.hideProgressBar();
            }
        });
    }

    @Override
    public void getMenu(Menu menu) {
        List<City> cities = mRepository.getCities();
        menu.clear();
        for (City city : cities) {
            menu.add(R.id.group_cities, Integer.parseInt(city.getKey()), 0, city.getLocalizedName());
        }
        mView.setNavigationMenu(cities);
    }

    @Override
    public void saveMenu(City city) {
        mRepository.insertCity(city);
        mView.showToast(((Context) mView).getString(R.string.city_save_message));
    }

    @Override
    public void onResponse(@NonNull final Call<List<City>> call, Response<List<City>> response) {
        if (response.isSuccessful()) {
            List<City> cityList = response.body();
            if (Objects.requireNonNull(cityList).size() > 1) {
                RecyclerViewCitiesAdapter.OnCityClickListener cityClickListener = new RecyclerViewCitiesAdapter.OnCityClickListener() {
                    @Override
                    public void onCityClick(City city) {
                        checkWeatherToLocation(city);
                        mView.closeAlertDialog();
                    }
                };
                citiesAdapter = new RecyclerViewCitiesAdapter(cityList, cityClickListener);
                mView.showAlertDialog(citiesAdapter);
            } else {
                checkWeatherToLocation(cityList.get(0));
            }
        }
    }

    @Override
    public void onFailure(@NonNull Call<List<City>> call, @NonNull Throwable t) {
        mView.showSnackbar(((Context) mView).getString(R.string.error));
    }
}
