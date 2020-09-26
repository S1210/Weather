package ru.alex.weather.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.alex.weather.models.city.City;

public interface ICityApi {
    @GET("locations/v1/cities/search")
    Call<List<City>> getCityEntries(@Query(value = "apikey", encoded = true) String apikey,
                                    @Query(value = "q", encoded = false) String q,
                                    @Query(value = "language", encoded = true) String language);
}