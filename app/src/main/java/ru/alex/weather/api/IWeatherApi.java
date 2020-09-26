package ru.alex.weather.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.alex.weather.models.current.Weather;

public interface IWeatherApi {
    @GET("currentconditions/v1/{keyCity}")
    Call<List<Weather>> getWeatherEntries(@Path("keyCity") String keyCity,
                                 @Query("apikey") String apikey,
                                 @Query("language") String language);
}
