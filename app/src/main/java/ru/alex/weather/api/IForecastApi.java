package ru.alex.weather.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.alex.weather.models.forecast.Forecast;

public interface IForecastApi {
    @GET("forecasts/v1/daily/5day/{keyCity}")
    Call<Forecast> getForecastEntries(@Path("keyCity") String keyCity,
                                      @Query("apikey") String apikey,
                                      @Query("language") String language,
                                      @Query("metric") String metric);
}
