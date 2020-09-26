package ru.alex.weather.api;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.alex.weather.models.city.City;
import ru.alex.weather.models.current.Weather;
import ru.alex.weather.models.forecast.Forecast;

public class ApiManager {
    private final ICityApi cityApi;
    private final IWeatherApi weatherApi;
    private final IForecastApi forecastApi;

    public ApiManager() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dataservice.accuweather.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        cityApi = retrofit.create(ICityApi.class);
        weatherApi = retrofit.create(IWeatherApi.class);
        forecastApi = retrofit.create(IForecastApi.class);
    }

    public void getCityEntries(String apiKey, String city, String language, Callback<List<City>> callback) {
        Call<List<City>> cityEntries = cityApi.getCityEntries(apiKey, city, language);
        cityEntries.enqueue(callback);
    }

    public Call<List<Weather>> getWeatherEntries(String keyCity, String apiKey, String language) {
        return weatherApi.getWeatherEntries(keyCity, apiKey, language);
    }

    public Call<Forecast> getForecastEntries(String keyCity, String apiKey, String language) {
        return forecastApi.getForecastEntries(keyCity, apiKey, language, "true");
    }
}
