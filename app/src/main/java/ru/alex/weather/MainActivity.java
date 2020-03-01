package ru.alex.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    public static final String CITY_PATH = "http://dataservice.accuweather.com/locations/v1/cities/search";
    public static final String API_KEY = "?apikey=f4wFEkXB8c0kJTpNt919266jOD076O0L";
    public static final String WEATHER_PATH = "http://dataservice.accuweather.com/currentconditions/v1/";
    public static final String LANGUAGE = "&language=ru-ru";
    public static final String INSTANCE_CITY = "city";
    public static final String INSTANCE_WEATHER = "weather";
    public static final String INSTANCE_ICON = "icon";

    private MaterialEditText etCity;
    private TextView tvCityField;
    private TextView tvWeather;
    private ImageView ivWeatherIcon;
    private ProgressBar progressBar;
    private DBHelper dbHelper;
    private CityPreference cityPreference;
    private DataCity dataCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataCity = new DataCity();
        etCity = findViewById(R.id.etCity);
        tvCityField = findViewById(R.id.tvCityField);
        tvWeather = findViewById(R.id.tvWeather);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        progressBar = findViewById(R.id.progressBar);
        ImageButton btnSearch = findViewById(R.id.btnSearch);
        dbHelper = new DBHelper(this);
        cityPreference = new CityPreference(MainActivity.this);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.INTERNET}, 0);
                } else {
                    cityPreference.setCity(Objects.requireNonNull(etCity.getText()).toString());
                    checkWeather();
                }
            }
        });
        dbHelper.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!cityPreference.getCity().isEmpty()) {
            etCity.setText(cityPreference.getCity());
            checkWeather();
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        etCity.setText(savedInstanceState.getString(INSTANCE_CITY));
        tvWeather.setText(savedInstanceState.getString(INSTANCE_WEATHER));
        insertIcon(savedInstanceState.getInt(INSTANCE_ICON, 0));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INSTANCE_CITY, Objects.requireNonNull(etCity.getText()).toString());
        outState.putString(INSTANCE_WEATHER, tvWeather.getText().toString());
        outState.putInt(INSTANCE_ICON, dataCity.getIcon());
    }

    private void checkWeather() {
        progressBar.setVisibility(View.VISIBLE);
        DownloadTask task = new DownloadTask();
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert mgr != null;
        mgr.hideSoftInputFromWindow(etCity.getWindowToken(), 0);
        try {
            task.execute(CITY_PATH + API_KEY + "&q=" + Objects.requireNonNull(etCity.getText()).toString() + LANGUAGE).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            URL url;
            HttpURLConnection httpURLConnection;
            try {
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result.append(current);
                    data = reader.read();
                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return "FAILED";
            }
        }


        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("FAILED")) {
                errorLoading();
            } else {
                try {
                    JSONArray jsonArray = new JSONArray(s);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    dataCity.setLocation(jsonObject.getInt("Key"));
                    dataCity.setCity(jsonObject.getString("LocalizedName"));
                    try {
                        DownloadTask task = new DownloadTask();
                        String j2 = task.execute(WEATHER_PATH + dataCity.getLocation() + MainActivity.API_KEY + LANGUAGE).get();
                        JSONArray array = new JSONArray(j2);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject1 = array.getJSONObject(i);
                            tvCityField.setText(dataCity.getCity());
                            dataCity.setWeather(getTemperature(jsonObject1.getJSONObject("Temperature").getJSONObject("Metric")
                                    .getDouble("Value")) + ", " + jsonObject1.getString("WeatherText"));
                            tvWeather.setText(dataCity.getWeather());
                            dataCity.setIcon(jsonObject1.getInt("WeatherIcon"));
                            insertIcon(dataCity.getIcon());
                        }
                        saveToBD();
                    } catch (InterruptedException | ExecutionException | JSONException e) {
                        e.printStackTrace();
                        errorLoading();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        }

        private void saveToBD() {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            @SuppressLint("Recycle")
            Cursor cursor = database.rawQuery("select " + DBHelper.LOCATION + " from "
                    + DBHelper.TABLE_CITY + " where " + DBHelper.LOCATION + " = " + dataCity.getLocation(), null, null);
            ContentValues values = new ContentValues();
            values.put(DBHelper.CITY, dataCity.getCity());
            values.put(DBHelper.LOCATION, dataCity.getLocation());
            values.put(DBHelper.ICON, dataCity.getIcon());
            values.put(DBHelper.WEATHER, dataCity.getWeather());
            if (cursor.getCount() == 0) {
                database.insert(DBHelper.TABLE_CITY, null, values);
            } else {
                database.update(DBHelper.TABLE_CITY, values, DBHelper.LOCATION + "=?",
                        new String[]{String.valueOf(dataCity.getLocation())});
            }
            cursor.close();
            database.close();
        }

        private void errorLoading() {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.error);
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            @SuppressLint("Recycle")
            Cursor cursor = database.query(DBHelper.TABLE_CITY, null, DBHelper.CITY + "=?",
                    new String[]{Objects.requireNonNull(etCity.getText()).toString()},
                    null, null, null);
            if (cursor.getCount() > 0) {
                builder.setMessage(R.string.error_message_with_data);
                cursor.moveToFirst();
                tvWeather.setText(cursor.getString(3));
                tvCityField.setText(cursor.getString(1));
                insertIcon(cursor.getInt(4));
            } else {
                builder.setMessage(R.string.error_message);
            }
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            progressBar.setVisibility(View.INVISIBLE);
            builder.show();
        }
    }

    private String getTemperature(double temp) {
        if (temp > 0) {
            return "+" + Math.round(temp);
        } else {
            return String.valueOf(Math.round(temp));
        }
    }

    private void insertIcon(int icon) {
        switch (icon) {
            case 1:
                ivWeatherIcon.setImageResource(R.drawable.s01);
                break;
            case 2:
                ivWeatherIcon.setImageResource(R.drawable.s02);
                break;
            case 3:
                ivWeatherIcon.setImageResource(R.drawable.s03);
                break;
            case 4:
                ivWeatherIcon.setImageResource(R.drawable.s04);
                break;
            case 5:
                ivWeatherIcon.setImageResource(R.drawable.s05);
                break;
            case 6:
                ivWeatherIcon.setImageResource(R.drawable.s06);
                break;
            case 7:
                ivWeatherIcon.setImageResource(R.drawable.s07);
                break;
            case 8:
                ivWeatherIcon.setImageResource(R.drawable.s08);
                break;
            case 11:
                ivWeatherIcon.setImageResource(R.drawable.s11);
                break;
            case 12:
                ivWeatherIcon.setImageResource(R.drawable.s12);
                break;
            case 13:
                ivWeatherIcon.setImageResource(R.drawable.s13);
                break;
            case 14:
                ivWeatherIcon.setImageResource(R.drawable.s14);
                break;
            case 15:
                ivWeatherIcon.setImageResource(R.drawable.s15);
                break;
            case 16:
                ivWeatherIcon.setImageResource(R.drawable.s16);
                break;
            case 17:
                ivWeatherIcon.setImageResource(R.drawable.s17);
                break;
            case 18:
                ivWeatherIcon.setImageResource(R.drawable.s18);
                break;
            case 19:
                ivWeatherIcon.setImageResource(R.drawable.s19);
                break;
            case 20:
                ivWeatherIcon.setImageResource(R.drawable.s20);
                break;
            case 21:
                ivWeatherIcon.setImageResource(R.drawable.s21);
                break;
            case 22:
                ivWeatherIcon.setImageResource(R.drawable.s22);
                break;
            case 23:
                ivWeatherIcon.setImageResource(R.drawable.s23);
                break;
            case 24:
                ivWeatherIcon.setImageResource(R.drawable.s24);
                break;
            case 25:
                ivWeatherIcon.setImageResource(R.drawable.s25);
                break;
            case 26:
                ivWeatherIcon.setImageResource(R.drawable.s26);
                break;
            case 27:
                ivWeatherIcon.setImageResource(R.drawable.s29);
                break;
            case 30:
                ivWeatherIcon.setImageResource(R.drawable.s30);
                break;
            case 31:
                ivWeatherIcon.setImageResource(R.drawable.s31);
                break;
            case 32:
                ivWeatherIcon.setImageResource(R.drawable.s32);
                break;
            case 33:
                ivWeatherIcon.setImageResource(R.drawable.s33);
                break;
            case 34:
                ivWeatherIcon.setImageResource(R.drawable.s34);
                break;
            case 35:
                ivWeatherIcon.setImageResource(R.drawable.s35);
                break;
            case 36:
                ivWeatherIcon.setImageResource(R.drawable.s36);
                break;
            case 37:
                ivWeatherIcon.setImageResource(R.drawable.s37);
                break;
            case 38:
                ivWeatherIcon.setImageResource(R.drawable.s38);
                break;
            case 39:
                ivWeatherIcon.setImageResource(R.drawable.s39);
                break;
            case 40:
                ivWeatherIcon.setImageResource(R.drawable.s40);
                break;
            case 41:
                ivWeatherIcon.setImageResource(R.drawable.s41);
                break;
            case 42:
                ivWeatherIcon.setImageResource(R.drawable.s42);
                break;
            case 43:
                ivWeatherIcon.setImageResource(R.drawable.s43);
                break;
            case 44:
                ivWeatherIcon.setImageResource(R.drawable.s44);
                break;
        }
    }
}
