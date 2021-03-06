package ru.alex.weather.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "weatherDB";
    public static final String TABLE_CITY = "city";
    public static final String ID = "id";
    public static final String CITY = "city";
    public static final String LOCATION = "location";
    public static final String WEATHER = "weather";
    public static final String COUNTRY = "country";

    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + TABLE_CITY + "(" + ID + " integer primary key not null, " +
                CITY + " text, " + LOCATION + " text unique, " + COUNTRY + " text )");
        db.execSQL("insert into " + TABLE_CITY + "(" + CITY + ", " + LOCATION + ", " + COUNTRY + ")"
                + " values ('Санкт-Петербург', '295212', 'Россия')");
        db.execSQL("insert into " + TABLE_CITY + "(" + CITY + ", " + LOCATION + ", " + COUNTRY + ")"
                + " values ('Москва', '294021', 'Россия')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
