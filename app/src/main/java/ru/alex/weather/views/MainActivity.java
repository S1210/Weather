package ru.alex.weather.views;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

import ru.alex.weather.R;
import ru.alex.weather.adpters.RecyclerViewCitiesAdapter;
import ru.alex.weather.contractors.MainContractor;
import ru.alex.weather.models.city.City;
import ru.alex.weather.models.current.Weather;
import ru.alex.weather.models.forecast.DailyForecast;
import ru.alex.weather.models.forecast.Forecast;
import ru.alex.weather.preferences.CityPreference;
import ru.alex.weather.presenters.MainPresenter;

public class MainActivity extends AppCompatActivity implements MainContractor.View {

    public static final String INSTANCE_CITY = "city";
    public static final String INSTANCE_WEATHER = "weather";

    private MainContractor.Presenter mPresenter;

    private DrawerLayout dlMain;
    private CoordinatorLayout clMain;
    private Toolbar toolbar;
    private MaterialEditText etCity;
    private LinearLayout llMainContent;
    private TextView tvCityField;
    private TextView tvWeather;
    private TextView tvWeatherOne;
    private TextView tvWeatherTwo;
    private TextView tvWeatherThree;
    private ProgressBar progressBar;
    private ImageButton btnSearch;
    private CityPreference cityPreference;
    private AlertDialog dialog;
    private NavigationView navigationView;
    private City city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPresenter = new MainPresenter(this);
        cityPreference = new CityPreference(MainActivity.this);
        init();
        initToolbar();
        mPresenter.checkWeatherToLocation(new City(cityPreference.getKey(),
                cityPreference.getCity(), cityPreference.getCountry()));
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.checkWeather(Objects.requireNonNull(etCity.getText()).toString());
                cityPreference.setCity(Objects.requireNonNull(etCity.getText()).toString());
            }
        });
    }

    private void init() {
        dlMain = findViewById(R.id.dl_main);
        clMain = findViewById(R.id.cl_main);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        llMainContent = findViewById(R.id.ll_main_content);
        etCity = findViewById(R.id.etCity);
        tvCityField = findViewById(R.id.tv_city_field);
        tvWeather = findViewById(R.id.tv_weather);
        tvWeatherOne = findViewById(R.id.tv_weather_one);
        tvWeatherTwo = findViewById(R.id.tv_weather_two);
        tvWeatherThree = findViewById(R.id.tv_weather_three);
        btnSearch = findViewById(R.id.btnSearch);
    }

    @SuppressLint("RestrictedApi")
    private void initToolbar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, dlMain, toolbar, R.string.open, R.string.close);
        dlMain.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.navigation_view_main);
        Menu menu = navigationView.getMenu();
        mPresenter.getMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!cityPreference.getCity().isEmpty()) {
            etCity.setText(cityPreference.getCity());
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        etCity.setText(savedInstanceState.getString(INSTANCE_CITY));
        tvWeather.setText(savedInstanceState.getString(INSTANCE_WEATHER));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INSTANCE_CITY, Objects.requireNonNull(etCity.getText()).toString());
        outState.putString(INSTANCE_WEATHER, tvWeather.getText().toString());
    }

    @Override
    public void showSnackbar(String message) {
        Snackbar.make(clMain, message, Snackbar.LENGTH_LONG).setTextColor(getResources()
                .getColor(R.color.icons)).show();
    }

    @Override
    public void closeAlertDialog() {
        dialog.dismiss();
    }

    @Override
    public void showAlertDialog(RecyclerViewCitiesAdapter citiesAdapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_city);
        LayoutInflater inflater = LayoutInflater.from(this);
        View selectCity = inflater.inflate(R.layout.list_cities, null);
        builder.setView(selectCity);
        RecyclerView rvCities = selectCity.findViewById(R.id.rv_cities);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rvCities.setLayoutManager(layoutManager);
        rvCities.setAdapter(citiesAdapter);
        dialog = builder.show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void setWeather(Weather weather, City city) {
        this.city = city;
        long lTemp = Math.round(weather.getTemperature().getMetric().getValue());
        String temp = lTemp > 0 ?
                "+" + lTemp : String.valueOf(lTemp);
        tvWeather.setText(temp + ", " + weather.getWeatherText());
        tvCityField.setText(city.getLocalizedName() + ", " + city.getCountry().getLocalizedName());
    }

    @Override
    public void setNavigationMenu(final List<City> cities) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                for (City city : cities) {
                    if (String.valueOf(item.getItemId()).equals(city.getKey())) {
                        etCity.setText(city.getLocalizedName());
                        mPresenter.checkWeatherToLocation(city);
                        break;
                    }
                }
                dlMain.closeDrawers();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Menu menu = navigationView.getMenu();
        mPresenter.saveMenu(city);
        mPresenter.getMenu(menu);
        return true;
    }

    @Override
    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setForecast(Forecast forecast) {
        try {
            setTemperatureForecast(tvWeatherOne, forecast.getDailyForecasts().get(1));
            setTemperatureForecast(tvWeatherTwo, forecast.getDailyForecasts().get(2));
            setTemperatureForecast(tvWeatherThree, forecast.getDailyForecasts().get(3));
        } catch (NullPointerException e) {
            showSnackbar(getString(R.string.error_limited));
        }
    }

    @Override
    public void showContent() {
        llMainContent.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void setTemperatureForecast(TextView textView, DailyForecast dailyForecast) {
        long lTempMin = Math.round(dailyForecast.getTemperature().getMinimum().getValue());
        long lTempMax = Math.round(dailyForecast.getTemperature().getMaximum().getValue());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd.MM");
        textView.setText(format.format(dailyForecast.getDate()) + " "
                + convertTemperatureToString(lTempMin) + "/" + convertTemperatureToString(lTempMax));
    }

    private String convertTemperatureToString(long temp) {
        return temp > 0 ? "+" + temp : String.valueOf(temp);
    }
}